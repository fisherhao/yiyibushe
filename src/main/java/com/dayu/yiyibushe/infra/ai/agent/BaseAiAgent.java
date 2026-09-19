package com.dayu.yiyibushe.infra.ai.agent;

import com.dayu.yiyibushe.domain.ai.agent.AiAgent;
import com.dayu.yiyibushe.infra.ai.connection.ModelConnection;
import com.dayu.yiyibushe.infra.ai.connection.ModelConnectionFactory;
import com.dayu.yiyibushe.infra.ai.core.AiMessage;
import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.StringUtilExt;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 智能体抽象基类：封装「查模型 -> 建连接 -> 发请求」通用流程。
 * <p>
 * 子类只需提供角色提示词并处理模型回复，不再关心连接细节。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public abstract class BaseAiAgent implements AiAgent {

    /** Agent 名称 */
    protected final String name;

    /** 使用的模型编码（ModelRegistry 中的 code） */
    protected final String modelCode;

    /** 角色系统提示词 */
    protected final String systemPrompt;

    private final ModelRegistry modelRegistry;
    private final ModelConnectionFactory connectionFactory;

    /**
     * 全参构造器
     *
     * @param name
     *     Agent 名称
     * @param modelCode
     *     模型编码
     * @param systemPrompt
     *     角色系统提示词
     * @param modelRegistry
     *     模型注册表
     * @param connectionFactory
     *     连接工厂
     */
    protected BaseAiAgent(String name, String modelCode, String systemPrompt,
                          ModelRegistry modelRegistry, ModelConnectionFactory connectionFactory) {
        this.name = name;
        this.modelCode = modelCode;
        this.systemPrompt = systemPrompt;
        this.modelRegistry = modelRegistry;
        this.connectionFactory = connectionFactory;
    }

    /**
     * 获取 Agent 名称
     *
     * @return Agent 名称
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 以单条用户消息调用大模型，返回回复文本
     *
     * @param userMessage
     *     用户消息
     * @return 模型回复
     */
    protected String callModel(String userMessage) {
        List<AiMessage> messages = new ArrayList<>();
        if (StringUtilExt.isNotBlank(systemPrompt)) {
            messages.add(AiMessage.system(systemPrompt));
        }
        messages.add(AiMessage.user(userMessage));
        AiResponse response = getConnection().execute(buildRequest(messages));
        if (!response.isSuccess()) {
            throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
        }
        return response.getText();
    }

    /**
     * 以自定义消息列表调用大模型
     *
     * @param messages
     *     消息列表
     * @return 模型响应
     */
    protected AiResponse callModel(List<AiMessage> messages) {
        return getConnection().execute(buildRequest(messages));
    }

    /**
     * 获取当前模型的连接
     *
     * @return 模型连接
     */
    protected ModelConnection getConnection() {
        return connectionFactory.create(modelRegistry.require(modelCode));
    }

    /**
     * 构建统一请求
     *
     * @param messages
     *     消息列表
     * @return AI 请求
     */
    private AiRequest buildRequest(List<AiMessage> messages) {
        AiRequest request = new AiRequest();
        request.setModelCode(modelCode);
        request.setMessages(messages);
        return request;
    }
}
