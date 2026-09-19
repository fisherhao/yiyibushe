package com.dayu.yiyibushe.infra.ai.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 说明：统一 AI 请求，封装模型编码、消息列表、生成参数与扩展属性。
 * <p>
 * 业务层只需构造本对象，连接层负责把它翻译成具体厂商的 API 请求体。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class AiRequest implements Serializable {

    private static final long serialVersionUID = 5829301847562039184L;

    /** 模型编码（在 ModelRegistry 中注册的唯一标识，如 dashscope-wanx-t2i） */
    private String modelCode;

    /** 消息列表（对话场景） */
    private List<AiMessage> messages;

    /** 文本提示词（文生图 / 图像编辑场景，与 messages 二选一） */
    private String prompt;

    /** 参考图 URL 列表（文生图参考 / 多模态输入） */
    private List<String> referenceImageUrls;

    /** 人物图 URL（虚拟试衣场景） */
    private String personImageUrl;

    /** 服装图 URL（虚拟试衣场景） */
    private String garmentImageUrl;

    /** 生成参数：temperature、maxTokens、size、n 等，连接层按需取用 */
    private Map<String, Object> parameters;

    /**
     * 无参构造器
     */
    public AiRequest() {
        this.messages = new ArrayList<>();
        this.parameters = new HashMap<>();
    }

    /**
     * 添加一条消息
     *
     * @param message
     *     消息
     * @return 本对象（链式调用）
     */
    public AiRequest addMessage(AiMessage message) {
        this.messages.add(message);
        return this;
    }

    /**
     * 设置生成参数
     *
     * @param key
     *     参数名
     * @param value
     *     参数值
     * @return 本对象（链式调用）
     */
    public AiRequest parameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    /**
     * Getter method for property <tt>modelCode</tt>.
     *
     * @return property value of modelCode
     */
    public String getModelCode() {
        return modelCode;
    }

    /**
     * Setter method for property <tt>modelCode</tt>.
     *
     * @param modelCode
     *     value to be assigned to property modelCode
     */
    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    /**
     * Getter method for property <tt>messages</tt>.
     *
     * @return property value of messages
     */
    public List<AiMessage> getMessages() {
        return messages;
    }

    /**
     * Setter method for property <tt>messages</tt>.
     *
     * @param messages
     *     value to be assigned to property messages
     */
    public void setMessages(List<AiMessage> messages) {
        this.messages = messages;
    }

    /**
     * Getter method for property <tt>prompt</tt>.
     *
     * @return property value of prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Setter method for property <tt>prompt</tt>.
     *
     * @param prompt
     *     value to be assigned to property prompt
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * Getter method for property <tt>referenceImageUrls</tt>.
     *
     * @return property value of referenceImageUrls
     */
    public List<String> getReferenceImageUrls() {
        return referenceImageUrls;
    }

    /**
     * Setter method for property <tt>referenceImageUrls</tt>.
     *
     * @param referenceImageUrls
     *     value to be assigned to property referenceImageUrls
     */
    public void setReferenceImageUrls(List<String> referenceImageUrls) {
        this.referenceImageUrls = referenceImageUrls;
    }

    /**
     * Getter method for property <tt>personImageUrl</tt>.
     *
     * @return property value of personImageUrl
     */
    public String getPersonImageUrl() {
        return personImageUrl;
    }

    /**
     * Setter method for property <tt>personImageUrl</tt>.
     *
     * @param personImageUrl
     *     value to be assigned to property personImageUrl
     */
    public void setPersonImageUrl(String personImageUrl) {
        this.personImageUrl = personImageUrl;
    }

    /**
     * Getter method for property <tt>garmentImageUrl</tt>.
     *
     * @return property value of garmentImageUrl
     */
    public String getGarmentImageUrl() {
        return garmentImageUrl;
    }

    /**
     * Setter method for property <tt>garmentImageUrl</tt>.
     *
     * @param garmentImageUrl
     *     value to be assigned to property garmentImageUrl
     */
    public void setGarmentImageUrl(String garmentImageUrl) {
        this.garmentImageUrl = garmentImageUrl;
    }

    /**
     * Getter method for property <tt>parameters</tt>.
     *
     * @return property value of parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Setter method for property <tt>parameters</tt>.
     *
     * @param parameters
     *     value to be assigned to property parameters
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
