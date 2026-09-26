package com.dayu.yiyibushe.infra.ai.connection;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.dayu.yiyibushe.common.constant.TaskStatus;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.JsonUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.core.ModelType;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

/**
 * DashScope 异步任务客户端：承载万相文生图 / 虚拟试衣的提交与轮询。
 * <p>
 * AgentScope 2.0.1 的模型扩展只覆盖对话类模型，未覆盖 DashScope 原生异步任务协议
 * （X-DashScope-Async），本类按官方 HTTP 协议保留最小实现，取代原
 * GenericModelConnection 中被 AgentScope 替代不了的这部分手写 HTTP 逻辑。
 * <p>
 * 一个实例对应一个异步模型定义，经 {@link #create} 创建时完成鉴权。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class AsyncDashScopeClient {

    // ==================== 路径常量（DashScope 原生异步协议） ====================

    /** 文生图提交路径 */
    private static final String TEXT2IMAGE_PATH = "/api/v1/services/aigc/text2image/image-synthesis";

    /** 虚拟试衣提交路径 */
    private static final String VIRTUALTRYON_PATH = "/api/v1/services/aigc/virtualtryon/image-generation";

    /** 任务轮询路径模板 */
    private static final String TASK_PATH_TEMPLATE = "/api/v1/tasks/{taskId}";

    // ==================== 鉴权与协议头 ====================

    /** Authorization 头名称 */
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /** Bearer 前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** DashScope 异步开关头名称 */
    private static final String HEADER_DASHSCOPE_ASYNC = "X-DashScope-Async";

    /** DashScope 异步开关值 */
    private static final String ASYNC_ENABLED = "enable";

    // ==================== 请求体键 ====================

    /** 模型名键 */
    private static final String KEY_MODEL = "model";

    /** input 键 */
    private static final String KEY_INPUT = "input";

    /** parameters 键 */
    private static final String KEY_PARAMETERS = "parameters";

    /** prompt 键 */
    private static final String KEY_PROMPT = "prompt";

    /** ref_img 键 */
    private static final String KEY_REF_IMG = "ref_img";

    /** ref_img 中单图的 image 键 */
    private static final String KEY_IMAGE = "image";

    /** person_image_url 键 */
    private static final String KEY_PERSON_IMAGE_URL = "person_image_url";

    /** garment_image_url 键 */
    private static final String KEY_GARMENT_IMAGE_URL = "garment_image_url";

    /** 尺寸键 */
    private static final String KEY_SIZE = "size";

    /** 默认图片尺寸 */
    private static final String DEFAULT_IMAGE_SIZE = "1024*1024";

    /** 张数键 */
    private static final String KEY_COUNT = "n";

    /** 默认生成张数 */
    private static final int DEFAULT_COUNT = 1;

    // ==================== 响应 JSON 指针 ====================

    /** 任务 ID 指针 */
    private static final String TASK_ID_POINTER = "$.output.task_id";

    /** 任务状态指针 */
    private static final String STATUS_POINTER = "$.output.task_status";

    /** 结果图指针 */
    private static final String IMAGE_POINTER = "$.output.results[0].url";

    /** 结果图兜底指针（Base64） */
    private static final String B64_IMAGE_POINTER = "$.output.results[0].b64_image";

    /** 失败消息指针 */
    private static final String MESSAGE_POINTER = "$.output.message";

    /** 模型定义 */
    private ModelDefinition model;

    /** HTTP 客户端（构造时完成鉴权头装配） */
    private RestClient restClient;

    /**
     * 静态工厂：校验入参并装配鉴权客户端
     *
     * @param model
     *     异步模型定义（baseUrl 指向 DashScope）
     * @param secret
     *     厂商密钥
     *
     * @return 异步客户端
     */
    public static AsyncDashScopeClient create(ModelDefinition model, String secret) {
        if (Objects.isNull(model)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        if (StringUtilExt.isBlank(secret)) {
            throw new BizException(BizErrorCode.PROVIDER_CREDENTIAL_MISSING);
        }
        RestClient restClient = RestClient.builder()
                .baseUrl(model.getBaseUrl())
                .defaultHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + secret)
                .defaultHeader(HEADER_DASHSCOPE_ASYNC, ASYNC_ENABLED)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        AsyncDashScopeClient client = new AsyncDashScopeClient();
        client.model = model;
        client.restClient = restClient;
        return client;
    }

    /**
     * 获取模型定义
     *
     * @return 模型定义
     */
    public ModelDefinition getModel() {
        return model;
    }

    /**
     * 提交异步任务：按模型类型装配 input 结构，返回任务 ID
     *
     * @param request
     *     统一 AI 请求
     * @return 任务 ID
     */
    public String submit(AiRequest request) {
        String path = resolveSubmitPath();
        String rawResponse = postJson(path, buildRequestBody(request));
        JSONObject json = JsonUtilExt.parseObject(rawResponse);

        String taskId = readPointer(json, TASK_ID_POINTER);
        if (StringUtilExt.isBlank(taskId)) {
            throw new BizException(BizErrorCode.DASHSCOPE_RESPONSE_INVALID);
        }
        return taskId;
    }

    /**
     * 单次轮询：按状态 / 图片 / 消息指针归一化为统一响应
     *
     * @param taskId
     *     任务 ID
     * @return 统一 AI 响应
     */
    public AiResponse poll(String taskId) {
        String rawResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder.path(renderTaskPath(taskId)).build())
                .retrieve()
                .body(String.class);
        JSONObject json = JsonUtilExt.parseObject(rawResponse);

        AiResponse response = new AiResponse();
        response.setTaskId(taskId);
        response.setRawResponse(rawResponse);
        String rawStatus = readPointer(json, STATUS_POINTER);
        String status = StringUtilExt.isBlank(rawStatus)
                ? TaskStatus.UNKNOWN : StringUtilExt.upperCase(rawStatus);
        response.setStatus(status);

        if (StringUtilExt.equals(TaskStatus.SUCCEEDED, status)) {
            response.setSuccess(true);
            response.setImageUrls(extractImageUrls(json));
        } else if (StringUtilExt.equals(TaskStatus.FAILED, status)) {
            response.setSuccess(false);
            response.setErrorMessage(readPointer(json, MESSAGE_POINTER));
        }
        return response;
    }

    /**
     * 按模型类型解析提交路径
     *
     * @return 提交路径
     */
    private String resolveSubmitPath() {
        if (model.getModelType() == ModelType.IMAGE_GENERATION) {
            return TEXT2IMAGE_PATH;
        }
        if (model.getModelType() == ModelType.IMAGE_EDITING) {
            return VIRTUALTRYON_PATH;
        }
        throw new BizException(BizErrorCode.MODEL_SYNC_NO_SUBMIT);
    }

    /**
     * 渲染轮询路径：替换任务 ID 占位符
     *
     * @param taskId
     *     任务 ID
     * @return 轮询路径
     */
    private String renderTaskPath(String taskId) {
        return StringUtilExt.replace(TASK_PATH_TEMPLATE, "{taskId}", taskId);
    }

    /**
     * 装配 DashScope 异步任务请求体：统一 model/input/parameters 结构
     *
     * @param request
     *     统一 AI 请求
     * @return 请求体 JSON
     */
    private JSONObject buildRequestBody(AiRequest request) {
        JSONObject body = new JSONObject();
        body.put(KEY_MODEL, model.getModelName());
        body.put(KEY_INPUT, buildInput(request));
        body.put(KEY_PARAMETERS, buildParameters(request));
        return body;
    }

    /**
     * 构建 input：文生图带 prompt/参考图，虚拟试衣带人物图/服装图
     *
     * @param request
     *     统一 AI 请求
     * @return input JSON
     */
    private JSONObject buildInput(AiRequest request) {
        JSONObject input = new JSONObject();
        if (model.getModelType() == ModelType.IMAGE_GENERATION) {
            input.put(KEY_PROMPT, request.getPrompt());
            if (Objects.nonNull(request.getReferenceImageUrls())) {
                JSONArray refs = new JSONArray();
                for (String referenceUrl : request.getReferenceImageUrls()) {
                    JSONObject reference = new JSONObject();
                    reference.put(KEY_IMAGE, referenceUrl);
                    refs.add(reference);
                }
                input.put(KEY_REF_IMG, refs);
            }
        } else if (model.getModelType() == ModelType.IMAGE_EDITING) {
            input.put(KEY_PERSON_IMAGE_URL, request.getPersonImageUrl());
            input.put(KEY_GARMENT_IMAGE_URL, request.getGarmentImageUrl());
        }
        return input;
    }

    /**
     * 构建 parameters：默认尺寸/张数，再合并业务自定义参数
     *
     * @param request
     *     统一 AI 请求
     * @return parameters JSON
     */
    private JSONObject buildParameters(AiRequest request) {
        JSONObject parameters = new JSONObject();
        parameters.put(KEY_SIZE, DEFAULT_IMAGE_SIZE);
        parameters.put(KEY_COUNT, DEFAULT_COUNT);
        if (Objects.nonNull(request.getParameters())) {
            request.getParameters().forEach(parameters::put);
        }
        return parameters;
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
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toJSONString())
                .retrieve()
                .body(String.class);
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
     * 提取结果图 URL：主指针无值走兜底指针（Base64），仍无值抛错
     *
     * @param json
     *     JSON 对象
     * @return 单图 URL 列表
     */
    private List<String> extractImageUrls(JSONObject json) {
        String imageUrl = readPointer(json, IMAGE_POINTER);
        if (StringUtilExt.isBlank(imageUrl)) {
            imageUrl = readPointer(json, B64_IMAGE_POINTER);
        }
        if (StringUtilExt.isBlank(imageUrl)) {
            throw new BizException(BizErrorCode.RESULT_IMAGE_MISSING);
        }
        return List.of(imageUrl);
    }
}
