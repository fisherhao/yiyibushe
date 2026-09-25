package com.dayu.yiyibushe.infra.ai.agent;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.domain.ai.agent.AiAgent;
import com.dayu.yiyibushe.infra.ai.connection.ModelConnectionPool;
import com.dayu.yiyibushe.infra.ai.trace.ExecutionTrace;
import com.dayu.yiyibushe.infra.ai.trace.TracePhase;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.tool.Toolkit;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * AI 智能体抽象基类：基于 AgentScope {@link ReActAgent} 封装「取模型连接 -> 建 Agent -> 对话」流程。
 * <p>
 * 无构造器、无 super 链：全部依赖经 {@code @Autowired} 字段注入，子类只需给
 * {@link #name}、{@link #modelCode} 赋值，需要角色提示时覆写 {@link #buildSystemPrompt()}。
 * <p>
 * 模型连接从 {@link ModelConnectionPool} 取：Spring 启动时不连接大模型，
 * 首次发问（或控制平台启用模型）时才建立连接并入池复用。
 * 每次对话构建一个独立 ReActAgent（会话 ID 随机），避免多次调用记忆串扰。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public abstract class BaseAiAgent implements AiAgent {

    /** 单次对话阻塞超时（毫秒） */
    private static final long CHAT_TIMEOUT_MS = 120_000L;

    /** Agent 名称（子类直接赋值） */
    protected String name;

    /** 使用的模型编码（ModelRegistry 中的 code，子类直接赋值） */
    protected String modelCode;

    @Autowired
    private ModelConnectionPool modelConnectionPool;

    @Autowired
    private AgentStateStore stateStore;

    /** 工具箱可选：无工具的 Agent（Plan/Execute/Review）注入为空 */
    @Autowired(required = false)
    private Toolkit toolkit;

    /** 系统提示懒解析缓存（buildSystemPrompt 只执行一次） */
    private volatile String resolvedSystemPrompt;

    /**
     * 子类提供角色系统提示：默认无提示。需要提示的子类直接覆写返回，
     * 结果在首次构建 Agent 时懒加载一次。
     *
     * @return 系统提示文本
     */
    protected String buildSystemPrompt() {
        return "";
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
     * 以单条用户消息调用大模型，返回回复文本（不记录执行轨迹）
     *
     * @param userMessage
     *     用户消息
     * @return 模型回复
     */
    protected String callModel(String userMessage) {
        return callModel(userMessage, null);
    }

    /**
     * 以单条用户消息调用大模型，返回回复文本；全链路步骤写入 {@code trace}。
     * trace 经 RuntimeContext 透传给工具：Agent 装配、模型调用、工具执行全程可追踪。
     *
     * @param userMessage
     *     用户消息
     * @param trace
     *     全链路执行轨迹（null 时不埋点）
     * @return 模型回复
     */
    protected String callModel(String userMessage, ExecutionTrace trace) {
        long startMillis = System.currentTimeMillis();

        ReActAgent agent = buildAgent(trace);

        // RuntimeContext：按类型携带 trace，工具执行时（任意线程）可直接取回
        RuntimeContext runtimeContext = RuntimeContext.builder().build();
        if (Objects.nonNull(trace)) {
            runtimeContext.put(ExecutionTrace.class, trace);
            trace.step(TracePhase.AGENT, "RuntimeContext 就绪，执行轨迹已按类型注入，随调用链透传给工具");
            trace.step(TracePhase.LLM, "大模型对话开始：用户消息=\"{0}\"，进入 ReAct 推理循环", userMessage);
        }

        Msg reply = agent.call(userMessage, runtimeContext)
                .block(Duration.ofMillis(CHAT_TIMEOUT_MS));
        if (Objects.isNull(reply) || StringUtilExt.isBlank(reply.getTextContent())) {
            throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
        }

        long elapsedMillis = System.currentTimeMillis() - startMillis;
        if (Objects.nonNull(trace)) {
            long toolHitCount = trace.countByPhase(TracePhase.TOOL);
            trace.stepWithDuration(TracePhase.LLM, elapsedMillis,
                    "大模型返回最终回复，ReAct 循环结束；模型共发起工具调用 {0} 次", toolHitCount);
        }
        return reply.getTextContent();
    }

    /**
     * 构建一次性的 AgentScope ReActAgent：绑定模型连接、系统提示与状态存储。
     * 会话 ID 每次随机，保证业务多次调用之间状态互不影响。
     *
     * @param trace
     *     执行轨迹（null 时不埋点）
     * @return ReActAgent 实例
     */
    private ReActAgent buildAgent(ExecutionTrace trace) {
        ReActAgent.Builder builder = ReActAgent.builder()
                .name(name)
                .model(modelConnectionPool.get(modelCode))
                .stateStore(stateStore)
                .defaultSessionId(name + "-" + UUID.randomUUID());
        // 挂上工具箱后，模型可在对话中按用户意图自行选择调用技能（function calling）
        if (Objects.nonNull(toolkit)) {
            builder.toolkit(toolkit);
        }
        String systemPrompt = resolveSystemPrompt();
        if (StringUtilExt.isNotBlank(systemPrompt)) {
            builder.sysPrompt(systemPrompt);
        }
        if (Objects.nonNull(trace)) {
            trace.step(TracePhase.AGENT,
                    "构建一次性 ReActAgent：模型 {0} 连接已从连接池取得，会话ID随机生成", modelCode);
            trace.step(TracePhase.AGENT,
                    "系统提示就绪（角色规则 + 技能 metadata 目录；技能正文不预载，命中时懒加载），工具箱已挂载");
        }
        return builder.build();
    }

    /**
     * 懒解析系统提示：首次调用执行子类 {@link #buildSystemPrompt()}，之后一直复用
     *
     * @return 系统提示文本
     */
    private String resolveSystemPrompt() {
        if (Objects.isNull(resolvedSystemPrompt)) {
            synchronized (this) {
                if (Objects.isNull(resolvedSystemPrompt)) {
                    resolvedSystemPrompt = StringUtilExt.defaultString(buildSystemPrompt());
                }
            }
        }
        return resolvedSystemPrompt;
    }
}
