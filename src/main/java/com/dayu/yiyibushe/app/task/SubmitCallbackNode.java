package com.dayu.yiyibushe.app.task;

import com.dayu.yiyibushe.infra.flowtask.TaskContext;
import com.dayu.yiyibushe.infra.flowtask.TaskNodeAction;
import com.dayu.yiyibushe.infra.flowtask.TaskNodeResult;

/**
 * 说明：演示节点 submitCallback——模拟"提交外部系统后等待回调"的异步节点。
 * <p>
 * execute 直接返回待回调；receipt 收到回调后成功。
 * 执行顺序由 {@link LoadDemoTaskNodeStrategy} 的编排数组决定，本节点排在链首；
 * 任务类型（TASK_TYPE）由编排处声明，本节点不再自带。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public class SubmitCallbackNode implements TaskNodeAction {

    /** 演示任务类型（三个 load 节点共用，由编排处声明） */
    public static final String TASK_TYPE = "LOAD_DEMO";

    /** 节点类型 */
    public static final String NODE_TYPE = "submitCallback";

    /**
     * 声明节点类型
     *
     * @return submitCallback
     */
    @Override
    public String getNodeType() {
        return NODE_TYPE;
    }

    /**
     * 模拟提交外部系统，返回待回调
     *
     * @param context
     *     任务上下文
     * @return 待回调结果
     */
    @Override
    public TaskNodeResult execute(TaskContext context) {
        System.out.println("[FlowTask-演示] submitCallback 已提交外部系统，等待回调");
        return TaskNodeResult.waiting();
    }

    /**
     * 处理回调（回调不带数据，结果以任务 task_context 里的信息为准），模拟处理成功
     *
     * @param context
     *     任务上下文
     * @return 成功结果
     */
    @Override
    public TaskNodeResult receipt(TaskContext context) {
        System.out.println("[FlowTask-演示] submitCallback 收到回调，处理完成");
        return TaskNodeResult.success("回调处理完成");
    }
}
