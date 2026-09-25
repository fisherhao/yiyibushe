package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 函数执行体持久化对象（PO），与 ai_function 表一条记录对应。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class FunctionPO implements Serializable {

    private static final long serialVersionUID = 2002002002002002002L;

    /** 物理主键 */
    private Long id;

    /** 业务ID */
    private Long functionId;

    /** 函数编码 */
    private String functionCode;

    /** 函数名称 */
    private String functionName;

    /** 函数说明 */
    private String description;

    /** 执行器类型：NATIVE/HTTP/MCP(预留)/SCRIPT(预留) */
    private String executorType;

    /** 执行配置 JSON 原文 */
    private String executorConfig;

    /** 输出 JSON Schema 原文 */
    private String outputSchema;

    /** 状态：ENABLED/DISABLED */
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
    public Long getFunctionId() {
        return functionId;
    }

    /**
     * 设置业务ID
     *
     * @param functionId
                         业务ID
     */
    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    /**
     * 获取函数编码
     *
     * @return 函数编码
     */
    public String getFunctionCode() {
        return functionCode;
    }

    /**
     * 设置函数编码
     *
     * @param functionCode
                           函数编码
     */
    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 设置函数名称
     *
     * @param functionName
                           函数名称
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * 获取函数说明
     *
     * @return 函数说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置函数说明
     *
     * @param description
                          函数说明
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取执行器类型
     *
     * @return 执行器类型
     */
    public String getExecutorType() {
        return executorType;
    }

    /**
     * 设置执行器类型
     *
     * @param executorType
                           执行器类型
     */
    public void setExecutorType(String executorType) {
        this.executorType = executorType;
    }

    /**
     * 获取执行配置 JSON 原文
     *
     * @return 执行配置 JSON 原文
     */
    public String getExecutorConfig() {
        return executorConfig;
    }

    /**
     * 设置执行配置 JSON 原文
     *
     * @param executorConfig
                             执行配置 JSON 原文
     */
    public void setExecutorConfig(String executorConfig) {
        this.executorConfig = executorConfig;
    }

    /**
     * 获取输出 JSON Schema 原文
     *
     * @return 输出 JSON Schema 原文
     */
    public String getOutputSchema() {
        return outputSchema;
    }

    /**
     * 设置输出 JSON Schema 原文
     *
     * @param outputSchema
                           输出 JSON Schema 原文
     */
    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
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
