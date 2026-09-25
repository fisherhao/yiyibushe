package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 能力挂载持久化对象（PO），与 ai_target_mount 表一条记录对应。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class TargetMountPO implements Serializable {

    private static final long serialVersionUID = 7007007007007007007L;

    /** 物理主键 */
    private Long id;

    /** 业务ID */
    private Long mountId;

    /** 挂载对象类型：AGENT/FLOW_NODE */
    private String targetType;

    /** 挂载对象业务ID */
    private Long targetId;

    /** ai_plugin_install.install_id */
    private Long installId;

    /** 挂载能力类型：PLUGIN/SKILL/TOOL */
    private String mountType;

    /** plugin_id/skill_id/tool_id */
    private Long mountRefId;

    /** 预设参数 JSON 原文 */
    private String presetConfig;

    /** 排序号 */
    private Integer sortNo;

    /** 状态：ENABLED/DISABLED */
    private String status;

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
    public Long getMountId() {
        return mountId;
    }

    /**
     * 设置业务ID
     *
     * @param mountId
                      业务ID
     */
    public void setMountId(Long mountId) {
        this.mountId = mountId;
    }

    /**
     * 获取挂载对象类型
     *
     * @return 挂载对象类型
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * 设置挂载对象类型
     *
     * @param targetType
                         挂载对象类型
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * 获取挂载对象业务ID
     *
     * @return 挂载对象业务ID
     */
    public Long getTargetId() {
        return targetId;
    }

    /**
     * 设置挂载对象业务ID
     *
     * @param targetId
                       挂载对象业务ID
     */
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    /**
     * 获取ai_plugin_install.install_id
     *
     * @return ai_plugin_install.install_id
     */
    public Long getInstallId() {
        return installId;
    }

    /**
     * 设置ai_plugin_install.install_id
     *
     * @param installId
                        ai_plugin_install.install_id
     */
    public void setInstallId(Long installId) {
        this.installId = installId;
    }

    /**
     * 获取挂载能力类型
     *
     * @return 挂载能力类型
     */
    public String getMountType() {
        return mountType;
    }

    /**
     * 设置挂载能力类型
     *
     * @param mountType
                        挂载能力类型
     */
    public void setMountType(String mountType) {
        this.mountType = mountType;
    }

    /**
     * 获取plugin_id/skill_id/tool_id
     *
     * @return plugin_id/skill_id/tool_id
     */
    public Long getMountRefId() {
        return mountRefId;
    }

    /**
     * 设置plugin_id/skill_id/tool_id
     *
     * @param mountRefId
                         plugin_id/skill_id/tool_id
     */
    public void setMountRefId(Long mountRefId) {
        this.mountRefId = mountRefId;
    }

    /**
     * 获取预设参数 JSON 原文
     *
     * @return 预设参数 JSON 原文
     */
    public String getPresetConfig() {
        return presetConfig;
    }

    /**
     * 设置预设参数 JSON 原文
     *
     * @param presetConfig
                           预设参数 JSON 原文
     */
    public void setPresetConfig(String presetConfig) {
        this.presetConfig = presetConfig;
    }

    /**
     * 获取排序号
     *
     * @return 排序号
     */
    public Integer getSortNo() {
        return sortNo;
    }

    /**
     * 设置排序号
     *
     * @param sortNo
                     排序号
     */
    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status
                     状态
     */
    public void setStatus(String status) {
        this.status = status;
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
