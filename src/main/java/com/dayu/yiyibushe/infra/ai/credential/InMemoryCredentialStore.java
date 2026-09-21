package com.dayu.yiyibushe.infra.ai.credential;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import jakarta.annotation.PostConstruct;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 凭证内存存储实现，启动时从环境变量加载各厂商凭证，支持运行时动态增删改。
 * <p>
 * 环境变量约定（厂商名大写）：
 * <ul>
 *   <li>{PROVIDER}_APP_KEY：应用标识（可选）</li>
 *   <li>{PROVIDER}_APP_SECRET：应用密钥</li>
 *   <li>{PROVIDER}_API_KEY：密钥的旧命名，作为兜底</li>
 * </ul>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Component
public class InMemoryCredentialStore implements CredentialStore {

    private static final Logger log = LogUtilExt.getLogger(InMemoryCredentialStore.class);

    /** 支持的厂商列表（未来可由 MySQL/Redis 加载） */
    private static final List<String> SUPPORTED_PROVIDERS = List.of(
            "dashscope", "openai", "deepseek", "claude", "gemini", "qwen", "doubao", "moonshot");

    /** 凭证存储：provider -> credential */
    private final Map<String, ApiCredential> credentialStore = new ConcurrentHashMap<>();

    /**
     * 启动时从环境变量加载凭证
     */
    @PostConstruct
    public void init() {
        for (String provider : SUPPORTED_PROVIDERS) {
            String upper = provider.toUpperCase();
            String appKey = System.getenv(upper + "_APP_KEY");
            String appSecret = System.getenv(upper + "_APP_SECRET");
            if (StringUtilExt.isBlank(appSecret)) {
                appSecret = System.getenv(upper + "_API_KEY");
            }
            if (StringUtilExt.isNotBlank(appSecret)) {
                credentialStore.put(provider, new ApiCredential(appKey, appSecret.trim()));
                LogUtilExt.info(log, "[CredentialStore] 已加载厂商凭证: {0}", provider);
            }
        }
    }

    /**
     * 查询指定厂商的凭证
     *
     * @param provider
     *     厂商名
     * @return 凭证，未配置时返回 null
     */
    @Override
    public ApiCredential getCredential(String provider) {
        return credentialStore.get(provider);
    }

    /**
     * 新增或更新凭证
     *
     * @param provider
     *     厂商名
     * @param credential
     *     凭证
     */
    @Override
    public void putCredential(String provider, ApiCredential credential) {
        if (StringUtilExt.isBlank(provider) || credential == null) {
            return;
        }
        credentialStore.put(provider, credential);
    }

    /**
     * 删除指定厂商的凭证
     *
     * @param provider
     *     厂商名
     */
    @Override
    public void removeCredential(String provider) {
        credentialStore.remove(provider);
    }

    /**
     * 列出已配置凭证的全部厂商
     *
     * @return 厂商名列表
     */
    @Override
    public List<String> listProviders() {
        return new ArrayList<>(credentialStore.keySet());
    }

    /**
     * 判断指定厂商是否已配置凭证
     *
     * @param provider
     *     厂商名
     * @return true 表示已配置
     */
    @Override
    public boolean hasCredential(String provider) {
        return credentialStore.containsKey(provider);
    }
}