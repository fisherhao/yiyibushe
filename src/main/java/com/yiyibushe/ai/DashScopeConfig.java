package com.yiyibushe.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * DashScope 调用所需的 RestClient Bean。
 * <p>
 * 所有 Wanx 系列接口都是异步任务模式，因此默认加上 X-DashScope-Async: enable 头。
 *
 * @author Witty·Kid Fisher
 */
@Configuration
@EnableConfigurationProperties(DashScopeProperties.class)
public class DashScopeConfig {

    @Bean
    public RestClient dashScopeRestClient(DashScopeProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("X-DashScope-Async", "enable")
                .build();
    }
}
