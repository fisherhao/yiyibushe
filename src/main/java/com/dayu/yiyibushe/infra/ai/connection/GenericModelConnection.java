package com.dayu.yiyibushe.infra.ai.connection;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.dayu.yiyibushe.common.constant.TaskStatus;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.JsonUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.connection.profile.ProtocolProfile;
import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.credential.CredentialManager;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

/**
 * 通用模型连接：所有厂商共用本类，连接行为完全由 {@link ProtocolProfile}（协议档案）
 * 与 {@link com.dayu.yiyibushe.infra.ai.connection.profile.RequestBodyStyle}（请求体风格）驱动。
 * <p>
 * 工作方式：
 * <ul>
 *     <li>鉴权：按档案把密钥填进鉴权头或查询参数；</li>
 *     <li>同步对话：按请求体风格组装 → POST 同步路径 → 按内容指针取回复文本；</li>
 *     <li>异步任务：提交路径返回任务 ID（指针提取），轮询路径按状态/图片/消息指针归一化，
 *     轮询直到完成的通用逻辑继承自 {@link AbstractModelConnection}。</li>
 * </ul>
 * 新接入一个厂商不需要再写连接类：OpenAI 兼容厂商只加一行厂商配置；
 * 全新协议只加一份协议档案。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class GenericModelConnection extends AbstractModelConnection {

    /** 协议档案 */
    private final ProtocolProfile profile;

    /** HTTP 客户端 */
    private final RestClient restClient;

    /** 密钥（用于查询参数鉴权） */
    private final String secret;

    /**
     * 构造器：按协议档案构建鉴权 HTTP 客户端
     *
     * @param model
     *     模型定义
     * @param credentialManager
     *     凭证管理器
     * @param profile
     *     通信协议档案
     */
    public GenericModelConnection(ModelDefinition model, CredentialManager credentialManager,
                                  ProtocolProfile profile) {
        super(model);
        this.profile = profile;
        this.secret = credentialManager.requireSecret(model.getProvider());
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(model.getBaseUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        // 鉴权头：把档案中的 {secret} 占位替换成真实密钥
        if (profile.hasAuthHeaders()) {
            for (ProtocolProfile.AuthHeader header : profile.authHeaders()) {
                String headerValue = StringUtilExt.replace(
                        header.valueTemplate(), ProtocolProfile.SECRET_PLACEHOLDER, secret);
                builder.defaultHeader(header.name(), headerValue);
            }
        }
        this.restClient = builder.build();
    }

    /**
     * 同步对话：组装请求体并 POST，按内容指针取回复
     *
     * @param request
     *     统一 AI 请求
     * @return 文本响应
     */
    @Override
    protected AiResponse doExecute(AiRequest request) {
        JSONObject body = profile.bodyStyle().assemble(model, request);
        String pathTemplate = StringUtilExt.isBlank(profile.syncPath())
                ? model.getChatPath() : profile.syncPath();
        String path = renderPath(pathTemplate, null);
        String rawResponse = postJson(path, body);
        JSONObject json = JsonUtilExt.parseObject(rawResponse);

        String content = readPointer(json, profile.contentPointer());
        if (StringUtilExt.isBlank(content)) {
            throw new BizException(BizErrorCode.DASHSCOPE_RESPONSE_INVALID);
        }
        return AiResponse.successText(content);
    }

    /**
     * 异步提交任务：POST 提交路径，按任务 ID 指针取值
     *
     * @param request
     *     统一 AI 请求
     * @return 任务 ID
     */
    @Override
    protected String doSubmit(AiRequest request) {
        JSONObject body = profile.bodyStyle().assemble(model, request);
        String pathTemplate = profile.submitPath(model.getModelType());
        if (StringUtilExt.isBlank(pathTemplate)) {
            throw new BizException(BizErrorCode.MODEL_SYNC_NO_SUBMIT);
        }
        String path = renderPath(pathTemplate, null);
        String rawResponse = postJson(path, body);
        JSONObject json = JsonUtilExt.parseObject(rawResponse);

        String taskId = readPointer(json, profile.taskIdPointer());
        if (StringUtilExt.isBlank(taskId)) {
            throw new BizException(BizErrorCode.DASHSCOPE_RESPONSE_INVALID);
        }
        return taskId;
    }

    /**
     * 单次轮询：GET 轮询路径，按状态/图片/消息指针归一化为统一响应
     *
     * @param taskId
     *     任务 ID
     * @return 统一 AI 响应
     */
    @Override
    protected AiResponse doPoll(String taskId) {
        String path = renderPath(profile.pollPathTemplate(), taskId);
        String rawResponse = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    // 查询参数鉴权（如 Gemini 的 key=）
                    if (StringUtilExt.isNotBlank(profile.secretQueryParam())) {
                        uriBuilder.queryParam(profile.secretQueryParam(), secret);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(String.class);
        JSONObject json = JsonUtilExt.parseObject(rawResponse);

        AiResponse response = new AiResponse();
        response.setTaskId(taskId);
        response.setRawResponse(rawResponse);
        String rawStatus = readPointer(json, profile.statusPointer());
        String status = StringUtilExt.isBlank(rawStatus)
                ? TaskStatus.UNKNOWN : StringUtilExt.upperCase(rawStatus);
        response.setStatus(status);

        if (StringUtilExt.equals(TaskStatus.SUCCEEDED, status)) {
            response.setSuccess(true);
            response.setImageUrls(extractImageUrls(json));
        } else if (StringUtilExt.equals(TaskStatus.FAILED, status)) {
            response.setSuccess(false);
            response.setErrorMessage(readPointer(json, profile.messagePointer()));
        }
        return response;
    }

    /**
     * POST JSON 并返回响应原文
     *
     * @param path
     *     请求路径
     * @param body
     *     请求体
     * @return 响应原文
     */
    private String postJson(String path, JSONObject body) {
        return restClient.post()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    if (StringUtilExt.isNotBlank(profile.secretQueryParam())) {
                        uriBuilder.queryParam(profile.secretQueryParam(), secret);
                    }
                    return uriBuilder.build();
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toJSONString())
                .retrieve()
                .body(String.class);
    }

    /**
     * 渲染路径模板：替换模型名与任务 ID 占位符
     *
     * @param template
     *     路径模板
     * @param taskId
     *     任务 ID（可为 null）
     * @return 渲染后的路径
     */
    private String renderPath(String template, String taskId) {
        String path = StringUtilExt.replace(
                template, ProtocolProfile.MODEL_NAME_PLACEHOLDER, model.getModelName());
        if (StringUtilExt.isNotBlank(taskId)) {
            path = StringUtilExt.replace(path, ProtocolProfile.TASK_ID_PLACEHOLDER, taskId);
        }
        return path;
    }

    /**
     * 按 JSON 指针读取字符串
     *
     * @param json
     *     JSON 对象
     * @param pointer
     *     JSON 指针
     * @return 字符串值，指针为 null 或无值返回 null
     */
    private String readPointer(JSONObject json, String pointer) {
        if (StringUtilExt.isBlank(pointer)) {
            return null;
        }
        Object value = JSONPath.of(pointer).eval(json);
        return Objects.isNull(value) ? null : value.toString();
    }

    /**
     * 提取结果图 URL：主指针无值走兜底指针，仍无值抛错
     *
     * @param json
     *     JSON 对象
     * @return 单图 URL 列表
     */
    private List<String> extractImageUrls(JSONObject json) {
        String imageUrl = readPointer(json, profile.imagePointer());
        if (StringUtilExt.isBlank(imageUrl)) {
            imageUrl = readPointer(json, profile.fallbackImagePointer());
        }
        if (StringUtilExt.isBlank(imageUrl)) {
            throw new BizException(BizErrorCode.RESULT_IMAGE_MISSING);
        }
        return List.of(imageUrl);
    }
}
