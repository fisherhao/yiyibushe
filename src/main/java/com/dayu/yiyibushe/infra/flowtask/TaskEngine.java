package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.infra.flowtask.retry.RetryStrategy;
import com.dayu.yiyibushe.infra.flowtask.retry.RetryStrategyRegistry;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 说明：flowtask 执行引擎，负责任务状态机的全部流转。
 * <p>
 * 框架只提供两个入口，捞取与分发由调用方负责（定时任务只捞 INIT 且到点的任务，
 * for 循环调 {@link #trigger(Long)}；回调走 {@link #callback(Long)}）：
 * 两个入口各自过任务级状态门槛后，统统汇入唯一的流转核心
 * {@link #advanceTask(FlowTask, boolean)}——按当前节点状态分叉：
 * 待执行走 execute，待回调（回调已到达）走 receipt，成功推进、失败重试均在其中处理。
 * 并发约定：任务 ID 全局唯一分发，引擎不做任何加锁；并发安全完全靠
 * 状态机硬门槛（每次流转只允许合法前置状态，门槛不过直接忽略），
 * 多实例部署时把"捞取翻状态"换成数据库条件 UPDATE 即可跨实例分发。
 * <p>
 * 每处理一个节点都会同时落 flow_task 与 task_node 两张表；
 * 任务执行上下文（task_context）随推进更新，下次唤起直接从库里拿。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.7
 */
@Component
public class TaskEngine {

    private static final Logger log = LogUtilExt.getLogger(TaskEngine.class);

    /** 引擎内部防御：节点返回空结果时的失败原因 */
    private static final String NODE_RESULT_NULL_MESSAGE = "NODE_RESULT_NULL";

    private final TaskNodeStrategy nodeStrategy;
    private final RetryStrategyRegistry retryStrategyRegistry;
    private final TaskStore taskStore;

    /**
     * 构造器
     *
     * @param nodeStrategy
     *                              节点编排策略（业务侧显式编排的有序节点链）
     * @param retryStrategyRegistry
     *                              重试间隔策略注册解析器
     * @param taskStore
     *                              任务仓库
     */
    public TaskEngine(TaskNodeStrategy nodeStrategy, RetryStrategyRegistry retryStrategyRegistry,
            TaskStore taskStore) {
        this.nodeStrategy = nodeStrategy;
        this.retryStrategyRegistry = retryStrategyRegistry;
        this.taskStore = taskStore;
    }

    /**
     * 触发一个任务：INIT -> RUNNING，然后 for-each 推进节点链。
     * 只响应 INIT 任务；RUNNING / SUCCESS / FAILED 直接忽略打日志
     * （重试也是失败后先回到 INIT，再由调用方触发）。
     *
     * @param taskId
     *               业务任务 ID
     */
    public void trigger(Long taskId) {
        if (Objects.isNull(taskId)) {
            throw new BizException(ParamErrorCode.TASK_ID_BLANK);
        }
        FlowTask task = taskStore.findByTaskId(taskId);
        if (Objects.isNull(task)) {
            throw new BizException(BizErrorCode.TASK_NOT_FOUND);
        }
        // 状态机硬门槛：只有 INIT 允许开跑
        if (task.getStatus() != FlowTaskStatus.INIT) {
            LogUtilExt.warn(log, "[FlowTask] 任务非 INIT 状态被忽略 status={0} taskId={1}", task.getStatus(), taskId);
            return;
        }
        task.setStatus(FlowTaskStatus.RUNNING);
        touch(task);
        taskStore.save(task);
        LogUtilExt.info(log, "[FlowTask] 任务开始执行 taskId={0} 从节点 {1} 继续", taskId, task.getCurrentNodeType());
        advanceTask(task, false);
    }

    /**
     * 回调唤醒：不带任何数据（数据都在 task 的 task_context / ext_info 里）。
     * 只做两道任务级门槛（任务存在、状态 RUNNING），节点级的"当前节点必须 WAIT"
     * 门槛与 receipt 执行全部在 {@link #advanceTask(FlowTask, boolean)} 内完成。
     *
     * @param taskId
     *               业务任务 ID
     */
    public void callback(Long taskId) {
        if (Objects.isNull(taskId)) {
            throw new BizException(ParamErrorCode.TASK_ID_BLANK);
        }
        FlowTask task = taskStore.findByTaskId(taskId);
        if (Objects.isNull(task)) {
            LogUtilExt.warn(log, "[FlowTask] 回调目标任务不存在 taskId={0}", taskId);
            return;
        }
        // 硬门槛：回调只服务执行中的任务
        if (task.getStatus() != FlowTaskStatus.RUNNING) {
            LogUtilExt.warn(log, "[FlowTask] 回调被忽略，任务状态={0} taskId={1}", task.getStatus(), taskId);
            return;
        }
        advanceTask(task, true);
    }

    /**
     * 推进节点链（引擎核心，执行与回调共用的唯一流转方法）：for-each 从当前指针
     * （currentNodeType）开始遍历编排数组，按当前节点状态分叉——
     * 待执行（INIT/FAILED 重试续跑）走 execute，待回调（WAIT，外围回调已调到）走 receipt。
     * 每处理一个节点同时落 flow_task 与 task_node 两张表。
     * <ul>
     * <li>指针前的节点：已 SUCCESS，跳过（断点续跑不重复执行）；</li>
     * <li>指针节点已 SUCCESS：指针继续前移；</li>
     * <li>指针节点 WAIT：回调到达（callbackArrived=true）则执行 receipt，
     * 普通触发则暂停推进，等 callback 唤醒；</li>
     * <li>指针节点 INIT / FAILED（重试续跑）：执行 execute。</li>
     * </ul>
     *
     * @param task
     *                        执行中任务（必须已处于 RUNNING）
     * @param callbackArrived
     *                        true 表示外围回调已到达（WAIT 节点执行 receipt）；false 表示普通触发推进
     */
    private void advanceTask(FlowTask task, boolean callbackArrived) {
        if (task.getStatus() != FlowTaskStatus.RUNNING) {
            return;
        }
        // 回调门槛：只有指针停在待回调节点上回调才有效（防重复投递、防误执行）
        if (callbackArrived) {
            List<TaskNode> records = task.getNodeRecords();
            int currentIndex = indexOfNodeType(records, task.getCurrentNodeType());
            if (currentIndex < 0 || !records.get(currentIndex).isWaitingCallback()) {
                LogUtilExt.warn(log, "[FlowTask] 回调被忽略，当前节点不在待回调状态 nodeType={0} taskId={1}",
                        task.getCurrentNodeType(), task.getTaskId());
                return;
            }
        }
        TaskContext context = buildContext(task);
        List<TaskNodeAction> chain = nodeStrategy.requireChain(getTaskType(task));
        boolean reached = false;
        for (TaskNodeAction node : chain) {
            if (!reached) {
                if (!node.getNodeType().equals(task.getCurrentNodeType())) {
                    continue;
                }
                reached = true;
            }
            TaskNode record = ensureNodeRecord(task, node);
            // 已成功节点直接跳过
            if (record.getStatus() == TaskNodeStatus.SUCCESS) {
                moveForward(task, indexOfNodeType(task.getNodeRecords(), node.getNodeType()));
                continue;
            }
            // 核心分叉：待回调且回调已到达 -> receipt；否则（INIT/FAILED 重试续跑）-> execute
            TaskNodeResult result;
            if (record.isWaitingCallback()) {
                if (!callbackArrived) {
                    // 普通触发推进遇到待回调节点：暂停，等 callback 唤醒
                    taskStore.save(task);
                    return;
                }
                record.setStatus(TaskNodeStatus.RUNNING);
                touch(task);
                taskStore.save(task);
                LogUtilExt.info(log, "[FlowTask] 收到回调，节点开始处理 nodeType={0} taskId={1}",
                        record.getNodeType(), task.getTaskId());
                result = invokeReceipt(node, context);
                if (Objects.isNull(result)) {
                    // 节点未实现 receipt（不支持回调），回 WAIT 继续等
                    record.setStatus(TaskNodeStatus.WAIT);
                    touch(task);
                    taskStore.save(task);
                    LogUtilExt.info(log, "[FlowTask] 节点不支持回调，继续等待 nodeType={0} taskId={1}",
                            record.getNodeType(), task.getTaskId());
                    return;
                }
            } else {
                record.setStatus(TaskNodeStatus.RUNNING);
                touch(task);
                taskStore.save(task);
                result = invokeExecute(node, context);
            }
            applyResult(task, record, result);
            if (record.getStatus() == TaskNodeStatus.FAILED) {
                handleNodeFailure(task, record);
                return;
            }
            if (record.getStatus() == TaskNodeStatus.WAIT) {
                return;
            }
            // 节点成功：指针移到下一个节点（最后一个节点成功后指针停在链尾）
            moveForward(task, indexOfNodeType(task.getNodeRecords(), node.getNodeType()));
        }
        // for-each 自然走完：全部节点 SUCCESS，任务收口
        task.setStatus(FlowTaskStatus.SUCCESS);
        touch(task);
        taskStore.save(task);
        LogUtilExt.info(log, "[FlowTask] 任务执行成功 taskId={0}", task.getTaskId());
    }

    /**
     * 指针前移：把 currentNodeType 更新为链上下一个节点的类型并落库
     *
     * @param task
     *                  目标任务
     * @param nodeIndex
     *                  当前节点的链下标
     */
    private void moveForward(FlowTask task, int nodeIndex) {
        List<TaskNodeAction> chain = nodeStrategy.requireChain(getTaskType(task));
        String nextNodeType = "";
        if (nodeIndex + 1 < CollectionUtilExt.getSize(chain)) {
            nextNodeType = chain.get(nodeIndex + 1).getNodeType();
        }
        task.setCurrentNodeType(nextNodeType);
        touch(task);
        taskStore.save(task);
    }

    /**
     * 确保链上指定节点已有执行记录：没有则补建一条（INIT 态）并单条落库
     *
     * @param task
     *             目标任务
     * @param node
     *             链上的节点动作
     * @return 节点记录
     */
    private TaskNode ensureNodeRecord(FlowTask task, TaskNodeAction node) {
        List<TaskNode> records = task.getNodeRecords();
        int nodeIndex = indexOfNodeType(records, node.getNodeType());
        if (nodeIndex < 0) {
            TaskNode record = TaskNode.init(node.getNodeType(), CollectionUtilExt.getSize(records));
            record.setTaskId(task.getTaskId());
            records.add(record);
            taskStore.saveNode(record);
        }
        return records.get(indexOfNodeType(records, node.getNodeType()));
    }

    /**
     * 节点失败后的重试判定（重试次数是<strong>任务级</strong>的，记在 flow_task.retryCount 上，
     * task_node 不存重试次数）：有预算（retryCount 未达 maxRetry）-> flow_task 回 INIT、
     * gmtFire = 当前时间 + 重试间隔，指针仍停在失败节点上，下次触发就从当前节点继续执行；
     * 预算耗尽 -> flow_task 终态 FAILED。调用方只管捞 INIT 且到点的任务。
     *
     * @param task
     *               所属任务
     * @param record
     *               失败节点记录
     */
    private void handleNodeFailure(FlowTask task, TaskNode record) {
        // 先累加任务重试次数再判断预算：本次失败若恰好用完预算，必须直接终态失败
        task.setRetryCount(task.getRetryCount() + 1);
        if (task.hasRetryBudget()) {
            RetryStrategy retryStrategy = retryStrategyRegistry.resolve(task.getRetryStrategyCode());
            long retryIntervalMillis = retryStrategy.nextIntervalMillis(task.getRetryCount());
            task.setStatus(FlowTaskStatus.INIT);
            task.setGmtFire(System.currentTimeMillis() + retryIntervalMillis);
            touch(task);
            taskStore.save(task);
            LogUtilExt.warn(log, "[FlowTask] 节点失败待重试 nodeType={0} taskId={1} 已重试次数={2}/{3} 原因={4} 已回 INIT，"
                            + "下次从节点 {0} 继续执行，可触发时间={1}",
                    record.getNodeType(), task.getTaskId(), task.getRetryCount(), task.getMaxRetry(),
                    record.getFailMessage(), task.getCurrentNodeType(), task.getGmtFire());
            return;
        }
        task.setStatus(FlowTaskStatus.FAILED);
        touch(task);
        taskStore.save(task);
        LogUtilExt.error(log, "[FlowTask] 任务最终失败（重试预算耗尽）taskId={0} 失败节点={1} 原因={2}",
                task.getTaskId(), record.getNodeType(), record.getFailMessage());
    }

    /**
     * 把节点执行结果落到节点记录与任务上（各自单条落库，不重写整条链）
     *
     * @param task
     *               所属任务
     * @param record
     *               节点记录
     * @param result
     *               执行结果
     */
    private void applyResult(FlowTask task, TaskNode record, TaskNodeResult result) {
        if (Objects.isNull(result) || !result.isValid()) {
            record.setStatus(TaskNodeStatus.FAILED);
            record.setOutput(null);
            record.setFailMessage(NODE_RESULT_NULL_MESSAGE);
            touch(task);
            taskStore.save(task);
            taskStore.saveNode(record);
            return;
        }
        switch (result.getStatus()) {
            case SUCCESS -> {
                record.setStatus(TaskNodeStatus.SUCCESS);
                record.setOutput(result.getOutput());
                record.setFailMessage(null);
                // 节点输出写入任务执行上下文（随任务落库，续跑节点直接从库里拿）
                if (Objects.nonNull(result.getOutput())) {
                    task.getTaskContext().put(record.getNodeType(), result.getOutput());
                }
            }
            case FAILED -> {
                record.setStatus(TaskNodeStatus.FAILED);
                record.setOutput(null);
                record.setFailMessage(result.getMessage());
            }
            case WAIT -> record.setStatus(TaskNodeStatus.WAIT);
            default -> {
                // INIT / RUNNING 不是合法的节点返回值，按失败处理
                record.setStatus(TaskNodeStatus.FAILED);
                record.setFailMessage("INVALID_NODE_STATUS_" + result.getStatus());
            }
        }
        touch(task);
        taskStore.save(task);
        taskStore.saveNode(record);
    }

    /**
     * 按节点类型定位节点记录下标
     *
     * @param records
     *                 节点记录列表
     * @param nodeType
     *                 目标节点类型
     * @return 下标，不存在返回 -1
     */
    private int indexOfNodeType(List<TaskNode> records, String nodeType) {
        for (int index = 0; index < CollectionUtilExt.getSize(records); index++) {
            if (records.get(index).matchesNode(nodeType)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * 构建任务上下文：底层就是 flow_task.task_context 那份 Map（库里拿，含任务类型与节点输出）
     *
     * @param task
     *             目标任务
     * @return 任务上下文
     */
    private TaskContext buildContext(FlowTask task) {
        return new TaskContext(task.getTaskId(), task.getTaskContext());
    }

    /**
     * 从任务上下文读取任务类型（创建任务时由模板写入，取链的唯一依据）
     *
     * @param task
     *             目标任务
     * @return 任务类型
     */
    private String getTaskType(FlowTask task) {
        return CollectionUtilExt.getString(task.getTaskContext(), TaskContext.KEY_TASK_TYPE);
    }

    /**
     * 执行节点 execute，异常统一转为失败结果
     *
     * @param node
     *                目标节点动作
     * @param context
     *                任务上下文
     * @return 执行结果
     */
    private TaskNodeResult invokeExecute(TaskNodeAction node, TaskContext context) {
        try {
            return node.execute(context);
        } catch (Exception e) {
            LogUtilExt.error(log, "[FlowTask] 节点 execute 异常 nodeType={0}", node.getNodeType(), e);
            return TaskNodeResult.failed(node.getClass().getSimpleName() + ": "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * 执行节点 receipt，异常统一转为失败结果
     *
     * @param node
     *                目标节点动作
     * @param context
     *                任务上下文
     * @return 回调处理结果
     */
    private TaskNodeResult invokeReceipt(TaskNodeAction node, TaskContext context) {
        try {
            return node.receipt(context);
        } catch (Exception e) {
            LogUtilExt.error(log, "[FlowTask] 节点 receipt 异常 nodeType={0}", node.getNodeType(), e);
            return TaskNodeResult.failed(node.getClass().getSimpleName() + ": "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * 刷新任务最后修改时间
     *
     * @param task
     *             目标任务
     */
    private void touch(FlowTask task) {
        task.setGmtModify(System.currentTimeMillis());
    }
}