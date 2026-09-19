package com.dayu.yiyibushe.app.task;

import com.dayu.yiyibushe.infra.flowtask.TaskContext;
import com.dayu.yiyibushe.infra.flowtask.TaskNodeAction;
import com.dayu.yiyibushe.infra.flowtask.TaskNodeResult;

/**
 * 说明：演示节点 passThrough——链尾节点，直接成功，用于验证
 * "submitCallback -> failOnce -> passThrough 顺序执行、全部成功后任务收口"。
 * 执行顺序由 {@link LoadDemoTaskNodeStrategy} 的编排数组决定，本节点排在链尾；
 * 本节点无回调场景，不重写 receipt（接口默认返回 null）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public class PassThroughNode implements TaskNodeAction {

    /** 节点类型 */
    public static final String NODE_TYPE = "passThrough";

    /**
     * 声明节点类型
     *
     * @return passThrough
     */
    @Override
    public String getNodeType() {
        return NODE_TYPE;
    }

    /**
     * 直接执行成功
     *
     * @param context
     *     任务上下文
     * @return 成功结果
     */
    @Override
    public TaskNodeResult execute(TaskContext context) {
        System.out.println("[FlowTask-演示] passThrough 执行成功，链路收口");
        return TaskNodeResult.success("passThrough 完成");
    }
}
