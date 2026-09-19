package com.dayu.yiyibushe.infra.ai.mcp;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 客户端占位骨架：统一管理外部 MCP Server 的连接与工具发现。
 * <p>
 * 当前版本仅保留「注册服务端信息 / 查询已注册服务端」的内存骨架，
 * 不发起任何真实网络连接，保证无外部环境也能启动。
 * 后续接入 MCP SDK 时，在本类补 listTools / callTool 等协议方法即可，
 * 业务层（app）无需感知协议细节。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class McpClient {

    /** key 为服务端名称 */
    private final Map<String, McpServerInfo> serverMap = new ConcurrentHashMap<>();

    /**
     * 注册一个 MCP 服务端
     *
     * @param serverInfo
     *     服务端连接信息
     */
    public void registerServer(McpServerInfo serverInfo) {
        if (Objects.isNull(serverInfo) || Objects.isNull(serverInfo.serverName())) {
            return;
        }
        serverMap.put(serverInfo.serverName(), serverInfo);
    }

    /**
     * 查询全部已注册的 MCP 服务端
     *
     * @return 不可变服务端集合
     */
    public Collection<McpServerInfo> listServers() {
        return Collections.unmodifiableCollection(serverMap.values());
    }
}
