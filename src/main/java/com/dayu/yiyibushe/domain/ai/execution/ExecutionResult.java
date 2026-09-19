package com.dayu.yiyibushe.domain.ai.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 说明：节点执行结果，封装输出数据、状态与元信息，在 DAG 节点间流转。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ExecutionResult implements Serializable {

    private static final long serialVersionUID = 6293847105623984710L;

    /** 执行是否成功 */
    private boolean success;

    /** 主输出数据（单个对象） */
    private Object output;

    /** 多输出数据（合并/并行场景，key 为来源节点 ID） */
    private Map<String, Object> outputs;

    /** 附加消息 */
    private String message;

    /** 耗时（毫秒） */
    private long elapsedMs;

    /**
     * 无参构造器
     */
    public ExecutionResult() {
        this.outputs = new HashMap<>();
    }

    /**
     * 构造成功结果
     *
     * @param output
     *     输出数据
     * @return 执行结果
     */
    public static ExecutionResult success(Object output) {
        ExecutionResult result = new ExecutionResult();
        result.success = true;
        result.output = output;
        return result;
    }

    /**
     * 构造失败结果
     *
     * @param message
     *     失败消息
     * @return 执行结果
     */
    public static ExecutionResult fail(String message) {
        ExecutionResult result = new ExecutionResult();
        result.success = false;
        result.message = message;
        return result;
    }

    /**
     * 添加命名输出（合并场景使用）
     *
     * @param key
     *     来源标识
     * @param value
     *     输出值
     * @return 本对象
     */
    public ExecutionResult addOutput(String key, Object value) {
        this.outputs.put(key, value);
        return this;
    }

    /**
     * 获取所有输出值列表
     *
     * @return 输出值列表
     */
    public List<Object> outputList() {
        return new ArrayList<>(outputs.values());
    }

    /**
     * Getter method for property <tt>success</tt>.
     *
     * @return property value of success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     *
     * @param success
     *     value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Getter method for property <tt>output</tt>.
     *
     * @return property value of output
     */
    public Object getOutput() {
        return output;
    }

    /**
     * Setter method for property <tt>output</tt>.
     *
     * @param output
     *     value to be assigned to property output
     */
    public void setOutput(Object output) {
        this.output = output;
    }

    /**
     * Getter method for property <tt>outputs</tt>.
     *
     * @return property value of outputs
     */
    public Map<String, Object> getOutputs() {
        return outputs;
    }

    /**
     * Setter method for property <tt>outputs</tt>.
     *
     * @param outputs
     *     value to be assigned to property outputs
     */
    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

    /**
     * Getter method for property <tt>message</tt>.
     *
     * @return property value of message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter method for property <tt>message</tt>.
     *
     * @param message
     *     value to be assigned to property message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter method for property <tt>elapsedMs</tt>.
     *
     * @return property value of elapsedMs
     */
    public long getElapsedMs() {
        return elapsedMs;
    }

    /**
     * Setter method for property <tt>elapsedMs</tt>.
     *
     * @param elapsedMs
     *     value to be assigned to property elapsedMs
     */
    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }
}
