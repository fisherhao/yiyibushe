package com.dayu.yiyibushe.infra.ai.credential;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 凭证管理器，对 {@link CredentialStore} 做一层封装，提供校验与统一异常。
 * <p>
 * 连接层统一通过本管理器获取凭证，不直接操作 Store，
 * 便于后续扩展加解密、用量审计等横切能力。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class CredentialManager {

    @Autowired
    private CredentialStore credentialStore;

    /**
     * 获取完整凭证，不存在或密钥为空则抛业务异常
     *
     * @param provider
     *     厂商标识
     * @return 凭证
     */
    public ApiCredential requireCredential(String provider) {
        if (StringUtilExt.isBlank(provider)) {
            throw new BizException(ParamErrorCode.PROVIDER_CODE_BLANK);
        }
        ApiCredential credential = credentialStore.getCredential(provider);
        if (Objects.isNull(credential) || !credential.hasSecret()) {
            throw new BizException(BizErrorCode.PROVIDER_CREDENTIAL_MISSING);
        }
        return credential;
    }

    /**
     * 只获取密钥（连接层鉴权最常用）
     *
     * @param provider
     *     厂商标识
     * @return 密钥
     */
    public String requireSecret(String provider) {
        return requireCredential(provider).getAppSecret();
    }

    /**
     * 获取凭证，不存在返回 null
     *
     * @param provider
     *     厂商标识
     * @return 凭证或 null
     */
    public ApiCredential getCredential(String provider) {
        return credentialStore.getCredential(provider);
    }

    /**
     * 保存 / 更新凭证
     *
     * @param provider
     *     厂商标识
     * @param credential
     *     凭证
     */
    public void saveCredential(String provider, ApiCredential credential) {
        credentialStore.putCredential(provider, credential);
    }

    /**
     * 删除凭证
     *
     * @param provider
     *     厂商标识
     */
    public void removeCredential(String provider) {
        credentialStore.removeCredential(provider);
    }

    /**
     * 列出所有已配置厂商
     *
     * @return 厂商标识列表
     */
    public List<String> listProviders() {
        return credentialStore.listProviders();
    }
}
