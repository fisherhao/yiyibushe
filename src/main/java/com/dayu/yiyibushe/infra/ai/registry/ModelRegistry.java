package com.dayu.yiyibushe.infra.ai.registry;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.dao.mapper.ModelMapper;
import com.dayu.yiyibushe.infra.ai.core.ModelType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型注册表：统一管理所有可用模型的 {@link ModelDefinition}。
 * <p>
 * 模型定义只从数据库（ai_model 表，经 {@link ModelMapper}）加载，运行期不使用内置默认、
 * 不读本地文件：
 * <ul>
 *   <li>启动时尝试全量加载数据库模型进运行缓存，失败仅告警，不阻断启动；</li>
 *   <li>静态默认模型仅作首次启动的种子来源（经 AiAssetDataSeeder 入库），不参与运行期兜底；</li>
 *   <li>种子灌库后由 {@link #reloadFromDatabase()} 刷新缓存；</li>
 *   <li>{@link #require(String)}：缓存中不存在即抛未注册异常。</li>
 * </ul>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Component
public class ModelRegistry {

    private static final Logger log = LogUtilExt.getLogger(ModelRegistry.class);

    /** 发布状态：已发布 */
    private static final String PUBLISH_STATUS_PUBLISHED = "PUBLISHED";

    /** 运行状态：可用 */
    private static final String RUNTIME_STATUS_ACTIVE = "ACTIVE";

    /** 可见范围：公开 */
    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    /** 来源：内置默认（本地文件基线） */
    private static final String SOURCE_TYPE_LOCAL_FILE = "LOCAL_FILE";

    /** 文本模态 JSON */
    private static final String MODALITY_TEXT = "[\"text\"]";

    /** 图片输出模态 JSON */
    private static final String MODALITY_IMAGE = "[\"image\"]";

    /** 文图输入模态 JSON */
    private static final String MODALITY_TEXT_IMAGE = "[\"text\",\"image\"]";

    /** 技能/模型目录引用前缀 */
    private static final String LOCAL_SOURCE_REF = "infra/ai/registry/ModelRegistry.java";

    /**
     * 内置默认模型定义表：数据库缺失时的降级基线，也是首次启动的种子来源。
     */
    private static final Map<String, ModelDefinition> DEFAULT_MODELS = new ConcurrentHashMap<>();

    static {
        // ===== DashScope 原生：万相图片系列（异步） =====
        putDefault(buildImageModel("dashscope-wanx-t2i", "DashScope 万相文生图", "dashscope",
                ModelType.IMAGE_GENERATION, "wanx2.1-t2i-turbo", MODALITY_TEXT, MODALITY_IMAGE));
        putDefault(buildImageModel("dashscope-virtualtryon", "DashScope 虚拟试衣", "dashscope",
                ModelType.IMAGE_EDITING, "wanx-virtualtryon", MODALITY_TEXT_IMAGE, MODALITY_IMAGE));

        // ===== OpenAI =====
        putDefault(buildChatModel("openai-gpt4o", "OpenAI GPT-4o", "openai", "gpt-4o"));
        putDefault(buildChatModel("openai-gpt4o-mini", "OpenAI GPT-4o mini", "openai", "gpt-4o-mini"));

        // ===== DeepSeek（OpenAI 兼容） =====
        putDefault(buildChatModel("deepseek-chat", "DeepSeek Chat", "deepseek", "deepseek-chat"));

        // ===== Anthropic Claude =====
        putDefault(buildChatModel("claude-3-5-sonnet", "Claude 3.5 Sonnet",
                "claude", "claude-3-5-sonnet-20241022"));

        // ===== Google Gemini =====
        putDefault(buildChatModel("gemini-2.0-flash", "Gemini 2.0 Flash",
                "gemini", "gemini-2.0-flash"));

        // ===== 通义千问（OpenAI 兼容模式） =====
        putDefault(buildChatModel("qwen-flash", "通义千问 Qwen Flash", "qwen", "qwen-flash"));

        // ===== 字节豆包（火山引擎，OpenAI 兼容） =====
        putDefault(buildChatModel("doubao-pro-32k", "豆包 Pro 32K", "doubao", "doubao-pro-32k"));

        // ===== Moonshot Kimi =====
        putDefault(buildChatModel("moonshot-v1-8k", "Kimi Moonshot v1 8K",
                "moonshot", "moonshot-v1-8k"));
    }

    /** 运行时模型缓存：code -> definition（数据库模型 + 动态注册） */
    private final Map<String, ModelDefinition> runtimeModels = new ConcurrentHashMap<>();

    @Autowired
    private ModelMapper modelMapper;

    /**
     * 启动时尝试全量加载数据库模型进运行缓存。
     */
    @PostConstruct
    public void init() {
        loadFromDatabase();
    }

    /**
     * 从数据库全量刷新运行缓存（种子灌库后调用）。
     */
    public void reloadFromDatabase() {
        runtimeModels.clear();
        loadFromDatabase();
    }

    /**
     * 从数据库加载全部模型进运行缓存，失败仅告警。
     */
    private void loadFromDatabase() {
        try {
            List<ModelDefinition> dbModels = modelMapper.selectAll();
            for (ModelDefinition dbModel : dbModels) {
                runtimeModels.put(dbModel.getCode(), dbModel);
            }
            LogUtilExt.info(log, "[ModelRegistry] 已从数据库加载 {0} 个模型（种子默认 {1} 个，仅供入库）",
                    runtimeModels.size(), DEFAULT_MODELS.size());
        } catch (Exception e) {
            LogUtilExt.error(log, "[ModelRegistry] 数据库模型加载失败: {0}", e.getMessage(), e);
        }
    }

    /**
     * 注册 / 覆盖一个运行期模型
     *
     * @param definition 模型定义
     */
    public void register(ModelDefinition definition) {
        if (Objects.isNull(definition) || StringUtilExt.isBlank(definition.getCode())) {
            throw new BizException(ParamErrorCode.MODEL_DEF_BLANK);
        }
        runtimeModels.put(definition.getCode(), definition);
    }

    /**
     * 根据编码获取模型定义：只查数据库缓存，不存在抛异常。
     *
     * @param code 模型编码
     * @return 模型定义
     */
    public ModelDefinition require(String code) {
        ModelDefinition definition = get(code);
        if (Objects.isNull(definition)) {
            throw new BizException(BizErrorCode.MODEL_NOT_REGISTERED);
        }
        return definition;
    }

    /**
     * 根据编码获取模型定义，只查数据库缓存，不存在返回 null。
     *
     * @param code 模型编码
     * @return 模型定义或 null
     */
    public ModelDefinition get(String code) {
        if (StringUtilExt.isBlank(code)) {
            return null;
        }
        return runtimeModels.get(code);
    }

    /**
     * 列出全部已从数据库加载的模型。
     *
     * @return 模型定义集合
     */
    public Collection<ModelDefinition> listAll() {
        return new ArrayList<>(runtimeModels.values());
    }

    /**
     * 列出内置默认模型定义，供首次启动种子数据使用。
     *
     * @return 内置默认模型集合
     */
    public Collection<ModelDefinition> listBuiltInDefaults() {
        return DEFAULT_MODELS.values();
    }

    /**
     * 往内置默认表放模型
     *
     * @param definition 模型定义
     */
    private static void putDefault(ModelDefinition definition) {
        DEFAULT_MODELS.put(definition.getCode(), definition);
    }

    /**
     * 填充新表治理字段（默认基线：已发布/可用/公开/本地来源）。
     *
     * @param definition 模型定义
     */
    private static void fillGovernanceFields(ModelDefinition definition) {
        definition.setPublishStatus(PUBLISH_STATUS_PUBLISHED);
        definition.setRuntimeStatus(RUNTIME_STATUS_ACTIVE);
        definition.setVisibility(VISIBILITY_PUBLIC);
        definition.setSourceType(SOURCE_TYPE_LOCAL_FILE);
        definition.setSourceRef(LOCAL_SOURCE_REF);
        definition.setVersion(1);
    }

    /**
     * 构建异步图片类模型定义。
     *
     * @param code            模型编码
     * @param displayName     展示名称
     * @param provider        厂商
     * @param modelType       模型类型
     * @param modelName       厂商模型名
     * @param inputModalities 输入模态 JSON
     * @param outputModalities 输出模态 JSON
     * @return 模型定义
     */
    private static ModelDefinition buildImageModel(String code, String displayName, String provider,
                                                    ModelType modelType, String modelName,
                                                    String inputModalities, String outputModalities) {
        ProviderInfo info = ProviderInfo.fromCode(provider);
        ModelDefinition definition = new ModelDefinition();
        definition.setCode(code);
        definition.setDisplayName(displayName);
        definition.setProvider(provider);
        definition.setModelType(modelType);
        definition.setProtocol(Objects.requireNonNull(info).getProtocol().name());
        definition.setBaseUrl(info.getBaseUrl());
        definition.setChatPath(info.getChatPath());
        definition.setModelName(modelName);
        definition.setAsync(true);
        definition.setInputModalities(inputModalities);
        definition.setOutputModalities(outputModalities);
        fillGovernanceFields(definition);
        return definition;
    }

    /**
     * 构建同步文本对话模型定义。
     *
     * @param code        模型编码
     * @param displayName 展示名称
     * @param provider    厂商
     * @param modelName   厂商模型名
     * @return 模型定义
     */
    private static ModelDefinition buildChatModel(String code, String displayName,
                                                   String provider, String modelName) {
        ProviderInfo info = ProviderInfo.fromCode(provider);
        ModelDefinition definition = new ModelDefinition();
        definition.setCode(code);
        definition.setDisplayName(displayName);
        definition.setProvider(provider);
        definition.setModelType(ModelType.TEXT_CHAT);
        definition.setProtocol(Objects.requireNonNull(info).getProtocol().name());
        definition.setBaseUrl(info.getBaseUrl());
        definition.setChatPath(info.getChatPath());
        definition.setModelName(modelName);
        definition.setAsync(false);
        definition.setInputModalities(MODALITY_TEXT);
        definition.setOutputModalities(MODALITY_TEXT);
        fillGovernanceFields(definition);
        return definition;
    }
}
