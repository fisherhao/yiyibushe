package com.dayu.yiyibushe.infra.ai.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 说明：统一 AI 响应，封装文本内容、图片 URL 列表、任务状态与原始响应。
 * <p>
 * 连接层把各厂商返回归一化为本对象，业务层无需关心厂商差异。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class AiResponse implements Serializable {

    private static final long serialVersionUID = 4729103856294710385L;

    /** 是否成功 */
    private boolean success;

    /** 文本内容（对话模型的回复） */
    private String text;

    /** 生成的图片 URL 列表（图片生成 / 图像编辑模型） */
    private List<String> imageUrls = new ArrayList<>();

    /** 任务 ID（异步任务模型） */
    private String taskId;

    /** 任务状态：PENDING / RUNNING / SUCCEEDED / FAILED */
    private String status;

    /** 错误信息（失败时） */
    private String errorMessage;

    /** 厂商原始响应（调试用，业务层不应依赖） */
    private String rawResponse;

    /**
     * 构造成功的文本响应
     *
     * @param text
     *     文本内容
     * @return 响应对象
     */
    public static AiResponse successText(String text) {
        AiResponse resp = new AiResponse();
        resp.success = true;
        resp.text = text;
        return resp;
    }

    /**
     * 构造成功的图片响应
     *
     * @param imageUrls
     *     图片 URL 列表
     * @return 响应对象
     */
    public static AiResponse successImage(List<String> imageUrls) {
        AiResponse resp = new AiResponse();
        resp.success = true;
        resp.imageUrls = new ArrayList<>(imageUrls);
        return resp;
    }

    /**
     * 构造失败响应
     *
     * @param errorMessage
     *     错误信息
     * @return 响应对象
     */
    public static AiResponse fail(String errorMessage) {
        AiResponse resp = new AiResponse();
        resp.success = false;
        resp.errorMessage = errorMessage;
        return resp;
    }

    /**
     * 是否成功
     *
     * @return true 表示成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置是否成功
     *
     * @param success
                      是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取文本内容（对话模型的回复）
     *
     * @return 文本内容（对话模型的回复）
     */
    public String getText() {
        return text;
    }

    /**
     * 设置文本内容（对话模型的回复）
     *
     * @param text
                   文本内容（对话模型的回复）
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 获取生成的图片 URL 列表（图片生成 / 图像编辑模型）
     *
     * @return 生成的图片 URL 列表（图片生成 / 图像编辑模型）
     */
    public List<String> getImageUrls() {
        return imageUrls;
    }

    /**
     * 设置生成的图片 URL 列表（图片生成 / 图像编辑模型）
     *
     * @param imageUrls
                        生成的图片 URL 列表（图片生成 / 图像编辑模型）
     */
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    /**
     * 获取任务 ID（异步任务模型）
     *
     * @return 任务 ID（异步任务模型）
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 设置任务 ID（异步任务模型）
     *
     * @param taskId
                     任务 ID（异步任务模型）
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * 获取任务状态
     *
     * @return 任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置任务状态
     *
     * @param status
                     任务状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取错误信息（失败时）
     *
     * @return 错误信息（失败时）
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 设置错误信息（失败时）
     *
     * @param errorMessage
                           错误信息（失败时）
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 获取厂商原始响应（调试用，业务层不应依赖）
     *
     * @return 厂商原始响应（调试用，业务层不应依赖）
     */
    public String getRawResponse() {
        return rawResponse;
    }

    /**
     * 设置厂商原始响应（调试用，业务层不应依赖）
     *
     * @param rawResponse
                          厂商原始响应（调试用，业务层不应依赖）
     */
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}
