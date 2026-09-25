package com.dayu.yiyibushe.infra.ai.definition;

import java.io.Serializable;

/**
 * 技能领域定义：声明式业务 SOP。
 * <p>
 * description 用于触发路由；instructions 为 SOP 正文（P2 起命中后懒加载）；
 * tools 为工具引用 JSON 原文（[{"toolId":...,"required":true,"order":1}]）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public class SkillDefinition implements Serializable {

    private static final long serialVersionUID = 3303303303303303303L;

    /** 业务ID */
    private Long skillId;

    /** 技能编码 */
    private String skillCode;

    /** 展示名称 */
    private String displayName;

    /** 触发条件描述 */
    private String description;

    /** SOP 正文 */
    private String instructions;

    /** 工具引用 JSON 原文 */
    private String tools;

    /** 触发/不触发 few-shot 注释 */
    private String triggerExamples;

    /** 分类 */
    private String category;

    /** 标签（逗号分隔） */
    private String tags;

    /** 累计调用次数 */
    private Long usageCount;

    /** 开源协议 */
    private String license = "Apache-2.0";

    /** 运行依赖说明 */
    private String compatibility;

    /** 扩展元数据 JSON 原文 */
    private String metadataJson;

    /** 排序号 */
    private Integer sortNo = 0;

    /** 发布状态：DRAFT/PUBLISHED/OFFLINE/DEPRECATED */
    private String publishStatus;

    /** 运行状态：INIT/ACTIVE/FAILED/UNLOADED */
    private String runtimeStatus;

    /** 版本号 */
    private Integer version;

    /** 备注 */
    private String remark;

    /**
     * 获取技能业务ID
     *
     * @return 技能业务ID
     */
    public Long getSkillId() {
        return skillId;
    }

    /**
     * 设置技能业务ID
     *
     * @param skillId
     *                技能业务ID
     */
    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    /**
     * 获取技能编码
     *
     * @return 技能编码
     */
    public String getSkillCode() {
        return skillCode;
    }

    /**
     * 设置技能编码
     *
     * @param skillCode
     *                  技能编码
     */
    public void setSkillCode(String skillCode) {
        this.skillCode = skillCode;
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
     * 获取触发条件描述
     *
     * @return 触发条件描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置触发条件描述
     *
     * @param description
     *                    触发条件描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取 SOP 正文
     *
     * @return SOP 正文
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * 设置 SOP 正文
     *
     * @param instructions
     *                      SOP 正文
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * 获取工具引用 JSON 原文
     *
     * @return 工具引用 JSON 原文
     */
    public String getTools() {
        return tools;
    }

    /**
     * 设置工具引用 JSON 原文
     *
     * @param tools
     *              工具引用 JSON 原文
     */
    public void setTools(String tools) {
        this.tools = tools;
    }

    /**
     * 获取触发/不触发示例
     *
     * @return 触发/不触发示例
     */
    public String getTriggerExamples() {
        return triggerExamples;
    }

    /**
     * 设置触发/不触发示例
     *
     * @param triggerExamples
     *                        触发/不触发示例
     */
    public void setTriggerExamples(String triggerExamples) {
        this.triggerExamples = triggerExamples;
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
     * 获取开源协议
     *
     * @return 开源协议
     */
    public String getLicense() {
        return license;
    }

    /**
     * 设置开源协议
     *
     * @param license
     *                开源协议
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * 获取运行依赖说明
     *
     * @return 运行依赖说明
     */
    public String getCompatibility() {
        return compatibility;
    }

    /**
     * 设置运行依赖说明
     *
     * @param compatibility
     *                      运行依赖说明
     */
    public void setCompatibility(String compatibility) {
        this.compatibility = compatibility;
    }

    /**
     * 获取扩展元数据 JSON 原文
     *
     * @return 扩展元数据 JSON 原文
     */
    public String getMetadataJson() {
        return metadataJson;
    }

    /**
     * 设置扩展元数据 JSON 原文
     *
     * @param metadataJson
     *                     扩展元数据 JSON 原文
     */
    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
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
     *                排序号
     */
    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
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
