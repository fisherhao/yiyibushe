package com.dayu.yiyibushe.infra.flowtask;

/**
 * 说明：任务回调消息（MQ 消息体），只带业务任务 ID。
 * <p>
 * 回调不带任何数据——数据都在任务的 ext_info / task_context 里；
 * 引擎按 taskId 找到任务后，用 currentNodeType 直接定位待回调节点。
 * 外部系统处理完成后，回调入口方发送本消息到 {@link #TOPIC}，
 * {@code TaskCallbackListener} 消费后驱动 {@link TaskEngine}。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public class TaskCallbackMessage {

    /** 回调消息固定 topic */
    public static final String TOPIC = "FLOW_TASK_CALLBACK_TOPIC";

    /** 目标任务 ID */
    private Long taskId;

    /**
     * 默认构造器（反序列化使用）
     */
    public TaskCallbackMessage() {
    }

    /**
     * 私有构造器，统一走 {@link #of(Long)}
     *
     * @param taskId
     *     目标任务 ID
     */
    private TaskCallbackMessage(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * 构造回调消息
     *
     * @param taskId
     *     目标任务 ID
     * @return 回调消息
     */
    public static TaskCallbackMessage of(Long taskId) {
        return new TaskCallbackMessage(taskId);
    }

    /**
     * 获取目标任务 ID
     *
     * @return 任务 ID
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * 设置目标任务 ID
     *
     * @param taskId
     *     任务 ID
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
