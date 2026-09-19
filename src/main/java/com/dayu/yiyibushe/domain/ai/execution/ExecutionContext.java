package com.dayu.yiyibushe.domain.ai.execution;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 说明：流程执行上下文，在 DAG 节点间共享变量与节点执行结果。
 * <p>
 * 节点可通过 {@link #put(String, Object)} 写入变量，通过 {@link #get(String, Class)} 读取，
 * 也可通过 {@link #getNodeResult(String)} 获取上游节点的执行结果。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ExecutionContext {

    /** 共享变量 */
    private final Map<String, Object> variables = new ConcurrentHashMap<>();

    /** 节点执行结果：nodeId -> result */
    private final Map<String, ExecutionResult> nodeResults = new ConcurrentHashMap<>();

    /**
     * 写入共享变量
     *
     * @param key
     *     变量名
     * @param value
     *     变量值
     */
    public void put(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 读取共享变量并转换类型
     *
     * @param key
     *     变量名
     * @param type
     *     目标类型
     * @param <T>
     *     类型参数
     * @return 变量值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = variables.get(key);
        if (Objects.isNull(value)) {
            return null;
        }
        return (T) value;
    }

    /**
     * 读取共享变量（原始 Object）
     *
     * @param key
     *     变量名
     * @return 变量值
     */
    public Object get(String key) {
        return variables.get(key);
    }

    /**
     * 记录节点执行结果
     *
     * @param nodeId
     *     节点 ID
     * @param result
     *     执行结果
     */
    public void setNodeResult(String nodeId, ExecutionResult result) {
        nodeResults.put(nodeId, result);
    }

    /**
     * 获取节点执行结果
     *
     * @param nodeId
     *     节点 ID
     * @return 执行结果，不存在返回 null
     */
    public ExecutionResult getNodeResult(String nodeId) {
        return nodeResults.get(nodeId);
    }

    /**
     * 获取所有变量的不可变视图
     *
     * @return 变量 Map
     */
    public Map<String, Object> getVariables() {
        return variables;
    }
}
