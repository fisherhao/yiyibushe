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
     * 是否执行是否成功
     *
     * @return true 表示执行是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置执行是否成功
     *
     * @param success
                      执行是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取主输出数据（单个对象）
     *
     * @return 主输出数据（单个对象）
     */
    public Object getOutput() {
        return output;
    }

    /**
     * 设置主输出数据（单个对象）
     *
     * @param output
                     主输出数据（单个对象）
     */
    public void setOutput(Object output) {
        this.output = output;
    }

    /**
     * 获取多输出数据（合并/并行场景，key 为来源节点 ID）
     *
     * @return 多输出数据（合并/并行场景，key 为来源节点 ID）
     */
    public Map<String, Object> getOutputs() {
        return outputs;
    }

    /**
     * 设置多输出数据（合并/并行场景，key 为来源节点 ID）
     *
     * @param outputs
                      多输出数据（合并/并行场景，key 为来源节点 ID）
     */
    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

    /**
     * 获取附加消息
     *
     * @return 附加消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置附加消息
     *
     * @param message
                      附加消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取耗时（毫秒）
     *
     * @return 耗时（毫秒）
     */
    public long getElapsedMs() {
        return elapsedMs;
    }

    /**
     * 设置耗时（毫秒）
     *
     * @param elapsedMs
                        耗时（毫秒）
     */
    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }
}
