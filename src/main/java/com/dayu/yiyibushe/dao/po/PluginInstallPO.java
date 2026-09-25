package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 插件安装实例持久化对象（PO），与 ai_plugin_install 表一条记录对应。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class PluginInstallPO implements Serializable {

    private static final long serialVersionUID = 6006006006006006006L;

    /** 物理主键 */
    private Long id;

    /** 业务ID */
    private Long installId;

    /** 指向具体版本 ai_plugin.plugin_id */
    private Long pluginId;

    /** 插件编码（冗余） */
    private String pluginCode;

    /** 所安装版本 */
    private String version;

    /** 安装范围：WORKSPACE/USER */
    private String scopeType;

    /** 空间或用户业务ID */
    private Long scopeId;

    /** 凭证引用（provider 标识），可空 */
    private String credentialRef;

    /** 安装级配置 JSON 原文 */
    private String configJson;

    /** 安装状态：INSTALLED/UPGRADING/UNINSTALLED/FAILED */
    private String installStatus;

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
    public Long getInstallId() {
        return installId;
    }

    /**
     * 设置业务ID
     *
     * @param installId
                        业务ID
     */
    public void setInstallId(Long installId) {
        this.installId = installId;
    }

    /**
     * 获取指向具体版本 ai_plugin.plugin_id
     *
     * @return 指向具体版本 ai_plugin.plugin_id
     */
    public Long getPluginId() {
        return pluginId;
    }

    /**
     * 设置指向具体版本 ai_plugin.plugin_id
     *
     * @param pluginId
                       指向具体版本 ai_plugin.plugin_id
     */
    public void setPluginId(Long pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * 获取插件编码（冗余）
     *
     * @return 插件编码（冗余）
     */
    public String getPluginCode() {
        return pluginCode;
    }

    /**
     * 设置插件编码（冗余）
     *
     * @param pluginCode
                         插件编码（冗余）
     */
    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    /**
     * 获取所安装版本
     *
     * @return 所安装版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置所安装版本
     *
     * @param version
                      所安装版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取安装范围
     *
     * @return 安装范围
     */
    public String getScopeType() {
        return scopeType;
    }

    /**
     * 设置安装范围
     *
     * @param scopeType
                        安装范围
     */
    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    /**
     * 获取空间或用户业务ID
     *
     * @return 空间或用户业务ID
     */
    public Long getScopeId() {
        return scopeId;
    }

    /**
     * 设置空间或用户业务ID
     *
     * @param scopeId
                      空间或用户业务ID
     */
    public void setScopeId(Long scopeId) {
        this.scopeId = scopeId;
    }

    /**
     * 获取凭证引用（provider 标识），可空
     *
     * @return 凭证引用（provider 标识），可空
     */
    public String getCredentialRef() {
        return credentialRef;
    }

    /**
     * 设置凭证引用（provider 标识），可空
     *
     * @param credentialRef
                            凭证引用（provider 标识），可空
     */
    public void setCredentialRef(String credentialRef) {
        this.credentialRef = credentialRef;
    }

    /**
     * 获取安装级配置 JSON 原文
     *
     * @return 安装级配置 JSON 原文
     */
    public String getConfigJson() {
        return configJson;
    }

    /**
     * 设置安装级配置 JSON 原文
     *
     * @param configJson
                         安装级配置 JSON 原文
     */
    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    /**
     * 获取安装状态
     *
     * @return 安装状态
     */
    public String getInstallStatus() {
        return installStatus;
    }

    /**
     * 设置安装状态
     *
     * @param installStatus
                            安装状态
     */
    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
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
