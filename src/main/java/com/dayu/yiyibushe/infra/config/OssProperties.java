package com.dayu.yiyibushe.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置属性（对应 application.properties 中 aliyun.oss.* 前缀）。
 *
 * @author Witty·Kid Fisher
 */
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /** OSS 接入点 */
    private String endpoint;

    /** 地域 */
    private String region;

    /** 访问密钥 ID */
    private String accessKeyId;

    /** 访问密钥 */
    private String accessKeySecret;

    /** Bucket 名称 */
    private String bucketName;

    /** 资源在 Bucket 中的路径前缀 */
    private String assetPrefix = "assets/";

    /**
     * 获取接入点
     *
     * @return 接入点
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * 设置接入点
     *
     * @param endpoint
     *     接入点
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 获取地域
     *
     * @return 地域
     */
    public String getRegion() {
        return region;
    }

    /**
     * 设置地域
     *
     * @param region
     *     地域
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 获取访问密钥 ID
     *
     * @return 访问密钥 ID
     */
    public String getAccessKeyId() {
        return accessKeyId;
    }

    /**
     * 设置访问密钥 ID
     *
     * @param accessKeyId
     *     访问密钥 ID
     */
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /**
     * 获取访问密钥
     *
     * @return 访问密钥
     */
    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    /**
     * 设置访问密钥
     *
     * @param accessKeySecret
     *     访问密钥
     */
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    /**
     * 获取 Bucket 名称
     *
     * @return Bucket 名称
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置 Bucket 名称
     *
     * @param bucketName
     *     Bucket 名称
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 获取资源路径前缀
     *
     * @return 路径前缀
     */
    public String getAssetPrefix() {
        return assetPrefix;
    }

    /**
     * 设置资源路径前缀
     *
     * @param assetPrefix
     *     路径前缀
     */
    public void setAssetPrefix(String assetPrefix) {
        this.assetPrefix = assetPrefix;
    }
}
