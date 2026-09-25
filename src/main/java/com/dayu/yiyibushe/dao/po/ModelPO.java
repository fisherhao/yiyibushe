package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 模型资源持久化对象（PO），与 ai_model 表一条记录对应，只做持久化映射。
 * <p>
 * JSON 列（input_modalities、supported_params、default_params 等）在 PO 层统一以 String
 * 承载，领域层按需用 Jackson 解析，避免类型处理器的嵌套结构不安全问题。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ModelPO implements Serializable {

    private static final long serialVersionUID = 1001001001001001001L;

    /** 物理主键 */
    private Long id;

    /** 业务ID（IdUtil 发号） */
    private Long modelId;

    /** 模型编码，Agent 以 model_code 引用 */
    private String modelCode;

    /** 展示名称 */
    private String displayName;

    /** 厂商标识，对应凭证 provider */
    private String provider;

    /** 模型类型：TEXT_CHAT/IMAGE_GENERATION/IMAGE_EDITING/MULTIMODAL/EMBEDDING */
    private String modelType;

    /** 接入协议：OPENAI_COMPATIBLE/DASHSCOPE_NATIVE/MCP(预留)/CUSTOM(预留) */
    private String protocol;

    /** API 基础地址 */
    private String baseUrl;

    /** 对话接口路径（OPENAI_COMPATIBLE 生效） */
    private String chatPath;

    /** 厂商侧模型名 */
    private String vendorModelName;

    /** 是否异步任务模型：1-是 0-否 */
    private Integer asyncFlag;

    /** 异步轮询间隔（毫秒） */
    private Long pollIntervalMs;

    /** 异步轮询超时（毫秒） */
    private Long pollTimeoutMs;

    /** 上下文窗口（token） */
    private Integer contextWindow;

    /** 最大输出 token */
    private Integer maxOutputTokens;

    /** 输入模态 JSON 原文，如 ["text","image"] */
    private String inputModalities;

    /** 输出模态 JSON 原文 */
    private String outputModalities;

    /** 可调参数结构 JSON 原文 */
    private String supportedParams;

    /** 默认推理参数 JSON 原文 */
    private String defaultParams;

    /** 图标地址 */
    private String icon;

    /** 分类 */
    private String category;

    /** 标签（逗号分隔） */
    private String tags;

    /** 发布状态：DRAFT/PUBLISHED/OFFLINE/DEPRECATED */
    private String publishStatus;

    /** 运行状态：INIT/ACTIVE/FAILED/UNLOADED */
    private String runtimeStatus;

    /** 可见范围：PRIVATE/TEAM/PUBLIC */
    private String visibility;

    /** 来源：DB/LOCAL_FILE/MARKET/GIT */
    private String sourceType;

    /** 来源引用（文件路径/市场编码等） */
    private String sourceRef;

    /** 负责人 */
    private String owner;

    /** 版本号 */
    private Integer version;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime gmtCreate;

    /** 更新时间 */
    private LocalDateTime gmtModify;

    /**
     * 获取物理主键
     *
     * @return 物理主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置物理主键
     *
     * @param id
                 物理主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取业务ID（IdUtil 发号）
     *
     * @return 业务ID（IdUtil 发号）
     */
    public Long getModelId() {
        return modelId;
    }

    /**
     * 设置业务ID（IdUtil 发号）
     *
     * @param modelId
                      业务ID（IdUtil 发号）
     */
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    /**
     * 获取模型编码，Agent 以 model_code 引用
     *
     * @return 模型编码，Agent 以 model_code 引用
     */
    public String getModelCode() {
        return modelCode;
    }

    /**
     * 设置模型编码，Agent 以 model_code 引用
     *
     * @param modelCode
                        模型编码，Agent 以 model_code 引用
     */
    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    /**
     * 获取展示名称
     *
     * @return 展示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置展示名称
     *
     * @param displayName
                          展示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取厂商标识，对应凭证 provider
     *
     * @return 厂商标识，对应凭证 provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置厂商标识，对应凭证 provider
     *
     * @param provider
                       厂商标识，对应凭证 provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 获取模型类型
     *
     * @return 模型类型
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * 设置模型类型
     *
     * @param modelType
                        模型类型
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    /**
     * 获取接入协议
     *
     * @return 接入协议
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 设置接入协议
     *
     * @param protocol
                       接入协议
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * 获取API 基础地址
     *
     * @return API 基础地址
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 设置API 基础地址
     *
     * @param baseUrl
                      API 基础地址
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取对话接口路径（OPENAI_COMPATIBLE 生效）
     *
     * @return 对话接口路径（OPENAI_COMPATIBLE 生效）
     */
    public String getChatPath() {
        return chatPath;
    }

    /**
     * 设置对话接口路径（OPENAI_COMPATIBLE 生效）
     *
     * @param chatPath
                       对话接口路径（OPENAI_COMPATIBLE 生效）
     */
    public void setChatPath(String chatPath) {
        this.chatPath = chatPath;
    }

    /**
     * 获取厂商侧模型名
     *
     * @return 厂商侧模型名
     */
    public String getVendorModelName() {
        return vendorModelName;
    }

    /**
     * 设置厂商侧模型名
     *
     * @param vendorModelName
                              厂商侧模型名
     */
    public void setVendorModelName(String vendorModelName) {
        this.vendorModelName = vendorModelName;
    }

    /**
     * 获取是否异步任务模型
     *
     * @return 是否异步任务模型
     */
    public Integer getAsyncFlag() {
        return asyncFlag;
    }

    /**
     * 设置是否异步任务模型
     *
     * @param asyncFlag
                        是否异步任务模型
     */
    public void setAsyncFlag(Integer asyncFlag) {
        this.asyncFlag = asyncFlag;
    }

    /**
     * 获取异步轮询间隔（毫秒）
     *
     * @return 异步轮询间隔（毫秒）
     */
    public Long getPollIntervalMs() {
        return pollIntervalMs;
    }

    /**
     * 设置异步轮询间隔（毫秒）
     *
     * @param pollIntervalMs
                             异步轮询间隔（毫秒）
     */
    public void setPollIntervalMs(Long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    /**
     * 获取异步轮询超时（毫秒）
     *
     * @return 异步轮询超时（毫秒）
     */
    public Long getPollTimeoutMs() {
        return pollTimeoutMs;
    }

    /**
     * 设置异步轮询超时（毫秒）
     *
     * @param pollTimeoutMs
                            异步轮询超时（毫秒）
     */
    public void setPollTimeoutMs(Long pollTimeoutMs) {
        this.pollTimeoutMs = pollTimeoutMs;
    }

    /**
     * 获取上下文窗口（token）
     *
     * @return 上下文窗口（token）
     */
    public Integer getContextWindow() {
        return contextWindow;
    }

    /**
     * 设置上下文窗口（token）
     *
     * @param contextWindow
                            上下文窗口（token）
     */
    public void setContextWindow(Integer contextWindow) {
        this.contextWindow = contextWindow;
    }

    /**
     * 获取最大输出 token
     *
     * @return 最大输出 token
     */
    public Integer getMaxOutputTokens() {
        return maxOutputTokens;
    }

    /**
     * 设置最大输出 token
     *
     * @param maxOutputTokens
                              最大输出 token
     */
    public void setMaxOutputTokens(Integer maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    /**
     * 获取输入模态 JSON 原文，如 ["text","image"]
     *
     * @return 输入模态 JSON 原文，如 ["text","image"]
     */
    public String getInputModalities() {
        return inputModalities;
    }

    /**
     * 设置输入模态 JSON 原文，如 ["text","image"]
     *
     * @param inputModalities
                              输入模态 JSON 原文，如 ["text","image"]
     */
    public void setInputModalities(String inputModalities) {
        this.inputModalities = inputModalities;
    }

    /**
     * 获取输出模态 JSON 原文
     *
     * @return 输出模态 JSON 原文
     */
    public String getOutputModalities() {
        return outputModalities;
    }

    /**
     * 设置输出模态 JSON 原文
     *
     * @param outputModalities
                               输出模态 JSON 原文
     */
    public void setOutputModalities(String outputModalities) {
        this.outputModalities = outputModalities;
    }

    /**
     * 获取可调参数结构 JSON 原文
     *
     * @return 可调参数结构 JSON 原文
     */
    public String getSupportedParams() {
        return supportedParams;
    }

    /**
     * 设置可调参数结构 JSON 原文
     *
     * @param supportedParams
                              可调参数结构 JSON 原文
     */
    public void setSupportedParams(String supportedParams) {
        this.supportedParams = supportedParams;
    }

    /**
     * 获取默认推理参数 JSON 原文
     *
     * @return 默认推理参数 JSON 原文
     */
    public String getDefaultParams() {
        return defaultParams;
    }

    /**
     * 设置默认推理参数 JSON 原文
     *
     * @param defaultParams
                            默认推理参数 JSON 原文
     */
    public void setDefaultParams(String defaultParams) {
        this.defaultParams = defaultParams;
    }

    /**
     * 获取图标地址
     *
     * @return 图标地址
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置图标地址
     *
     * @param icon
                   图标地址
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取分类
     *
     * @return 分类
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置分类
     *
     * @param category
                       分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取标签（逗号分隔）
     *
     * @return 标签（逗号分隔）
     */
    public String getTags() {
        return tags;
    }

    /**
     * 设置标签（逗号分隔）
     *
     * @param tags
                   标签（逗号分隔）
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 获取发布状态
     *
     * @return 发布状态
     */
    public String getPublishStatus() {
        return publishStatus;
    }

    /**
     * 设置发布状态
     *
     * @param publishStatus
                            发布状态
     */
    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    /**
     * 获取运行状态
     *
     * @return 运行状态
     */
    public String getRuntimeStatus() {
        return runtimeStatus;
    }

    /**
     * 设置运行状态
     *
     * @param runtimeStatus
                            运行状态
     */
    public void setRuntimeStatus(String runtimeStatus) {
        this.runtimeStatus = runtimeStatus;
    }

    /**
     * 获取可见范围
     *
     * @return 可见范围
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * 设置可见范围
     *
     * @param visibility
                         可见范围
     */
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    /**
     * 获取来源
     *
     * @return 来源
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * 设置来源
     *
     * @param sourceType
                         来源
     */
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * 获取来源引用（文件路径/市场编码等）
     *
     * @return 来源引用（文件路径/市场编码等）
     */
    public String getSourceRef() {
        return sourceRef;
    }

    /**
     * 设置来源引用（文件路径/市场编码等）
     *
     * @param sourceRef
                        来源引用（文件路径/市场编码等）
     */
    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    /**
     * 获取负责人
     *
     * @return 负责人
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 设置负责人
     *
     * @param owner
                    负责人
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 获取版本号
     *
     * @return 版本号
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 设置版本号
     *
     * @param version
                      版本号
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     *
     * @param remark
                     备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 设置创建时间
     *
     * @param gmtCreate
                        创建时间
     */
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    public LocalDateTime getGmtModify() {
        return gmtModify;
    }

    /**
     * 设置更新时间
     *
     * @param gmtModify
                        更新时间
     */
    public void setGmtModify(LocalDateTime gmtModify) {
        this.gmtModify = gmtModify;
    }
}
