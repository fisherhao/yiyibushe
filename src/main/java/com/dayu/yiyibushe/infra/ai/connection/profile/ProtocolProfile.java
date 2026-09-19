package com.dayu.yiyibushe.infra.ai.connection.profile;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.infra.ai.core.ModelType;

import java.util.List;
import java.util.Map;

/**
 * 通信协议档案：用「配置」描述一个厂商协议长什么样，连接层照单办事，不再为每个厂商写类。
 * <p>
 * 档案声明：鉴权头/查询参数、请求体装配风格、同步路径、异步提交/轮询路径、
 * 响应取值的 JSON 指针。新接入厂商时：
 * <ul>
 *     <li>OpenAI 兼容厂商（DeepSeek、Kimi、千问兼容模式、豆包等）：只需在 {@code ProviderInfo}
 *     加一行厂商配置，协议档案零改动；</li>
 *     <li>遇到全新协议：在本类目录里加一份档案（数据）+ 必要时加一个请求体装配风格，
 *     不需要动连接层代码。</li>
 * </ul>
 *
 * @param code
 *     档案编码（与 {@code ProviderInfo.Protocol} 名称一致）
 * @param async
 *     是否异步任务协议
 * @param authHeaders
 *     鉴权请求头模板列表，值中用 {@link #SECRET_PLACEHOLDER} 占位密钥
 * @param secretQueryParam
 *     密钥查询参数名（如 Gemini 的 key），不使用为 null
 * @param bodyStyle
 *     请求体装配风格
 * @param syncPath
 *     同步对话路径，为 null 时使用模型定义中的 chatPath，支持 {modelName} 占位
 * @param submitPaths
 *     异步提交路径（按模型类型区分），同步协议为空表
 * @param pollPathTemplate
 *     异步轮询路径模板，支持 {taskId} 占位
 * @param contentPointer
 *     同步对话文本结果的 JSON 指针
 * @param taskIdPointer
 *     异步提交响应中任务 ID 的 JSON 指针
 * @param statusPointer
 *     轮询响应中任务状态的 JSON 指针
 * @param imagePointer
 *     轮询成功时结果图 URL 的 JSON 指针
 * @param fallbackImagePointer
 *     结果图兜底指针（如 b64_image）
 * @param messagePointer
 *     失败消息的 JSON 指针
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record ProtocolProfile(
        String code,
        boolean async,
        List<AuthHeader> authHeaders,
        String secretQueryParam,
        RequestBodyStyle bodyStyle,
        String syncPath,
        Map<ModelType, String> submitPaths,
        String pollPathTemplate,
        String contentPointer,
        String taskIdPointer,
        String statusPointer,
        String imagePointer,
        String fallbackImagePointer,
        String messagePointer) {

    // ==================== 协议档案编码（与 ProviderInfo.Protocol 枚举名一致） ====================

    /** OpenAI 兼容协议（DeepSeek、Kimi、豆包、千问兼容模式等） */
    public static final String OPENAI_COMPATIBLE = "OPENAI_COMPATIBLE";

    /** 阿里云百炼 DashScope 原生异步任务协议 */
    public static final String DASHSCOPE_NATIVE = "DASHSCOPE_NATIVE";

    /** Anthropic Claude Messages 协议 */
    public static final String CLAUDE = "CLAUDE";

    /** Google Gemini generateContent 协议 */
    public static final String GEMINI = "GEMINI";

    // ==================== 鉴权模板常量 ====================

    /** 密钥占位符 */
    public static final String SECRET_PLACEHOLDER = "{secret}";

    /** 模型名占位符 */
    public static final String MODEL_NAME_PLACEHOLDER = "{modelName}";

    /** 任务 ID 占位符 */
    public static final String TASK_ID_PLACEHOLDER = "{taskId}";

    /** Authorization 头名称 */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /** Bearer 前缀 */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Claude api-key 头名称 */
    public static final String HEADER_X_API_KEY = "x-api-key";

    /** Claude 协议版本头名称 */
    public static final String HEADER_ANTHROPIC_VERSION = "anthropic-version";

    /** Claude 协议版本值 */
    public static final String ANTHROPIC_VERSION_VALUE = "2023-06-01";

    /** DashScope 异步开关头名称 */
    public static final String HEADER_DASHSCOPE_ASYNC = "X-DashScope-Async";

    /** DashScope 异步开关值 */
    public static final String ASYNC_ENABLED = "enable";

    /** Gemini 密钥查询参数名 */
    public static final String GEMINI_KEY_PARAM = "key";

    // ==================== 路径常量 ====================

    /** Claude 同步对话路径 */
    public static final String CLAUDE_SYNC_PATH = "/v1/messages";

    /** Gemini 同步对话路径模板 */
    public static final String GEMINI_SYNC_PATH = "/v1beta/models/{modelName}:generateContent";

    /** DashScope 文生图提交路径 */
    public static final String DASHSCOPE_TEXT2IMAGE_PATH = "/api/v1/services/aigc/text2image/image-synthesis";

    /** DashScope 虚拟试衣提交路径 */
    public static final String DASHSCOPE_VIRTUALTRYON_PATH = "/api/v1/services/aigc/virtualtryon/image-generation";

    /** DashScope 任务轮询路径模板 */
    public static final String DASHSCOPE_TASK_PATH_TEMPLATE = "/api/v1/tasks/{taskId}";

    // ==================== 响应 JSON 指针 ====================

    /** OpenAI 对话文本指针 */
    public static final String POINTER_OPENAI_CONTENT = "$.choices[0].message.content";

    /** Claude 对话文本指针 */
    public static final String CLAUDE_CONTENT_POINTER = "$.content[0].text";

    /** Gemini 对话文本指针 */
    public static final String GEMINI_CONTENT_POINTER = "$.candidates[0].content.parts[0].text";

    /** DashScope 任务 ID 指针 */
    public static final String DASHSCOPE_TASK_ID_POINTER = "$.output.task_id";

    /** DashScope 任务状态指针 */
    public static final String DASHSCOPE_STATUS_POINTER = "$.output.task_status";

    /** DashScope 结果图指针 */
    public static final String DASHSCOPE_IMAGE_POINTER = "$.output.results[0].url";

    /** DashScope 结果图兜底指针（Base64） */
    public static final String DASHSCOPE_B64_IMAGE_POINTER = "$.output.results[0].b64_image";

    /** DashScope 失败消息指针 */
    public static final String DASHSCOPE_MESSAGE_POINTER = "$.output.message";

    /** 内置协议档案表 */
    private static final Map<String, ProtocolProfile> CATALOG = Map.of(
            OPENAI_COMPATIBLE, buildOpenAiCompatible(),
            CLAUDE, buildClaude(),
            GEMINI, buildGemini(),
            DASHSCOPE_NATIVE, buildDashScope());

    /**
     * 鉴权请求头模板
     *
     * @param name
     *     头名称
     * @param valueTemplate
     *     值模板（可用 {secret} 占位）
     */
    public record AuthHeader(String name, String valueTemplate) {
    }

    /**
     * 按编码获取内置协议档案；未知档案按 OpenAI 兼容协议兜底
     *
     * @param code
     *     档案编码
     * @return 协议档案
     */
    public static ProtocolProfile fromCode(String code) {
        ProtocolProfile profile = CATALOG.get(code);
        if (profile == null) {
            return CATALOG.get(OPENAI_COMPATIBLE);
        }
        return profile;
    }

    /**
     * 判断是否声明了鉴权头
     *
     * @return true 表示至少一个鉴权头
     */
    public boolean hasAuthHeaders() {
        return CollectionUtilExt.isNotEmpty(authHeaders);
    }

    /**
     * 获取指定模型类型的异步提交路径
     *
     * @param modelType
     *     模型类型
     * @return 路径，未配置返回 null
     */
    public String submitPath(ModelType modelType) {
        return submitPaths.get(modelType);
    }

    /**
     * 构建 OpenAI 兼容协议档案
     *
     * @return 协议档案
     */
    private static ProtocolProfile buildOpenAiCompatible() {
        List<AuthHeader> headers = List.of(
                new AuthHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + SECRET_PLACEHOLDER));
        return new ProtocolProfile(
                OPENAI_COMPATIBLE, false, headers, null,
                RequestBodyStyle.OPENAI_MESSAGES,
                null, Map.of(), null,
                POINTER_OPENAI_CONTENT, null, null, null, null, null);
    }

    /**
     * 构建 Claude Messages 协议档案
     *
     * @return 协议档案
     */
    private static ProtocolProfile buildClaude() {
        List<AuthHeader> headers = List.of(
                new AuthHeader(HEADER_X_API_KEY, SECRET_PLACEHOLDER),
                new AuthHeader(HEADER_ANTHROPIC_VERSION, ANTHROPIC_VERSION_VALUE));
        return new ProtocolProfile(
                CLAUDE, false, headers, null,
                RequestBodyStyle.CLAUDE_MESSAGES,
                CLAUDE_SYNC_PATH, Map.of(), null,
                CLAUDE_CONTENT_POINTER, null, null, null, null, null);
    }

    /**
     * 构建 Gemini generateContent 协议档案
     *
     * @return 协议档案
     */
    private static ProtocolProfile buildGemini() {
        return new ProtocolProfile(
                GEMINI, false, List.of(), GEMINI_KEY_PARAM,
                RequestBodyStyle.GEMINI_CONTENTS,
                GEMINI_SYNC_PATH, Map.of(), null,
                GEMINI_CONTENT_POINTER, null, null, null, null, null);
    }

    /**
     * 构建 DashScope 原生异步任务协议档案
     *
     * @return 协议档案
     */
    private static ProtocolProfile buildDashScope() {
        List<AuthHeader> headers = List.of(
                new AuthHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + SECRET_PLACEHOLDER),
                new AuthHeader(HEADER_DASHSCOPE_ASYNC, ASYNC_ENABLED));
        Map<ModelType, String> submitPaths = Map.of(
                ModelType.IMAGE_GENERATION, DASHSCOPE_TEXT2IMAGE_PATH,
                ModelType.IMAGE_EDITING, DASHSCOPE_VIRTUALTRYON_PATH);
        return new ProtocolProfile(
                DASHSCOPE_NATIVE, true, headers, null,
                RequestBodyStyle.DASHSCOPE_ASYNC_TASK,
                null, submitPaths, DASHSCOPE_TASK_PATH_TEMPLATE,
                null, DASHSCOPE_TASK_ID_POINTER, DASHSCOPE_STATUS_POINTER,
                DASHSCOPE_IMAGE_POINTER, DASHSCOPE_B64_IMAGE_POINTER, DASHSCOPE_MESSAGE_POINTER);
    }
}
