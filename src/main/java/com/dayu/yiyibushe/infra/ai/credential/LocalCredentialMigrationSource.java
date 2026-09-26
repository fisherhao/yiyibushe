package com.dayu.yiyibushe.infra.ai.credential;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.registry.ProviderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 本地凭证一次性迁移源：仅供 AiAssetDataSeeder 在首次启动时，把环境变量/本地配置中的存量密钥
 * 迁入 ai_credential 表。
 * <p>
 * 本类不是 {@link CredentialStore} 实现，不承载运行期凭证，迁移完成后不再被任何业务路径使用。
 * 解析来源与优先级（前者命中即采用，后者兜底）：
 * <ol>
 *   <li>环境变量：{@code {PROVIDER}_APP_KEY}、{@code {PROVIDER}_APP_SECRET}，
 *       旧命名 {@code {PROVIDER}_API_KEY} 兜底；</li>
 *   <li>配置文件：{@code ai.credential.{provider}.app-key/app-secret}；</li>
 *   <li>阿里系厂商（dashscope、qwen）共用：{@code ai.credential.aliyun.app-key/app-secret}。</li>
 * </ol>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class LocalCredentialMigrationSource {

    /** 支持迁移的厂商列表（以 ProviderInfo 枚举为单一事实源） */
    private static final List<String> SUPPORTED_PROVIDERS = List.of(
            ProviderInfo.DASHSCOPE.getCode(), ProviderInfo.OPENAI.getCode(),
            ProviderInfo.DEEPSEEK.getCode(), ProviderInfo.CLAUDE.getCode(),
            ProviderInfo.GEMINI.getCode(), ProviderInfo.QWEN.getCode(),
            ProviderInfo.DOUBAO.getCode(), ProviderInfo.MOONSHOT.getCode());

    /** 阿里系厂商：共用阿里云百炼通用密钥配置 */
    private static final List<String> ALIYUN_FAMILY_PROVIDERS = List.of(
            ProviderInfo.DASHSCOPE.getCode(), ProviderInfo.QWEN.getCode());

    /** properties 配置前缀 */
    private static final String PROP_PREFIX = "ai.credential.";

    /** Spring 环境：读取 application-*.properties 中的凭证配置 */
    @Autowired
    private Environment environment;

    /**
     * 列出本地来源中已配置密钥的全部厂商（按需解析，不缓存）。
     *
     * @return 厂商名列表
     */
    public List<String> listProviders() {
        List<String> providers = new ArrayList<>();
        for (String provider : SUPPORTED_PROVIDERS) {
            if (Objects.nonNull(resolveCredential(provider))) {
                providers.add(provider);
            }
        }
        return providers;
    }

    /**
     * 按厂商解析本地凭证，未配置密钥返回 null。
     *
     * @param provider 厂商名
     * @return 凭证或 null
     */
    public ApiCredential getCredential(String provider) {
        if (StringUtilExt.isBlank(provider)) {
            return null;
        }
        return resolveCredential(provider);
    }

    /**
     * 按优先级解析单个厂商凭证：环境变量 → 厂商专属 properties → 阿里系通用 properties。
     *
     * @param provider 厂商名
     * @return 凭证，全部来源都未配置密钥时返回 null
     */
    private ApiCredential resolveCredential(String provider) {
        String upper = StringUtilExt.upperCase(provider);

        // 1. 环境变量
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

        // 3. 阿里系通用 properties
        if (StringUtilExt.isBlank(appSecret) && ALIYUN_FAMILY_PROVIDERS.contains(provider)) {
            appKey = firstNotBlank(appKey, environment.getProperty(PROP_PREFIX + "aliyun.app-key"));
            appSecret = environment.getProperty(PROP_PREFIX + "aliyun.app-secret");
        }

        if (StringUtilExt.isBlank(appSecret)) {
            return null;
        }
        String trimmedKey = StringUtilExt.isBlank(appKey) ? null : StringUtilExt.trim(appKey);
        return ApiCredential.create(trimmedKey, StringUtilExt.trim(appSecret));
    }

    /**
     * 返回第一个非空白字符串，都为空时返回 null。
     *
     * @param first  第一个值
     * @param second 第二个值
     * @return 第一个非空白值或 null
     */
    private String firstNotBlank(String first, String second) {
        if (StringUtilExt.isNotBlank(first)) {
            return first;
        }
        return StringUtilExt.isNotBlank(second) ? second : null;
    }
}
