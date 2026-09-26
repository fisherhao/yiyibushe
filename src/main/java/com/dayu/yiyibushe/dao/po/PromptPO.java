package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 提示词持久化对象（PO），与 ai_prompt 表一条记录对应。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class PromptPO implements Serializable {

    private static final long serialVersionUID = 5005005005005005005L;

    /** 物理主键 */
    private Long id;

    /** 提示词编码 */
    private String promptCode;

    /** 展示名称 */
    private String promptName;

    /** 分类 */
    private String category;

    /** 提示词内容 */
    private String content;

    /** 状态：ACTIVE/INACTIVE */
    private String status;

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
     *           物理主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取提示词编码
     *
     * @return 提示词编码
     */
    public String getPromptCode() {
        return promptCode;
    }

    /**
     * 设置提示词编码
     *
     * @param promptCode
     *                   提示词编码
     */
    public void setPromptCode(String promptCode) {
        this.promptCode = promptCode;
    }

    /**
     * 获取展示名称
     *
     * @return 展示名称
     */
    public String getPromptName() {
        return promptName;
    }

    /**
     * 设置展示名称
     *
     * @param promptName
     *                   展示名称
     */
    public void setPromptName(String promptName) {
        this.promptName = promptName;
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
     *                  分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取提示词内容
     *
     * @return 提示词内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置提示词内容
     *
     * @param content
     *                 提示词内容
     */
    public void setContent(String content) {
        this.content = content;
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
     *                状态
     */
    public void setStatus(String status) {
        this.status = status;
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
     *                版本号
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
     *                  创建时间
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
     *                  更新时间
     */
    public void setGmtModify(LocalDateTime gmtModify) {
        this.gmtModify = gmtModify;
    }
}
