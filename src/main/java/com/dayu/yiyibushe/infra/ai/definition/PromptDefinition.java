package com.dayu.yiyibushe.infra.ai.definition;

/**
 * 提示词领域定义：与 {@code ai_prompt} 一条记录对应，是各 Agent/Tool 消费的提示词载体。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class PromptDefinition {

    /** 提示词编码（kebab-case） */
    private String promptCode;

    /** 展示名称 */
    private String promptName;

    /** 分类：SYSTEM/USER_TEMPLATE/IMAGE_PROMPT/FIXED_REPLY/TOOL_DESC */
    private String category;

    /** 提示词内容（支持 {0} 编号占位符） */
    private String content;

    /** 状态：ACTIVE/INACTIVE */
    private String status;

    /** 版本号 */
    private Integer version;

    /** 备注 */
    private String remark;

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
}
