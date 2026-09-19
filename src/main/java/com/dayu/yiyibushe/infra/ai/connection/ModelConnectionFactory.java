package com.dayu.yiyibushe.infra.ai.connection;

import com.dayu.yiyibushe.infra.ai.connection.profile.ProtocolProfile;
import com.dayu.yiyibushe.infra.ai.credential.CredentialManager;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ProviderInfo;
import org.springframework.stereotype.Component;

/**
 * 模型连接工厂：根据模型所属厂商的通信协议，创建协议档案驱动的
 * {@link GenericModelConnection}。
 * <p>
 * 本工厂不再为任何厂商写分支：厂商协议是 {@link ProviderInfo} 里的一行数据，
 * 协议行为是 {@link ProtocolProfile} 里的一份档案。新增厂商（Kimi、DeepSeek、
 * 千问、元宝等 OpenAI 兼容厂商）时工厂零改动。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class ModelConnectionFactory {

    /** 凭证管理器 */
    private final CredentialManager credentialManager;

    /**
     * 构造器
     *
     * @param credentialManager
     *     凭证管理器
     */
    public ModelConnectionFactory(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    /**
     * 创建模型连接：解析厂商协议档案并交给通用连接
     *
     * @param model
     *     模型定义
     * @return 通用模型连接
     */
    public ModelConnection create(ModelDefinition model) {
        ProviderInfo providerInfo = ProviderInfo.fromCode(model.getProvider());
        // 未收录厂商按 OpenAI 兼容协议处理（绝大多数新厂商都兼容）
        String protocolCode = providerInfo == null
                ? ProtocolProfile.OPENAI_COMPATIBLE
                : providerInfo.getProtocol().name();
        ProtocolProfile profile = ProtocolProfile.fromCode(protocolCode);
        return new GenericModelConnection(model, credentialManager, profile);
    }
}
