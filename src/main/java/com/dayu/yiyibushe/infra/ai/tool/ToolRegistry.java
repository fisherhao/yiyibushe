package com.dayu.yiyibushe.infra.ai.tool;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心：集中管理所有可供大模型 function calling 的工具定义。
 * <p>
 * 当前为内存版：应用启动后由各模块通过 {@link #register} 注册，
 * 未来可扩展为从数据库或配置中心加载。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class ToolRegistry {

    /** key 为工具名称 */
    private final Map<String, ToolDefinition> toolMap = new ConcurrentHashMap<>();

    /**
     * 注册工具定义（同名覆盖）
     *
     * @param toolDefinition
     *     工具定义
     */
    public void register(ToolDefinition toolDefinition) {
        if (Objects.isNull(toolDefinition) || Objects.isNull(toolDefinition.name())) {
            return;
        }
        toolMap.put(toolDefinition.name(), toolDefinition);
    }

    /**
     * 按名称获取工具定义
     *
     * @param name
     *     工具名称
     * @return 工具定义；不存在返回 null
     */
    public ToolDefinition get(String name) {
        return toolMap.get(name);
    }

    /**
     * 列出全部工具定义（用于组装大模型请求的 tools 字段）
     *
     * @return 不可变工具集合
     */
    public Collection<ToolDefinition> listAll() {
        return Collections.unmodifiableCollection(toolMap.values());
    }
}
