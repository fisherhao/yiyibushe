package com.dayu.yiyibushe.infra.ai.registry;

import com.dayu.yiyibushe.common.util.StringUtilExt;

/**
 * 厂商信息：集中维护各模型厂商的默认端点、对话路径与通信协议。
 * <p>
 * 业务侧做「免注册直连」时，只需提供厂商名 + appKey/appSecret + 模型名，
 * 由本枚举补齐端点信息。未来厂商增多（7-8 个甚至上百个模型），
 * 可将本枚举数据迁移到 MySQL/Redis，而调用方式不变。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public enum ProviderInfo {

    /** OpenAI 官方 */
    OPENAI("openai", "https://api.openai.com",
            "/v1/chat/completions", Protocol.OPENAI_COMPATIBLE),

    /** DeepSeek（OpenAI 兼容） */
    DEEPSEEK("deepseek", "https://api.deepseek.com",
            "/v1/chat/completions", Protocol.OPENAI_COMPATIBLE),

    /** 阿里 DashScope 原生协议（万相图片系列） */
    DASHSCOPE("dashscope", "https://dashscope.aliyuncs.com",
            null, Protocol.DASHSCOPE_NATIVE),

    /** 通义千问（DashScope OpenAI 兼容模式） */
    QWEN("qwen", "https://dashscope.aliyuncs.com/compatible-mode",
            "/v1/chat/completions", Protocol.OPENAI_COMPATIBLE),

    /** 字节豆包（火山方舟 Ark，OpenAI 兼容） */
    DOUBAO("doubao", "https://ark.cn-beijing.volces.com/api/v3",
            "/chat/completions", Protocol.OPENAI_COMPATIBLE),

    /** Anthropic Claude */
    CLAUDE("claude", "https://api.anthropic.com",
            "/v1/messages", Protocol.CLAUDE),

    /** Google Gemini */
    GEMINI("gemini", "https://generativelanguage.googleapis.com",
            null, Protocol.GEMINI),

    /** Moonshot Kimi（OpenAI 兼容） */
    MOONSHOT("moonshot", "https://api.moonshot.cn",
            "/v1/chat/completions", Protocol.OPENAI_COMPATIBLE);

    /** 厂商标识（凭证存储的 key） */
    private final String code;

    /** 默认基础 URL */
    private final String baseUrl;

    /** 默认对话接口路径 */
    private final String chatPath;

    /** 通信协议（工厂据此选择连接适配器） */
    private final Protocol protocol;

    /**
     * 构造器
     *
     * @param code
     *     厂商标识
     * @param baseUrl
     *     默认基础 URL
     * @param chatPath
     *     默认对话接口路径
     * @param protocol
     *     通信协议
     */
    ProviderInfo(String code, String baseUrl, String chatPath, Protocol protocol) {
        this.code = code;
        this.baseUrl = baseUrl;
        this.chatPath = chatPath;
        this.protocol = protocol;
    }

    /**
     * 连接协议：工厂按协议选适配器，新增协议时扩展本枚举。
     */
    public enum Protocol {
        /** OpenAI 兼容协议（绝大多数国内外厂商支持） */
        OPENAI_COMPATIBLE,
        /** DashScope 原生协议（异步任务 + X-DashScope-Async） */
        DASHSCOPE_NATIVE,
        /** Anthropic Messages 协议 */
        CLAUDE,
        /** Google Gemini 协议 */
        GEMINI
    }

    /**
     * 获取厂商标识
     *
     * @return 厂商名
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取默认基础 URL
     *
     * @return 基础 URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 获取默认对话接口路径
     *
     * @return 接口路径
     */
    public String getChatPath() {
        return chatPath;
    }

    /**
     * 获取通信协议
     *
     * @return 通信协议
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * 按厂商名解析，未收录时按 OpenAI 兼容协议处理（baseUrl 需调用方自带）
     *
     * @param code
     *     厂商名
     * @return 厂商信息，未收录返回 null
     */
    public static ProviderInfo fromCode(String code) {
        for (ProviderInfo info : values()) {
            if (StringUtilExt.equalsIgnoreCase(info.code, code)) {
                return info;
            }
        }
        return null;
    }
}
