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
    private List<String> imageUrls;

    /** 任务 ID（异步任务模型） */
    private String taskId;

    /** 任务状态：PENDING / RUNNING / SUCCEEDED / FAILED */
    private String status;

    /** 错误信息（失败时） */
    private String errorMessage;

    /** 厂商原始响应（调试用，业务层不应依赖） */
    private String rawResponse;

    /**
     * 无参构造器
     */
    public AiResponse() {
        this.imageUrls = new ArrayList<>();
    }

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
     * Getter method for property <tt>success</tt>.
     *
     * @return property value of success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     *
     * @param success
     *     value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Getter method for property <tt>text</tt>.
     *
     * @return property value of text
     */
    public String getText() {
        return text;
    }

    /**
     * Setter method for property <tt>text</tt>.
     *
     * @param text
     *     value to be assigned to property text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Getter method for property <tt>imageUrls</tt>.
     *
     * @return property value of imageUrls
     */
    public List<String> getImageUrls() {
        return imageUrls;
    }

    /**
     * Setter method for property <tt>imageUrls</tt>.
     *
     * @param imageUrls
     *     value to be assigned to property imageUrls
     */
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    /**
     * Getter method for property <tt>taskId</tt>.
     *
     * @return property value of taskId
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Setter method for property <tt>taskId</tt>.
     *
     * @param taskId
     *     value to be assigned to property taskId
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * Getter method for property <tt>status</tt>.
     *
     * @return property value of status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     *
     * @param status
     *     value to be assigned to property status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter method for property <tt>errorMessage</tt>.
     *
     * @return property value of errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Setter method for property <tt>errorMessage</tt>.
     *
     * @param errorMessage
     *     value to be assigned to property errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Getter method for property <tt>rawResponse</tt>.
     *
     * @return property value of rawResponse
     */
    public String getRawResponse() {
        return rawResponse;
    }

    /**
     * Setter method for property <tt>rawResponse</tt>.
     *
     * @param rawResponse
     *     value to be assigned to property rawResponse
     */
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}
