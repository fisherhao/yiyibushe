package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 工具持久化对象（PO），与 ai_tool 表一条记录对应。工具是函数的模型视图。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ToolPO implements Serializable {

    private static final long serialVersionUID = 3003003003003003003L;

    /** 物理主键 */
    private Long id;

    /** 业务ID */
    private Long toolId;

    /** 工具编码（模型可见，kebab-case） */
    private String toolCode;

    /** 绑定 ai_function.function_id */
    private Long functionId;

    /** 展示名称 */
    private String displayName;

    /** 调用路由描述，决定模型命中率 */
    private String description;

    /** 入参 JSON Schema 原文（2020-12） */
    private String inputSchema;

    /** 产出声明 JSON 原文 */
    private String provides;

    /** 入参依赖声明 JSON 原文 */
    private String requires;

    /** 检索同义词/口语说法（逗号分隔） */
    private String discoverKeywords;

    /** 分类 */
    private String category;

    /** 标签（逗号分隔） */
    private String tags;

    /** 累计调用次数 */
    private Long usageCount;

    /** 是否可并行调用：1-是 0-否 */
    private Integer concurrencySafe;

    /** 是否只读：1-是 0-否 */
    private Integer readOnly;

    /** 是否外部执行（触发人工确认闸门）：1-是 0-否 */
    private Integer externalExecution;

    /** 破坏性操作提示：1-是 0-否 */
    private Integer destructiveHint;

    /** 幂等提示：1-是 0-否 */
    private Integer idempotentHint;

    /** 超时秒数 */
    private Integer timeoutSeconds;

    /** 重试次数 */
    private Integer retryCount;

    /** 结果缓存秒数（0 不缓存） */
    private Integer cacheTtlSeconds;

    /** 图标地址 */
    private String icon;

    /** 发布状态：DRAFT/PUBLISHED/OFFLINE/DEPRECATED */
    private String publishStatus;

    /** 运行状态：INIT/ACTIVE/FAILED/UNLOADED */
    private String runtimeStatus;

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
     * 获取业务ID
     *
     * @return 业务ID
     */
    public Long getToolId() {
        return toolId;
    }

    /**
     * 设置业务ID
     *
     * @param toolId
                     业务ID
     */
    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    /**
     * 获取工具编码（模型可见，kebab-case）
     *
     * @return 工具编码（模型可见，kebab-case）
     */
    public String getToolCode() {
        return toolCode;
    }

    /**
     * 设置工具编码（模型可见，kebab-case）
     *
     * @param toolCode
                       工具编码（模型可见，kebab-case）
     */
    public void setToolCode(String toolCode) {
        this.toolCode = toolCode;
    }

    /**
     * 获取绑定 ai_function.function_id
     *
     * @return 绑定 ai_function.function_id
     */
    public Long getFunctionId() {
        return functionId;
    }

    /**
     * 设置绑定 ai_function.function_id
     *
     * @param functionId
                         绑定 ai_function.function_id
     */
    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
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
     * 获取调用路由描述，决定模型命中率
     *
     * @return 调用路由描述，决定模型命中率
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置调用路由描述，决定模型命中率
     *
     * @param description
                          调用路由描述，决定模型命中率
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取入参 JSON Schema 原文（2020-12）
     *
     * @return 入参 JSON Schema 原文（2020-12）
     */
    public String getInputSchema() {
        return inputSchema;
    }

    /**
     * 设置入参 JSON Schema 原文（2020-12）
     *
     * @param inputSchema
                          入参 JSON Schema 原文（2020-12）
     */
    public void setInputSchema(String inputSchema) {
        this.inputSchema = inputSchema;
    }

    /**
     * 获取产出声明 JSON 原文
     *
     * @return 产出声明 JSON 原文
     */
    public String getProvides() {
        return provides;
    }

    /**
     * 设置产出声明 JSON 原文
     *
     * @param provides
                       产出声明 JSON 原文
     */
    public void setProvides(String provides) {
        this.provides = provides;
    }

    /**
     * 获取入参依赖声明 JSON 原文
     *
     * @return 入参依赖声明 JSON 原文
     */
    public String getRequires() {
        return requires;
    }

    /**
     * 设置入参依赖声明 JSON 原文
     *
     * @param requires
                       入参依赖声明 JSON 原文
     */
    public void setRequires(String requires) {
        this.requires = requires;
    }

    /**
     * 获取检索同义词/口语说法（逗号分隔）
     *
     * @return 检索同义词/口语说法（逗号分隔）
     */
    public String getDiscoverKeywords() {
        return discoverKeywords;
    }

    /**
     * 设置检索同义词/口语说法（逗号分隔）
     *
     * @param discoverKeywords
                               检索同义词/口语说法（逗号分隔）
     */
    public void setDiscoverKeywords(String discoverKeywords) {
        this.discoverKeywords = discoverKeywords;
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
     * 获取累计调用次数
     *
     * @return 累计调用次数
     */
    public Long getUsageCount() {
        return usageCount;
    }

    /**
     * 设置累计调用次数
     *
     * @param usageCount
                         累计调用次数
     */
    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * 获取是否可并行调用
     *
     * @return 是否可并行调用
     */
    public Integer getConcurrencySafe() {
        return concurrencySafe;
    }

    /**
     * 设置是否可并行调用
     *
     * @param concurrencySafe
                              是否可并行调用
     */
    public void setConcurrencySafe(Integer concurrencySafe) {
        this.concurrencySafe = concurrencySafe;
    }

    /**
     * 获取是否只读
     *
     * @return 是否只读
     */
    public Integer getReadOnly() {
        return readOnly;
    }

    /**
     * 设置是否只读
     *
     * @param readOnly
                       是否只读
     */
    public void setReadOnly(Integer readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * 获取是否外部执行（触发人工确认闸门）：1-是 0-否
     *
     * @return 是否外部执行（触发人工确认闸门）：1-是 0-否
     */
    public Integer getExternalExecution() {
        return externalExecution;
    }

    /**
     * 设置是否外部执行（触发人工确认闸门）：1-是 0-否
     *
     * @param externalExecution
                                是否外部执行（触发人工确认闸门）：1-是 0-否
     */
    public void setExternalExecution(Integer externalExecution) {
        this.externalExecution = externalExecution;
    }

    /**
     * 获取破坏性操作提示
     *
     * @return 破坏性操作提示
     */
    public Integer getDestructiveHint() {
        return destructiveHint;
    }

    /**
     * 设置破坏性操作提示
     *
     * @param destructiveHint
                              破坏性操作提示
     */
    public void setDestructiveHint(Integer destructiveHint) {
        this.destructiveHint = destructiveHint;
    }

    /**
     * 获取幂等提示
     *
     * @return 幂等提示
     */
    public Integer getIdempotentHint() {
        return idempotentHint;
    }

    /**
     * 设置幂等提示
     *
     * @param idempotentHint
                             幂等提示
     */
    public void setIdempotentHint(Integer idempotentHint) {
        this.idempotentHint = idempotentHint;
    }

    /**
     * 获取超时秒数
     *
     * @return 超时秒数
     */
    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * 设置超时秒数
     *
     * @param timeoutSeconds
                             超时秒数
     */
    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 获取重试次数
     *
     * @return 重试次数
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 设置重试次数
     *
     * @param retryCount
                         重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 获取结果缓存秒数（0 不缓存）
     *
     * @return 结果缓存秒数（0 不缓存）
     */
    public Integer getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    /**
     * 设置结果缓存秒数（0 不缓存）
     *
     * @param cacheTtlSeconds
                              结果缓存秒数（0 不缓存）
     */
    public void setCacheTtlSeconds(Integer cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
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
