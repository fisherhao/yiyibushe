package com.dayu.yiyibushe.infra.ai.credential;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import jakarta.annotation.PostConstruct;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 凭证内存存储实现，启动时加载各厂商凭证，支持运行时动态增删改。
 * <p>
 * 加载来源与优先级（前者命中即采用，后者兜底）：
 * <ol>
 *   <li>环境变量：{@code {PROVIDER}_APP_KEY}、{@code {PROVIDER}_APP_SECRET}，
 *       旧命名 {@code {PROVIDER}_API_KEY} 兜底</li>
 *   <li>配置文件（application-*.properties）：{@code ai.credential.{provider}.app-key/app-secret}</li>
 *   <li>阿里系厂商（dashscope、qwen）共用：{@code ai.credential.aliyun.app-key/app-secret}</li>
 * </ol>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Component
public class InMemoryCredentialStore implements CredentialStore {

    private static final Logger log = LogUtilExt.getLogger(InMemoryCredentialStore.class);

    /** 支持的厂商列表（未来可由 MySQL/Redis 加载） */
    private static final List<String> SUPPORTED_PROVIDERS = List.of(
            "dashscope", "openai", "deepseek", "claude", "gemini", "qwen", "doubao", "moonshot");

    /** 阿里系厂商：共用阿里云百炼通用密钥配置（ai.credential.aliyun.*） */
    private static final List<String> ALIYUN_FAMILY_PROVIDERS = List.of("dashscope", "qwen");

    /** properties 配置前缀 */
    private static final String PROP_PREFIX = "ai.credential.";

    /** 凭证存储：provider -> credential */
    private final Map<String, ApiCredential> credentialStore = new ConcurrentHashMap<>();

    /** Spring 环境：读取 application-*.properties 中的凭证配置 */
    @Autowired
    private Environment environment;

    /**
     * 启动时按优先级加载各厂商凭证
     */
    @PostConstruct
    public void init() {
        for (String provider : SUPPORTED_PROVIDERS) {
            ApiCredential credential = resolveCredential(provider);
            if (Objects.nonNull(credential)) {
                credentialStore.put(provider, credential);
                LogUtilExt.info(log, "[CredentialStore] 已加载厂商凭证: {0}", provider);
            }
        }
    }

    /**
     * 按优先级解析单个厂商凭证：环境变量 → 厂商专属 properties → 阿里系通用 properties
     *
     * @param provider
     *     厂商名
     * @return 凭证，全部来源都未配置密钥时返回 null
     */
    private ApiCredential resolveCredential(String provider) {
        String upper = provider.toUpperCase();

        // 1. 环境变量（原有约定）
        String appKey = System.getenv(upper + "_APP_KEY");
        String appSecret = System.getenv(upper + "_APP_SECRET");
        if (StringUtilExt.isBlank(appSecret)) {
            appSecret = System.getenv(upper + "_API_KEY");
        }

        // 2. 厂商专属 properties
        if (StringUtilExt.isBlank(appSecret)) {
            appKey = firstNotBlank(appKey, environment.getProperty(PROP_PREFIX + provider + ".app-key"));
            appSecret = environment.getProperty(PROP_PREFIX + provider + ".app-secret");
        }

        // 3. 阿里系通用 properties（dashscope / qwen 共用一个百炼密钥）
        if (StringUtilExt.isBlank(appSecret) && ALIYUN_FAMILY_PROVIDERS.contains(provider)) {
            appKey = firstNotBlank(appKey, environment.getProperty(PROP_PREFIX + "aliyun.app-key"));
            appSecret = environment.getProperty(PROP_PREFIX + "aliyun.app-secret");
        }

        if (StringUtilExt.isBlank(appSecret)) {
            return null;
        }
        String trimmedKey = StringUtilExt.isBlank(appKey) ? null : appKey.trim();
        return new ApiCredential(trimmedKey, appSecret.trim());
    }

    /**
     * 返回第一个非空白字符串，都为空时返回 null
     *
     * @param first
     *     第一个值
     * @param second
     *     第二个值
     * @return 第一个非空白值或 null
     */
    private String firstNotBlank(String first, String second) {
        if (StringUtilExt.isNotBlank(first)) {
            return first;
        }
        return StringUtilExt.isNotBlank(second) ? second : null;
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
