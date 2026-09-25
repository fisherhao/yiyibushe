package com.dayu.yiyibushe.infra.ai.definition;

import java.io.Serializable;

/**
 * 函数执行体领域定义，描述"用什么执行器、怎么执行"。
 * <p>
 * executor_type 决定执行路径（NATIVE 容器 Bean / HTTP 端点 / MCP 预留），
 * executor_config 为对应执行器的配置 JSON 原文。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public class FunctionDefinition implements Serializable {

    private static final long serialVersionUID = 1101101101101101101L;

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

    /**
     * 获取函数业务ID
     *
     * @return 函数业务ID
     */
    public Long getFunctionId() {
        return functionId;
    }

    /**
     * 设置函数业务ID
     *
     * @param functionId
     *                   函数业务ID
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
     *                     函数编码
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
     *                     函数名称
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
     *                     函数说明
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
     *                     执行器类型
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
     *                       执行配置 JSON 原文
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
     *                     输出 JSON Schema 原文
     */
    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
    }

    /**
     * 获取函数状态
     *
     * @return 函数状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置函数状态
     *
     * @param status
     *                函数状态
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
