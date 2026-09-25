package com.dayu.yiyibushe.infra.ai.connection;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.credential.CredentialManager;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * AgentScope 模型工厂：按 {@link ModelDefinition} 与厂商凭证构造 AgentScope 的 {@link Model} 实例。
 * <p>
 * 取代原先手写 HTTP 的 GenericModelConnection：对话模型的协议适配、鉴权、请求体装配、
 * 响应解析全部交给 AgentScope 模型扩展完成。路由规则：
 * <ul>
 *     <li>dashscope 厂商 → {@link DashScopeChatModel}（DashScope 原生协议）；</li>
 *     <li>其余厂商（OpenAI / DeepSeek / Kimi / 豆包 / 千问兼容模式等）→
 *     {@link OpenAIChatModel}，baseUrl 与 endpointPath 从模型定义取得。</li>
 * </ul>
 * 注意：万相文生图 / 虚拟试衣等 DashScope 异步任务协议 AgentScope 暂未覆盖，
 * 由 {@link AsyncDashScopeClient} 单独承载，本工厂只负责对话类模型。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class AgentScopeModelFactory {

    /** DashScope 厂商标识（原生协议走 DashScope 专用模型类） */
    private static final String PROVIDER_DASHSCOPE = "dashscope";

    @Autowired
    private CredentialManager credentialManager;

    /**
     * 创建 AgentScope 对话模型：密钥缺失时抛出业务异常
     *
     * @param definition
     *     模型定义
     * @return AgentScope 模型实例
     */
    public Model createChatModel(ModelDefinition definition) {
        Objects.requireNonNull(definition, "definition must not be null");
        String secret = credentialManager.requireSecret(definition.getProvider());
        if (StringUtilExt.equals(PROVIDER_DASHSCOPE, definition.getProvider())) {
            return buildDashScopeModel(definition, secret);
        }
        return buildOpenAiCompatibleModel(definition, secret);
    }

    /**
     * 构建 DashScope 原生对话模型（baseUrl 交由扩展默认值，指向官方端点）
     *
     * @param definition
     *     模型定义
     * @param secret
     *     厂商密钥
     * @return DashScope 对话模型
     */
    private Model buildDashScopeModel(ModelDefinition definition, String secret) {
        return DashScopeChatModel.builder()
                .apiKey(secret)
                .modelName(definition.getModelName())
                .stream(false)
                .build();
    }

    /**
     * 构建 OpenAI 兼容对话模型：baseUrl / endpointPath 来自模型定义，
     * 兼容 DeepSeek（/v1）、千问（/compatible-mode/v1）、豆包（/api/v3）等路径差异
     *
     * @param definition
     *     模型定义
     * @param secret
     *     厂商密钥
     * @return OpenAI 兼容对话模型
     */
    private Model buildOpenAiCompatibleModel(ModelDefinition definition, String secret) {
        return OpenAIChatModel.builder()
                .apiKey(secret)
                .modelName(definition.getModelName())
                .baseUrl(definition.getBaseUrl())
                .endpointPath(definition.getChatPath())
                .stream(false)
                .build();
    }
}
