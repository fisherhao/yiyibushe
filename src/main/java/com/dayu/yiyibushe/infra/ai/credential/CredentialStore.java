package com.dayu.yiyibushe.infra.ai.credential;

import java.util.List;

/**
 * 模型凭证存储接口，统一管理各厂商的 appKey / appSecret。
 * <p>
 * 密钥不写入 application.properties，运行期唯一实现为 {@link DbCredentialStore}，
 * 只从 ai_credential 表读取。环境变量/本地配置中的存量密钥仅在首次启动时由
 * LocalCredentialMigrationSource 一次性迁入数据库。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
public interface CredentialStore {

    /**
     * 获取指定厂商的凭证
     *
     * @param provider
     *     厂商标识（如 dashscope、openai、claude、deepseek）
     * @return 凭证，不存在返回 null
     */
    ApiCredential getCredential(String provider);

    /**
     * 保存 / 更新指定厂商的凭证
     *
     * @param provider
     *     厂商标识
     * @param credential
     *     凭证
     */
    void putCredential(String provider, ApiCredential credential);

    /**
     * 删除指定厂商的凭证
     *
     * @param provider
     *     厂商标识
     */
    void removeCredential(String provider);

    /**
     * 列出所有已配置的厂商标识
     *
     * @return 厂商标识列表
     */
    List<String> listProviders();

    /**
     * 判断指定厂商是否已配置凭证
     *
     * @param provider
     *     厂商标识
     * @return true 已配置
     */
    boolean hasCredential(String provider);
}
