package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 说明：FlowTask 任务模板，业务创建任务的统一入口（对齐 RocketMQTemplate 的定位）。
 * <p>
 * 创建任务时按任务类型从 {@link TaskNodeStrategy} 取出编排好的节点链，
 * 为每个节点生成初始快照，连同 gmtFire / priority / 重试预算 / 重试策略 /
 * 业务数据一并入库，返回自增任务 ID。
 * 支持自动触发（gmtFire 到点由定时任务 trigger）与手动触发（submitNow / HTTP）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Component
public class FlowTaskTemplate {

    private static final Logger log = LogUtilExt.getLogger(FlowTaskTemplate.class);

    /** 默认最大重试次数 */
    public static final int DEFAULT_MAX_RETRY = 3;

    @Autowired
    private TaskNodeStrategy nodeStrategy;

    @Autowired
    private TaskStore taskStore;

    /**
     * 使用默认参数创建任务：默认优先级、最大重试 3 次、重试策略走全局默认
     *
     * @param taskType
     *                      任务类型（该类型下必须存在节点 Bean）
     * @param gmtFireMillis
     *                      期望调度时间（毫秒时间戳，早于当前时间则到点即被 trigger）
     * @param context
     *                      任务级业务数据
     * @return 任务 ID
     */
    public Long createTask(String taskType, long gmtFireMillis, Map<String, Object> context) {
        return createTask(taskType, gmtFireMillis, FlowTask.DEFAULT_PRIORITY, context,
                DEFAULT_MAX_RETRY, null);
    }

    /**
     * 创建任务：自定义重试预算与重试策略，优先级走默认值
     *
     * @param taskType
     *                          任务类型
     * @param gmtFireMillis
     *                          期望调度时间（毫秒时间戳）
     * @param context
     *                          任务级业务数据
     * @param maxRetry
     *                          最大重试次数
     * @param retryStrategyCode
     *                          重试间隔策略 code（空=全局默认策略）
     * @return 任务 ID
     */
    public Long createTask(String taskType, long gmtFireMillis, Map<String, Object> context,
            int maxRetry, String retryStrategyCode) {
        return createTask(taskType, gmtFireMillis, FlowTask.DEFAULT_PRIORITY, context,
                maxRetry, retryStrategyCode);
    }

    /**
     * 创建任务（完整参数）：校验类型 -> 节点策略取有序链 -> 生成初始节点快照 -> 入库。
     * 任务类型（taskType）只写入任务上下文一份（键见 {@link TaskContext#KEY_TASK_TYPE}），
     * 引擎按它取链，任务表不再单独存列。
     *
     * @param taskType
     *                          任务类型（该类型下必须存在节点 Bean）
     * @param gmtFireMillis
     *                          期望调度时间（毫秒时间戳）
     * @param priority
     *                          优先级（数值越小越优先）
     * @param context
     *                          任务级业务数据
     * @param maxRetry
     *                          最大重试次数
     * @param retryStrategyCode
     *                          重试间隔策略 code（空=全局默认策略）
     * @return 任务 ID
     */
    public Long createTask(String taskType, long gmtFireMillis, int priority,
            Map<String, Object> context, int maxRetry, String retryStrategyCode) {
        if (StringUtilExt.isBlank(taskType)) {
            throw new BizException(ParamErrorCode.TASK_TYPE_BLANK);
        }
        // 只创建任务主表（flow_task），不预建节点记录：
        // task_node 由引擎在执行到对应节点时一步步创建，通过 task_id 关联、node_order 排序。
        List<TaskNodeAction> chain = nodeStrategy.requireChain(taskType);
        if (CollectionUtilExt.getSize(chain) == 0) {
            throw new BizException(BizErrorCode.TASK_CHAIN_MISSING);
        }
        // taskType 与业务数据共用一份上下文 Map（业务方未传则用空 Map 兜底）
        Map<String, Object> taskContext = Objects.requireNonNullElse(context, new HashMap<>());
        taskContext.put(TaskContext.KEY_TASK_TYPE, taskType);
        FlowTask task = FlowTask.init(gmtFireMillis, priority, taskContext, maxRetry,
                retryStrategyCode);
        // 当前指针指向链首节点：下次唤起从这里开始执行
        task.setCurrentNodeType(chain.get(0).getNodeType());
        Long taskId = taskStore.save(task);
        LogUtilExt.info(log, "[FlowTask] 任务已创建并入库 taskId={0} taskType={1} 首节点={2} 节点数={3} priority={4} retryStrategy={5}",
                taskId, taskType, task.getCurrentNodeType(), chain.size(), priority,
                Objects.requireNonNullElse(retryStrategyCode, "DEFAULT"));
        return taskId;
    }

    /**
     * 手动触发任务：gmtFire 设为当前时间，下次 trigger 即执行
     *
     * @param taskType
     *                 任务类型
     * @param context
     *                 任务级业务数据
     * @return 任务 ID
     */
    public Long submitNow(String taskType, Map<String, Object> context) {
        return createTask(taskType, System.currentTimeMillis(), context);
    }

    /**
     * 按任务 ID 查询任务，不存在抛业务异常
     *
     * @param taskId
     *               任务 ID
     * @return 任务
     */
    public FlowTask requireTask(Long taskId) {
        if (Objects.isNull(taskId)) {
            throw new BizException(ParamErrorCode.TASK_ID_BLANK);
        }
        FlowTask task = taskStore.findByTaskId(taskId);
        if (Objects.isNull(task)) {
            throw new BizException(BizErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }
}