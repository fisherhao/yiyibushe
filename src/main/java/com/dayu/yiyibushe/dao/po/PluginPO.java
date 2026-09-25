package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 插件持久化对象（PO），与 ai_plugin 表一条记录对应。插件是分发/安装单元。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class PluginPO implements Serializable {

    private static final long serialVersionUID = 5005005005005005005L;

    /** 物理主键 */
    private Long id;

    /** 业务ID */
    private Long pluginId;

    /** 插件编码 */
    private String pluginCode;

    /** 展示名称 */
    private String displayName;

    /** 能力描述（市场展示与安装路由） */
    private String description;

    /** 图标地址 */
    private String icon;

    /** 语义化版本 */
    private String version;

    /** 作者 */
    private String author;

    /** 负责人 */
    private String owner;

    /** 可见范围：PRIVATE/TEAM/PUBLIC */
    private String visibility;

    /** 来源：DB/LOCAL_FILE/MARKET/GIT */
    private String sourceType;

    /** 来源引用 */
    private String sourceRef;

    /** 分类 */
    private String category;

    /** 标签（逗号分隔） */
    private String tags;

    /** 内容清单 JSON 原文 */
    private String items;

    /** 权限声明 JSON 原文 */
    private String permissionConfig;

    /** 是否需要配置凭证：1-是 0-否 */
    private Integer credentialRequired;

    /** 发布状态：DRAFT/PUBLISHED/OFFLINE/DEPRECATED */
    private String publishStatus;

    /** 运行状态：INIT/ACTIVE/FAILED/UNLOADED */
    private String runtimeStatus;

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
    public Long getPluginId() {
        return pluginId;
    }

    /**
     * 设置业务ID
     *
     * @param pluginId
                       业务ID
     */
    public void setPluginId(Long pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * 获取插件编码
     *
     * @return 插件编码
     */
    public String getPluginCode() {
        return pluginCode;
    }

    /**
     * 设置插件编码
     *
     * @param pluginCode
                         插件编码
     */
    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
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
     * 获取能力描述（市场展示与安装路由）
     *
     * @return 能力描述（市场展示与安装路由）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置能力描述（市场展示与安装路由）
     *
     * @param description
                          能力描述（市场展示与安装路由）
     */
    public void setDescription(String description) {
        this.description = description;
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
     * 获取语义化版本
     *
     * @return 语义化版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置语义化版本
     *
     * @param version
                      语义化版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取作者
     *
     * @return 作者
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 设置作者
     *
     * @param author
                     作者
     */
    public void setAuthor(String author) {
        this.author = author;
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
     * 获取来源引用
     *
     * @return 来源引用
     */
    public String getSourceRef() {
        return sourceRef;
    }

    /**
     * 设置来源引用
     *
     * @param sourceRef
                        来源引用
     */
    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
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
     * 获取内容清单 JSON 原文
     *
     * @return 内容清单 JSON 原文
     */
    public String getItems() {
        return items;
    }

    /**
     * 设置内容清单 JSON 原文
     *
     * @param items
                    内容清单 JSON 原文
     */
    public void setItems(String items) {
        this.items = items;
    }

    /**
     * 获取权限声明 JSON 原文
     *
     * @return 权限声明 JSON 原文
     */
    public String getPermissionConfig() {
        return permissionConfig;
    }

    /**
     * 设置权限声明 JSON 原文
     *
     * @param permissionConfig
                               权限声明 JSON 原文
     */
    public void setPermissionConfig(String permissionConfig) {
        this.permissionConfig = permissionConfig;
    }

    /**
     * 获取是否需要配置凭证
     *
     * @return 是否需要配置凭证
     */
    public Integer getCredentialRequired() {
        return credentialRequired;
    }

    /**
     * 设置是否需要配置凭证
     *
     * @param credentialRequired
                                 是否需要配置凭证
     */
    public void setCredentialRequired(Integer credentialRequired) {
        this.credentialRequired = credentialRequired;
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
