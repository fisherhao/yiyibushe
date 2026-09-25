package com.dayu.yiyibushe.infra.ai.registry;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.core.ModelType;
import jakarta.annotation.PostConstruct;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型注册表：统一管理所有可用模型的 {@link ModelDefinition}。
 * <p>
 * 当前以 {@code static Map} 占位（内存存储），启动时把默认模型装入运行表；
 * 未来迁移到 MySQL/Redis 时，只需让本类从数据源加载，业务代码无感。
 * 运行期也可通过 {@link #register(ModelDefinition)} 动态扩展。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class ModelRegistry {

    private static final Logger log = LogUtilExt.getLogger(ModelRegistry.class);

    /**
     * 默认模型定义表（静态占位，未来迁移到 MySQL/Redis）。
     */
    private static final Map<String, ModelDefinition> DEFAULT_MODELS = new ConcurrentHashMap<>();

    static {
        // ===== DashScope 原生：万相图片系列（异步） =====
        putDefault(buildImageModel("dashscope-wanx-t2i", "dashscope",
                ModelType.IMAGE_GENERATION, "wanx2.1-t2i-turbo"));
        putDefault(buildImageModel("dashscope-virtualtryon", "dashscope",
                ModelType.IMAGE_EDITING, "wanx-virtualtryon"));

        // ===== OpenAI =====
        putDefault(buildChatModel("openai-gpt4o", "openai", "gpt-4o"));
        putDefault(buildChatModel("openai-gpt4o-mini", "openai", "gpt-4o-mini"));

        // ===== DeepSeek（OpenAI 兼容） =====
        putDefault(buildChatModel("deepseek-chat", "deepseek", "deepseek-chat"));

        // ===== Anthropic Claude =====
        putDefault(buildChatModel("claude-3-5-sonnet", "claude", "claude-3-5-sonnet-20241022"));

        // ===== Google Gemini =====
        putDefault(buildChatModel("gemini-2.0-flash", "gemini", "gemini-2.0-flash"));

        // ===== 通义千问（OpenAI 兼容模式） =====
        // qwen-flash：Qwen3 系列 Flash，思考/非思考融合，1M 上下文，文本模型中最省钱版本
        putDefault(buildChatModel("qwen-flash", "qwen", "qwen-flash"));

        // ===== 字节豆包（火山方舟，OpenAI 兼容） =====
        putDefault(buildChatModel("doubao-pro-32k", "doubao", "doubao-pro-32k"));

        // ===== Moonshot Kimi =====
        putDefault(buildChatModel("moonshot-v1-8k", "moonshot", "moonshot-v1-8k"));
    }

    /** 运行时模型表：code -> definition */
    private final Map<String, ModelDefinition> models = new ConcurrentHashMap<>();

    /**
     * 启动时把静态默认模型装入运行表
     */
    @PostConstruct
    public void init() {
        models.putAll(DEFAULT_MODELS);
        LogUtilExt.info(log, "[ModelRegistry] 已加载 {0} 个默认模型", CollectionUtilExt.getSize(models));
    }

    /**
     * 注册 / 覆盖一个模型
     *
     * @param definition
     *     模型定义
     */
    public void register(ModelDefinition definition) {
        if (Objects.isNull(definition) || StringUtilExt.isBlank(definition.getCode())) {
            throw new BizException(ParamErrorCode.MODEL_DEF_BLANK);
        }
        models.put(definition.getCode(), definition);
    }

    /**
     * 根据编码获取模型定义，不存在抛异常
     *
     * @param code
     *     模型编码
     * @return 模型定义
     */
    public ModelDefinition require(String code) {
        ModelDefinition definition = models.get(code);
        if (Objects.isNull(definition)) {
            throw new BizException(BizErrorCode.MODEL_NOT_REGISTERED);
        }
        return definition;
    }

    /**
     * 根据编码获取模型定义，不存在返回 null
     *
     * @param code
     *     模型编码
     * @return 模型定义或 null
     */
    public ModelDefinition get(String code) {
        return models.get(code);
    }

    /**
     * 列出全部已注册模型
     *
     * @return 模型定义集合
     */
    public Collection<ModelDefinition> listAll() {
        return models.values();
    }

    /**
     * 往静态默认表放模型
     *
     * @param definition
     *     模型定义
     */
    private static void putDefault(ModelDefinition definition) {
        DEFAULT_MODELS.put(definition.getCode(), definition);
    }

    /**
     * 构建异步图片类模型定义
     *
     * @param code
     *     模型编码
     * @param provider
     *     厂商
     * @param modelType
     *     模型类型
     * @param modelName
     *     厂商模型名
     * @return 模型定义
     */
    private static ModelDefinition buildImageModel(String code, String provider,
                                                    ModelType modelType, String modelName) {
        ProviderInfo info = ProviderInfo.fromCode(provider);
        ModelDefinition definition = new ModelDefinition();
        definition.setCode(code);
        definition.setProvider(provider);
        definition.setModelType(modelType);
        definition.setBaseUrl(Objects.requireNonNull(info).getBaseUrl());
        definition.setModelName(modelName);
        definition.setAsync(true);
        return definition;
    }

    /**
     * 构建同步文本对话模型定义
     *
     * @param code
     *     模型编码
     * @param provider
     *     厂商
     * @param modelName
     *     厂商模型名
     * @return 模型定义
     */
    private static ModelDefinition buildChatModel(String code, String provider, String modelName) {
        ProviderInfo info = ProviderInfo.fromCode(provider);
        ModelDefinition definition = new ModelDefinition();
        definition.setCode(code);
        definition.setProvider(provider);
        definition.setModelType(ModelType.TEXT_CHAT);
        definition.setBaseUrl(Objects.requireNonNull(info).getBaseUrl());
        definition.setChatPath(info.getChatPath());
        definition.setModelName(modelName);
        definition.setAsync(false);
        return definition;
    }
}