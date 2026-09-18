package com.yiyibushe.tryon;

/**
 * 任务查询结果。
 *
 * @param taskId   任务 ID
 * @param status   状态：PENDING / RUNNING / SUCCEEDED / FAILED
 * @param imageUrl 结果图 URL（成功时返回）
 * @param shareUrl 分享 URL（成功时返回）
 * @param message  附加消息
 *
 * @author Witty·Kid Fisher
 */
public record TryOnTaskResult(
        String taskId,
        String status,
        String imageUrl,
        String shareUrl,
        String message) {

    public static TryOnTaskResult pending(String taskId) {
        return new TryOnTaskResult(taskId, "PENDING", null, null, "任务排队中");
    }

    public static TryOnTaskResult running(String taskId) {
        return new TryOnTaskResult(taskId, "RUNNING", null, null, "任务执行中");
    }

    public static TryOnTaskResult succeeded(String taskId, String imageUrl, String shareUrl) {
        return new TryOnTaskResult(taskId, "SUCCEEDED", imageUrl, shareUrl, "成功");
    }

    public static TryOnTaskResult failed(String taskId, String message) {
        return new TryOnTaskResult(taskId, "FAILED", null, null, message);
    }
}
