package com.dayu.yiyibushe.infra.ai.connection;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.credential.CredentialManager;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ProviderInfo;
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
 * 响应解析全部交给 AgentScope 模型扩展完成。路由依据模型定义的 protocol：
 * <ul>
 *     <li>DASHSCOPE_NATIVE → {@link DashScopeChatModel}（DashScope 原生协议）；</li>
 *     <li>OPENAI_COMPATIBLE（OpenAI / DeepSeek / Kimi / 豆包 / 千问兼容模式等）→
 *     {@link OpenAIChatModel}，baseUrl 与 endpointPath 从模型定义取得。</li>
 * </ul>
 * 注意：万相文生图 / 虚拟试衣等 DashScope 异步任务协议 AgentScope 暂未覆盖，
 * 由 {@link AsyncDashScopeClient} 单独承载，本工厂只负责对话类模型。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class AgentScopeModelFactory {

    @Autowired
    private CredentialManager credentialManager;

    /**
     * 创建 AgentScope 对话模型：密钥缺失时抛出业务异常
     *
     * @param definition 模型定义
     * @return AgentScope 模型实例
     */
    public Model createChatModel(ModelDefinition definition) {
        if (Objects.isNull(definition)) {
            throw new BizException(ParamErrorCode.MODEL_DEF_BLANK);
        }
        String secret = credentialManager.requireSecret(definition.getProvider());
        if (isDashScopeNative(definition)) {
            return buildDashScopeModel(definition, secret);
        }
        return buildOpenAiCompatibleModel(definition, secret);
    }

    /**
     * 判断是否走 DashScope 原生协议：优先看 protocol，protocol 缺失时按厂商兜底。
     *
     * @param definition 模型定义
     * @return true 表示走 DashScope 原生模型
     */
    private boolean isDashScopeNative(ModelDefinition definition) {
        if (StringUtilExt.isNotBlank(definition.getProtocol())) {
            return StringUtilExt.equals(ProviderInfo.Protocol.DASHSCOPE_NATIVE.name(),
                    definition.getProtocol());
        }
        return StringUtilExt.equals(ProviderInfo.DASHSCOPE.getCode(), definition.getProvider());
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
