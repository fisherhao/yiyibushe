package com.dayu.yiyibushe.infra.ai.state;

import io.agentscope.core.state.InMemoryAgentStateStore;
import io.agentscope.core.state.JsonFileAgentStateStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgentScope 会话状态存储装配。
 * <p>
 * {@code AgentStateStore} 是 AgentScope 官方的状态持久化接口（save / load / delete / list），
 * ReActAgent 据此跨调用保存与恢复会话记忆、工具状态。本配置默认提供内存实现；
 * 需要进程内持久化时切换为 {@link JsonFileAgentStateStore}（见注释示例），
 * 需要分布式持久化时自研实现 {@code AgentStateStore} 接口注册为 Bean 即可，业务无感。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Configuration
public class AgentStateStoreConfig {

    /**
     * 会话状态存储：内存实现（应用重启即清空）。
     * <p>
     * 本地文件持久化可替换为：
     * <pre>{@code
     * return new JsonFileAgentStateStore(Path.of("data/agent-state"));
     * }</pre>
     *
     * @return AgentScope 状态存储
     */
    @Bean
    public InMemoryAgentStateStore inMemoryAgentStateStore() {
        return new InMemoryAgentStateStore();
    }
}
