package com.yiyibushe.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DashScope（通义万相）配置属性。
 *
 * @author Witty·Kid Fisher
 */
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    private String baseUrl;
    private String apiKey;
    private String textToImageModel = "wanx2.1-t2i-turbo";
    private String virtualTryonModel = "wanx-virtualtryon";
    private long pollIntervalMs = 3000L;
    private long pollTimeoutMs = 180000L;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getTextToImageModel() { return textToImageModel; }
    public void setTextToImageModel(String textToImageModel) { this.textToImageModel = textToImageModel; }

    public String getVirtualTryonModel() { return virtualTryonModel; }
    public void setVirtualTryonModel(String virtualTryonModel) { this.virtualTryonModel = virtualTryonModel; }

    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }

    public long getPollTimeoutMs() { return pollTimeoutMs; }
    public void setPollTimeoutMs(long pollTimeoutMs) { this.pollTimeoutMs = pollTimeoutMs; }
}
