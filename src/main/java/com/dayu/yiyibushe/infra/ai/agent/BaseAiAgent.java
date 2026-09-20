package com.dayu.yiyibushe.infra.ai.agent;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.domain.ai.agent.AiAgent;
import com.dayu.yiyibushe.infra.ai.connection.AgentScopeModelFactory;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.state.AgentStateStore;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * AI 智能体抽象基类：基于 AgentScope {@link ReActAgent} 封装「查模型 -> 建 Agent -> 对话」流程。
 * <p>
 * 子类只需提供角色提示词并处理模型回复，不再关心模型连接细节。
 * 每次 {@link #callModel(String)} 构建一个独立的 ReActAgent 实例（会话 ID 随机），
 * 避免业务多次调用之间记忆串扰；Agent 会话状态经由 AgentScope
 * {@link AgentStateStore} 保存与恢复。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public abstract class BaseAiAgent implements AiAgent {

    /** 单次对话阻塞超时（毫秒） */
    private static final long CHAT_TIMEOUT_MS = 120_000L;

    /** Agent 名称 */
    protected final String name;

    /** 使用的模型编码（ModelRegistry 中的 code） */
    protected final String modelCode;

    /** 角色系统提示词 */
    protected final String systemPrompt;

    private final ModelRegistry modelRegistry;
    private final AgentScopeModelFactory modelFactory;
    private final AgentStateStore stateStore;

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
     * @param modelFactory
     *     AgentScope 模型工厂
     * @param stateStore
     *     AgentScope 会话状态存储
     */
    protected BaseAiAgent(String name, String modelCode, String systemPrompt,
                          ModelRegistry modelRegistry, AgentScopeModelFactory modelFactory,
                          AgentStateStore stateStore) {
        this.name = name;
        this.modelCode = modelCode;
        this.systemPrompt = systemPrompt;
        this.modelRegistry = modelRegistry;
        this.modelFactory = modelFactory;
        this.stateStore = stateStore;
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
        ReActAgent agent = buildAgent();
        Msg reply = agent.call(userMessage, RuntimeContext.empty())
                .block(Duration.ofMillis(CHAT_TIMEOUT_MS));
        if (Objects.isNull(reply) || StringUtilExt.isBlank(reply.getTextContent())) {
            throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
        }
        return reply.getTextContent();
    }

    /**
     * 构建一次性的 AgentScope ReActAgent：绑定模型、角色提示词与状态存储。
     * 会话 ID 每次随机，保证业务多次调用之间状态互不影响。
     *
     * @return ReActAgent 实例
     */
    private ReActAgent buildAgent() {
        ReActAgent.Builder builder = ReActAgent.builder()
                .name(name)
                .model(modelFactory.createChatModel(modelRegistry.require(modelCode)))
                .stateStore(stateStore)
                .defaultSessionId(name + "-" + UUID.randomUUID());
        if (StringUtilExt.isNotBlank(systemPrompt)) {
            builder.sysPrompt(systemPrompt);
        }
        return builder.build();
    }
}
