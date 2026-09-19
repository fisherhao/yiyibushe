package com.dayu.yiyibushe.infra.ai.registry;

import com.dayu.yiyibushe.infra.ai.core.ModelType;

import java.io.Serializable;

/**
 * 说明：模型定义，描述一个可用模型的元信息（编码、厂商、类型、端点、模型名）。
 * <p>
 * 业务层通过 modelCode 调用模型，连接层通过本定义知道该用哪个厂商的哪个模型、走哪个端点。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ModelDefinition implements Serializable {

    private static final long serialVersionUID = 8392017465028391047L;

    /** 模型唯一编码，如 dashscope-wanx-t2i、openai-gpt4o、claude-3-5-sonnet */
    private String code;

    /** 厂商标识，与 CredentialStore 的 provider 对应，如 dashscope、openai、claude */
    private String provider;

    /** 模型类型 */
    private ModelType modelType;

    /** 厂商 API 基础 URL */
    private String baseUrl;

    /** 厂商侧的模型名，如 wanx2.1-t2i-turbo、gpt-4o、claude-3-5-sonnet-20241022 */
    private String modelName;

    /** OpenAI 兼容协议下的对话接口路径（不同厂商有 /v1、/api/v3、/compatible-mode/v1 等差异） */
    private String chatPath = "/v1/chat/completions";

    /** 是否为异步任务模型（图片生成类通常为异步） */
    private boolean async;

    /** 异步轮询间隔（毫秒），async=true 时生效 */
    private long pollIntervalMs = 3000L;

    /** 异步轮询超时（毫秒），async=true 时生效 */
    private long pollTimeoutMs = 180000L;

    /**
     * 无参构造器
     */
    public ModelDefinition() {
    }

    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter method for property <tt>code</tt>.
     *
     * @param code
     *     value to be assigned to property code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter method for property <tt>provider</tt>.
     *
     * @return property value of provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Setter method for property <tt>provider</tt>.
     *
     * @param provider
     *     value to be assigned to property provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Getter method for property <tt>modelType</tt>.
     *
     * @return property value of modelType
     */
    public ModelType getModelType() {
        return modelType;
    }

    /**
     * Setter method for property <tt>modelType</tt>.
     *
     * @param modelType
     *     value to be assigned to property modelType
     */
    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    /**
     * Getter method for property <tt>baseUrl</tt>.
     *
     * @return property value of baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Setter method for property <tt>baseUrl</tt>.
     *
     * @param baseUrl
     *     value to be assigned to property baseUrl
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Getter method for property <tt>modelName</tt>.
     *
     * @return property value of modelName
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Setter method for property <tt>modelName</tt>.
     *
     * @param modelName
     *     value to be assigned to property modelName
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Getter method for property <tt>chatPath</tt>.
     *
     * @return property value of chatPath
     */
    public String getChatPath() {
        return chatPath;
    }

    /**
     * Setter method for property <tt>chatPath</tt>.
     *
     * @param chatPath
     *     value to be assigned to property chatPath
     */
    public void setChatPath(String chatPath) {
        this.chatPath = chatPath;
    }

    /**
     * Getter method for property <tt>async</tt>.
     *
     * @return property value of async
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Setter method for property <tt>async</tt>.
     *
     * @param async
     *     value to be assigned to property async
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Getter method for property <tt>pollIntervalMs</tt>.
     *
     * @return property value of pollIntervalMs
     */
    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    /**
     * Setter method for property <tt>pollIntervalMs</tt>.
     *
     * @param pollIntervalMs
     *     value to be assigned to property pollIntervalMs
     */
    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    /**
     * Getter method for property <tt>pollTimeoutMs</tt>.
     *
     * @return property value of pollTimeoutMs
     */
    public long getPollTimeoutMs() {
        return pollTimeoutMs;
    }

    /**
     * Setter method for property <tt>pollTimeoutMs</tt>.
     *
     * @param pollTimeoutMs
     *     value to be assigned to property pollTimeoutMs
     */
    public void setPollTimeoutMs(long pollTimeoutMs) {
        this.pollTimeoutMs = pollTimeoutMs;
    }
}
