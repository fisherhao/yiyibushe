package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 说明：节点编排抽象基类（业务侧继承它完成编排）。
 * <p>
 * 编排方式：业务子类在构造器里调用 {@link #registerChain(String, TaskNodeAction...)}，
 * 把同一任务类型的节点动作<strong>按执行顺序</strong>依次传入——
 * 数组顺序即执行顺序（第一个先执行、最后一个收口），框架没有排序值的概念，
 * 引擎用 for 循环按链数组从头跑到尾。
 * <p>
 * 任务类型（taskType）只在编排处声明一次：注册时即把 taskType 与链绑定，
 * 节点动作自身不再声明任务类型。注册时即做门槛校验：任务类型非空、链非空、
 * 节点类型（nodeType）链内唯一；运行期通过 {@link #requireChain(String)} 取链。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public abstract class TaskNodeStrategy {

    private static final Logger log = LogUtilExt.getLogger(TaskNodeStrategy.class);

    /** 任务类型 -> 有序节点动作链（数组顺序即执行顺序） */
    private final Map<String, List<TaskNodeAction>> chainMap = new ConcurrentHashMap<>();

    /**
     * 构造器（业务子类在构造器中调用 {@link #registerChain} 完成编排）
     */
    protected TaskNodeStrategy() {
    }

    /**
     * 注册一条任务类型的节点链：actions 的数组顺序即执行顺序。
     *
     * @param taskType
     *                  任务类型（如"下单"）
     * @param actions
     *                  有序节点动作（可变参数，从头排到尾）
     */
    protected void registerChain(String taskType, TaskNodeAction... actions) {
        if (StringUtilExt.isBlank(taskType)) {
            throw new BizException(BizErrorCode.TASK_CHAIN_TYPE_BLANK);
        }
        if (CollectionUtilExt.isEmpty(List.of(actions))) {
            throw new BizException(BizErrorCode.TASK_CHAIN_EMPTY);
        }
        for (TaskNodeAction action : actions) {
            checkAction(action);
        }
        checkNodeTypeUnique(actions);
        chainMap.put(taskType, List.of(actions));
        LogUtilExt.info(log, "[FlowTask] 节点链就绪 taskType={0} 节点数={1}", taskType, actions.length);
    }

    /**
     * 按任务类型取编排好的节点链（数组顺序即执行顺序）
     *
     * @param taskType
     *     任务类型
     * @return 有序节点动作链
     */
    public List<TaskNodeAction> requireChain(String taskType) {
        List<TaskNodeAction> chain = chainMap.get(taskType);
        if (CollectionUtilExt.isEmpty(chain)) {
            throw new BizException(BizErrorCode.TASK_CHAIN_MISSING);
        }
        return chain;
    }

    /**
     * 校验单个动作：必须声明节点类型（任务类型由编排处声明，节点无需自带）
     *
     * @param action
     *     节点动作
     */
    private static void checkAction(TaskNodeAction action) {
        if (StringUtilExt.isBlank(action.getNodeType())) {
            throw new BizException(BizErrorCode.TASK_NODE_TYPE_BLANK);
        }
    }

    /**
     * 校验同一条链内 nodeType 不允许重复
     *
     * @param actions
     *     节点动作数组
     */
    private static void checkNodeTypeUnique(TaskNodeAction... actions) {
        for (int index = 0; index < actions.length; index++) {
            for (int later = index + 1; later < actions.length; later++) {
                if (StringUtilExt.equals(actions[index].getNodeType(),
                        actions[later].getNodeType())) {
                    throw new BizException(BizErrorCode.TASK_NODE_TYPE_DUPLICATED);
                }
            }
        }
    }
}