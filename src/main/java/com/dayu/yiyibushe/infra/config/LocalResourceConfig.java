package com.dayu.yiyibushe.infra.config;

import com.dayu.yiyibushe.infra.resource.LocalResourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 本地临时资源配置入口，启用 {@link LocalResourceProperties}。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Configuration
@EnableConfigurationProperties(LocalResourceProperties.class)
public class LocalResourceConfig {
}
