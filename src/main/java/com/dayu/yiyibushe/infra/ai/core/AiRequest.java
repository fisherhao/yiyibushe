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
    private List<AiMessage> messages = new ArrayList<>();

    /** 文本提示词（文生图 / 图像编辑场景，与 messages 二选一） */
    private String prompt;

    /** 参考图 URL 列表（文生图参考 / 多模态输入） */
    private List<String> referenceImageUrls;

    /** 人物图 URL（虚拟试衣场景） */
    private String personImageUrl;

    /** 服装图 URL（虚拟试衣场景） */
    private String garmentImageUrl;

    /** 生成参数：temperature、maxTokens、size、n 等，连接层按需取用 */
    private Map<String, Object> parameters = new HashMap<>();

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
     * 获取模型编码（在 ModelRegistry 中注册的唯一标识，如 dashscope-wanx-t2i）
     *
     * @return 模型编码（在 ModelRegistry 中注册的唯一标识，如 dashscope-wanx-t2i）
     */
    public String getModelCode() {
        return modelCode;
    }

    /**
     * 设置模型编码（在 ModelRegistry 中注册的唯一标识，如 dashscope-wanx-t2i）
     *
     * @param modelCode
                        模型编码（在 ModelRegistry 中注册的唯一标识，如 dashscope-wanx-t2i）
     */
    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    /**
     * 获取消息列表（对话场景）
     *
     * @return 消息列表（对话场景）
     */
    public List<AiMessage> getMessages() {
        return messages;
    }

    /**
     * 设置消息列表（对话场景）
     *
     * @param messages
                       消息列表（对话场景）
     */
    public void setMessages(List<AiMessage> messages) {
        this.messages = messages;
    }

    /**
     * 获取文本提示词（文生图 / 图像编辑场景，与 messages 二选一）
     *
     * @return 文本提示词（文生图 / 图像编辑场景，与 messages 二选一）
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * 设置文本提示词（文生图 / 图像编辑场景，与 messages 二选一）
     *
     * @param prompt
                     文本提示词（文生图 / 图像编辑场景，与 messages 二选一）
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * 获取参考图 URL 列表（文生图参考 / 多模态输入）
     *
     * @return 参考图 URL 列表（文生图参考 / 多模态输入）
     */
    public List<String> getReferenceImageUrls() {
        return referenceImageUrls;
    }

    /**
     * 设置参考图 URL 列表（文生图参考 / 多模态输入）
     *
     * @param referenceImageUrls
                                 参考图 URL 列表（文生图参考 / 多模态输入）
     */
    public void setReferenceImageUrls(List<String> referenceImageUrls) {
        this.referenceImageUrls = referenceImageUrls;
    }

    /**
     * 获取人物图 URL（虚拟试衣场景）
     *
     * @return 人物图 URL（虚拟试衣场景）
     */
    public String getPersonImageUrl() {
        return personImageUrl;
    }

    /**
     * 设置人物图 URL（虚拟试衣场景）
     *
     * @param personImageUrl
                             人物图 URL（虚拟试衣场景）
     */
    public void setPersonImageUrl(String personImageUrl) {
        this.personImageUrl = personImageUrl;
    }

    /**
     * 获取服装图 URL（虚拟试衣场景）
     *
     * @return 服装图 URL（虚拟试衣场景）
     */
    public String getGarmentImageUrl() {
        return garmentImageUrl;
    }

    /**
     * 设置服装图 URL（虚拟试衣场景）
     *
     * @param garmentImageUrl
                              服装图 URL（虚拟试衣场景）
     */
    public void setGarmentImageUrl(String garmentImageUrl) {
        this.garmentImageUrl = garmentImageUrl;
    }

    /**
     * 获取生成参数
     *
     * @return 生成参数
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * 设置生成参数
     *
     * @param parameters
                         生成参数
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
