package com.dayu.yiyibushe.infra.ai.definition;

import java.io.Serializable;

/**
 * 工具领域定义：函数的模型视图，承载工具编码、调用描述、入参 Schema 与治理项。
 * <p>
 * description 与 input_schema 决定模型的调用命中率；治理标志（只读/破坏性/超时/缓存）
 * 供 P3 中间件链消费。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public class ToolDefinition implements Serializable {

    private static final long serialVersionUID = 2202202202202202202L;

    /** 业务ID */
    private Long toolId;

    /** 工具编码（模型可见，kebab-case） */
    private String toolCode;

    /** 绑定函数业务ID */
    private Long functionId;

    /** 展示名称 */
    private String displayName;

    /** 调用路由描述 */
    private String description;

    /** 入参 JSON Schema 原文 */
    private String inputSchema;

    /** 产出声明 JSON 原文 */
    private String provides;

    /** 入参依赖声明 JSON 原文 */
    private String requires;

    /** 检索同义词（逗号分隔） */
    private String discoverKeywords;

    /** 分类 */
    private String category;

    /** 标签（逗号分隔） */
    private String tags;

    /** 累计调用次数 */
    private Long usageCount;

    /** 是否可并行调用 */
    private boolean concurrencySafe = true;

    /** 是否只读 */
    private boolean readOnly = true;

    /** 是否外部执行（人工确认闸门） */
    private boolean externalExecution;

    /** 破坏性操作提示 */
    private boolean destructiveHint;

    /** 幂等提示 */
    private boolean idempotentHint;

    /** 超时秒数 */
    private Integer timeoutSeconds = 8;

    /** 重试次数 */
    private Integer retryCount = 1;

    /** 结果缓存秒数（0 不缓存） */
    private Integer cacheTtlSeconds = 0;

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

    /**
     * 获取工具业务ID
     *
     * @return 工具业务ID
     */
    public Long getToolId() {
        return toolId;
    }

    /**
     * 设置工具业务ID
     *
     * @param toolId
     *               工具业务ID
     */
    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    /**
     * 获取工具编码
     *
     * @return 工具编码
     */
    public String getToolCode() {
        return toolCode;
    }

    /**
     * 设置工具编码
     *
     * @param toolCode
     *                 工具编码
     */
    public void setToolCode(String toolCode) {
        this.toolCode = toolCode;
    }

    /**
     * 获取绑定函数业务ID
     *
     * @return 绑定函数业务ID
     */
    public Long getFunctionId() {
        return functionId;
    }

    /**
     * 设置绑定函数业务ID
     *
     * @param functionId
     *                   绑定函数业务ID
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
     *                    展示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取调用路由描述
     *
     * @return 调用路由描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置调用路由描述
     *
     * @param description
     *                    调用路由描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取入参 JSON Schema 原文
     *
     * @return 入参 JSON Schema 原文
     */
    public String getInputSchema() {
        return inputSchema;
    }

    /**
     * 设置入参 JSON Schema 原文
     *
     * @param inputSchema
     *                    入参 JSON Schema 原文
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
     *                 产出声明 JSON 原文
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
     *                 入参依赖声明 JSON 原文
     */
    public void setRequires(String requires) {
        this.requires = requires;
    }

    /**
     * 获取检索同义词
     *
     * @return 检索同义词
     */
    public String getDiscoverKeywords() {
        return discoverKeywords;
    }

    /**
     * 设置检索同义词
     *
     * @param discoverKeywords
     *                         检索同义词
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
     *                 分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取标签
     *
     * @return 标签
     */
    public String getTags() {
        return tags;
    }

    /**
     * 设置标签
     *
     * @param tags
     *             标签
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
     *                   累计调用次数
     */
    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * 是否可并行调用
     *
     * @return true 表示可并行调用
     */
    public boolean isConcurrencySafe() {
        return concurrencySafe;
    }

    /**
     * 设置是否可并行调用
     *
     * @param concurrencySafe
     *                        是否可并行调用
     */
    public void setConcurrencySafe(boolean concurrencySafe) {
        this.concurrencySafe = concurrencySafe;
    }

    /**
     * 是否只读
     *
     * @return true 表示只读
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * 设置是否只读
     *
     * @param readOnly
     *                 是否只读
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * 是否外部执行（人工确认闸门）
     *
     * @return true 表示外部执行
     */
    public boolean isExternalExecution() {
        return externalExecution;
    }

    /**
     * 设置是否外部执行
     *
     * @param externalExecution
     *                          是否外部执行
     */
    public void setExternalExecution(boolean externalExecution) {
        this.externalExecution = externalExecution;
    }

    /**
     * 是否破坏性操作
     *
     * @return true 表示破坏性操作
     */
    public boolean isDestructiveHint() {
        return destructiveHint;
    }

    /**
     * 设置是否破坏性操作
     *
     * @param destructiveHint
     *                        是否破坏性操作
     */
    public void setDestructiveHint(boolean destructiveHint) {
        this.destructiveHint = destructiveHint;
    }

    /**
     * 是否幂等操作
     *
     * @return true 表示幂等操作
     */
    public boolean isIdempotentHint() {
        return idempotentHint;
    }

    /**
     * 设置是否幂等操作
     *
     * @param idempotentHint
     *                       是否幂等操作
     */
    public void setIdempotentHint(boolean idempotentHint) {
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
     *                       超时秒数
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
     *                   重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 获取结果缓存秒数
     *
     * @return 结果缓存秒数
     */
    public Integer getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    /**
     * 设置结果缓存秒数
     *
     * @param cacheTtlSeconds
     *                        结果缓存秒数
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
     *             图标地址
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
     *                      发布状态
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
     *                      运行状态
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
     *                 版本号
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
     *                备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
