package com.dayu.yiyibushe.infra.ai.credential;

import com.dayu.yiyibushe.common.util.StringUtilExt;

import java.io.Serializable;

/**
 * 模型访问凭证：appKey + appSecret。
 * <p>
 * appKey 为应用标识（部分厂商可不填），appSecret 为密钥，连接层用它生成 Authorization 头。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ApiCredential implements Serializable {

    private static final long serialVersionUID = 5829103756201938475L;

    /** 应用标识（可选） */
    private final String appKey;

    /** 应用密钥（必填） */
    private final String appSecret;

    /**
     * 全参构造器
     *
     * @param appKey
     *     应用标识
     * @param appSecret
     *     应用密钥
     */
    public ApiCredential(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    /**
     * 获取应用标识
     *
     * @return 应用标识
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * 获取应用密钥
     *
     * @return 应用密钥
     */
    public String getAppSecret() {
        return appSecret;
    }

    /**
     * 是否配置了密钥
     *
     * @return true 表示 appSecret 非空
     */
    public boolean hasSecret() {
        return StringUtilExt.isNotBlank(appSecret);
    }
}
