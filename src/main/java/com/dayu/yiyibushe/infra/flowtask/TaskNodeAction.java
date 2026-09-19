package com.dayu.yiyibushe.infra.flowtask;

/**
 * 说明：任务节点动作契约（接口），是 flowtask 框架的业务执行单元。
 * <p>
 * 节点只声明自己的类型（{@link #getNodeType()}）并实现执行动作，
 * <strong>不声明任务类型</strong>——节点属于哪条任务链由编排处
 * {@link TaskNodeStrategy#registerChain(String, TaskNodeAction...)} 声明，
 * 知道 taskType 就知道它的节点，节点无需重复登记。
 * <p>
 * 一个节点有两类驱动方式：
 * <ol>
 * <li>{@link #execute(TaskContext)}：节点被调度到时执行，返回成功/失败/待回调；</li>
 * <li>{@link #receipt(TaskContext)}：节点处于待回调状态时，外部 callback 唤醒后由引擎调用；
 * 无回调场景的节点不用重写（默认返回 null，引擎按"不支持回调"忽略）。</li>
 * </ol>
 * 另有可选的 {@link #poll(TaskContext)} 轮询兜底能力（默认不支持，返回 null）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public interface TaskNodeAction {

    /**
     * 获取节点类型：同一任务链内不可重复（如 PRE_CHECK / DEDUCT_STOCK / PAY）
     *
     * @return 节点类型（业务侧通常用枚举的 name）
     */
    String getNodeType();

    /**
     * 执行节点业务：节点被调度到时调用一次
     *
     * @param context
     *                任务上下文（含业务数据与上游节点输出）
     * @return 执行结果（成功 / 失败 / 待回调）
     */
    TaskNodeResult execute(TaskContext context);

    /**
     * 处理回调：节点处于待回调状态、外部 callback 唤醒时由引擎调用；
     * 无回调场景的节点不用重写（默认返回 null，引擎按"不支持回调"忽略）
     *
     * @param context
     *                任务上下文
     * @return 回调处理结果（成功收口 / 失败 / 继续待回调）；null 表示本节点不支持回调
     */
    default TaskNodeResult receipt(TaskContext context) {
        return null;
    }

    /**
     * 轮询兜底（节点能力预留）：默认不支持（返回 null），需要兜底的节点自行重写
     *
     * @param context
     *                任务上下文
     * @return 查询结果；返回 null 表示本节点不支持轮询
     */
    default TaskNodeResult poll(TaskContext context) {
        return null;
    }
}
