package com.dayu.yiyibushe.infra.ai.credential;

import java.util.List;

/**
 * 模型凭证存储接口，统一管理各厂商的 appKey / appSecret。
 * <p>
 * 设计目标：密钥不写入 application.properties，统一由本接口管理。
 * 默认实现 {@link InMemoryCredentialStore} 启动时从环境变量加载；
 * 生产环境可替换为 Redis / MySQL / 配置中心实现，只需实现本接口并注册为 Bean。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
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
