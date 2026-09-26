package com.dayu.yiyibushe.web.dto;

/**
 * 新增提示词请求。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class PromptCreateRequest {

    /** 提示词编码（kebab-case，唯一） */
    private String promptCode;

    /** 展示名称 */
    private String promptName;

    /** 分类 */
    private String category;

    /** 提示词内容（支持 {0} 编号占位符） */
    private String content;

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
     *                 分类
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
     *                提示词内容
     */
    public void setContent(String content) {
        this.content = content;
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
     *               备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
