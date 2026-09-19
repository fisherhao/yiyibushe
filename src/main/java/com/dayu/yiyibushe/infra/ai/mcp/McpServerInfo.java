package com.dayu.yiyibushe.infra.ai.mcp;

/**
 * MCP（Model Context Protocol）服务端连接信息。
 * <p>
 * 属于基础设施层：描述一个外部 MCP Server 的接入坐标，
 * 与具体业务无关。
 *
 * @param serverName
 *     服务端名称（本地唯一标识）
 * @param endpoint
 *     接入地址（HTTP/SSE/stdio 等，按实际协议填写）
 * @param version
 *     协议或服务版本
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record McpServerInfo(String serverName, String endpoint, String version) {
}
