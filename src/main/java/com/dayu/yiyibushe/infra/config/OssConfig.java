package com.dayu.yiyibushe.infra.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 阿里云 OSS 客户端 Bean 配置，仅 storage.type=oss 时生效。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Configuration
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "oss")
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    @Autowired
    private OssProperties properties;

    /**
     * 构建 OSS 客户端，容器关闭时自动释放连接池
     *
     * @return OSS 客户端
     */
    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder()
                .build(properties.getEndpoint(),
                        properties.getAccessKeyId(),
                        properties.getAccessKeySecret());
    }
}
