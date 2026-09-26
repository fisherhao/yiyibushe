package com.dayu.yiyibushe.infra.flowtask;

import java.util.Objects;

/**
 * 说明：节点单次执行/回调的结果，是 {@link TaskNodeAction} 各方法统一的返回值。
 * <p>
 * 引擎根据结果里的状态推进节点状态机：
 * success -> 节点成功收口并继续下一个节点；
 * failed -> 节点失败，进入任务重试判定；
 * wait -> 节点进入待回调，暂停推进等外部 callback 唤醒。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public class TaskNodeResult {

    /** 结果状态（只允许 SUCCESS / FAILED / WAIT 三种） */
    private TaskNodeStatus status;

    /** 节点输出（成功或待回调时可携带，会持久化并作为后续节点入参） */
    private Object output;

    /** 附加消息（失败时为失败原因） */
    private String message;

    /**
     * 构造成功结果
     *
     * @param output
     *     节点输出
     * @return 成功结果
     */
    public static TaskNodeResult success(Object output) {
        TaskNodeResult result = new TaskNodeResult();
        result.status = TaskNodeStatus.SUCCESS;
        result.output = output;
        return result;
    }

    /**
     * 构造失败结果
     *
     * @param message
     *     失败原因
     * @return 失败结果
     */
    public static TaskNodeResult failed(String message) {
        TaskNodeResult result = new TaskNodeResult();
        result.status = TaskNodeStatus.FAILED;
        result.message = message;
        return result;
    }

    /**
     * 构造待回调结果：节点已把请求提交给外部系统，等外部 callback 唤醒后走 receipt
     *
     * @return 待回调结果
     */
    public static TaskNodeResult waiting() {
        TaskNodeResult result = new TaskNodeResult();
        result.status = TaskNodeStatus.WAIT;
        return result;
    }

    /**
     * 获取结果状态
     *
     * @return 结果状态
     */
    public TaskNodeStatus getStatus() {
        return status;
    }

    /**
     * 获取节点输出
     *
     * @return 节点输出，可能为 null
     */
    public Object getOutput() {
        return output;
    }

    /**
     * 获取附加消息
     *
     * @return 附加消息，可能为 null
     */
    public String getMessage() {
        return message;
    }

    /**
     * 判断是否成功结果
     *
     * @return true 表示成功
     */
    public boolean isSuccess() {
        return status == TaskNodeStatus.SUCCESS;
    }

    /**
     * 输出调试信息
     *
     * @return 调试字符串
     */
    @Override
    public String toString() {
        return "TaskNodeResult{status=" + status + ", output=" + output + ", message=" + message + "}";
    }

    /**
     * 校验结果状态是否有效（引擎内部防御：空结果按失败处理）
     *
     * @return true 表示结果对象可用
     */
    public boolean isValid() {
        return Objects.nonNull(status);
    }
}
