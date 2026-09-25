package com.dayu.yiyibushe.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * HTTP 客户端统一配置：三个技能工具（定位 / 天气 / 新闻）共享一个 {@link RestClient}。
 * <p>
 * 基于 JDK 原生 HttpURLConnection，连接与读取各 8 秒超时，默认带浏览器 UA。
 * 原先每个工具各自构建客户端的逻辑全部收敛到这里。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Configuration
public class RestClientConfig {

    /** 默认浏览器 UA（新闻源要求，天气接口带此头无影响） */
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/126.0.0.0 Safari/537.36";

    /** HTTP 超时时长（秒） */
    private static final long TIMEOUT_SECONDS = 8L;

    /**
     * 共享 RestClient：8 秒超时 + 默认 UA
     *
     * @return RestClient 实例
     */
    @Bean
    public RestClient defaultRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(TIMEOUT_SECONDS).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(TIMEOUT_SECONDS).toMillis());
        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", DEFAULT_USER_AGENT)
                .build();
    }
}
