package com.dayu.yiyibushe.domain.tryon;

import com.dayu.yiyibushe.common.constant.TaskStatus;

/**
 * 任务查询结果。
 *
 * @param taskId
 *     任务 ID
 * @param status
 *     状态：PENDING / RUNNING / SUCCEEDED / FAILED
 * @param imageUrl
 *     结果图 URL（成功时返回）
 * @param shareUrl
 *     分享 URL（成功时返回）
 * @param message
 *     附加消息
 * @author Witty·Kid Fisher
 */
public record TryOnTaskResult(
        String taskId,
        String status,
        String imageUrl,
        String shareUrl,
        String message) {

    /**
     * 构造排队中的任务结果
     *
     * @param taskId
     *     任务 ID
     * @param message
     *     状态文案（由调用方从提示词库取）
     * @return PENDING 结果
     */
    public static TryOnTaskResult pending(String taskId, String message) {
        return new TryOnTaskResult(taskId, TaskStatus.PENDING, null, null, message);
    }

    /**
     * 构造执行中的任务结果
     *
     * @param taskId
     *     任务 ID
     * @param message
     *     状态文案（由调用方从提示词库取）
     * @return RUNNING 结果
     */
    public static TryOnTaskResult running(String taskId, String message) {
        return new TryOnTaskResult(taskId, TaskStatus.RUNNING, null, null, message);
    }

    /**
     * 构造成功的任务结果
     *
     * @param taskId
     *     任务 ID
     * @param imageUrl
     *     结果图 URL（成功时返回）
     * @param shareUrl
     *     分享 URL（成功时返回）
     * @param message
     *     状态文案（由调用方从提示词库取）
     * @return SUCCEEDED 结果
     */
    public static TryOnTaskResult succeeded(String taskId, String imageUrl, String shareUrl, String message) {
        return new TryOnTaskResult(taskId, TaskStatus.SUCCEEDED, imageUrl, shareUrl, message);
    }

    /**
     * 构造失败的任务结果
     *
     * @param taskId
     *     任务 ID
     * @param message
     *     失败消息
     * @return FAILED 结果
     */
    public static TryOnTaskResult failed(String taskId, String message) {
        return new TryOnTaskResult(taskId, TaskStatus.FAILED, null, null, message);
    }
}
