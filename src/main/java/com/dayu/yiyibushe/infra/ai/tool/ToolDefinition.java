package com.dayu.yiyibushe.infra.ai.tool;

/**
 * Function Calling 工具定义：描述一个可被大模型调用的工具元数据。
 * <p>
 * 属于基础设施层技术能力：它不关心业务，只承载「工具叫什么、做什么、
 * 需要哪些参数」的协议描述，真正执行由 app 层的 Skill/Service 完成。
 *
 * @param name
 *     工具名称（大模型返回的 function name）
 * @param description
 *     工具描述（供大模型判断调用时机）
 * @param parametersJson
 *     入参 JSON Schema（function calling 协议要求）
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record ToolDefinition(String name, String description, String parametersJson) {
}
