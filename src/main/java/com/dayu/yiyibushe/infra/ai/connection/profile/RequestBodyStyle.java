package com.dayu.yiyibushe.infra.ai.connection.profile;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.core.AiMessage;
import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.ModelType;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;

import java.util.List;
import java.util.Objects;

/**
 * 请求体装配风格：把统一的 {@link AiRequest} 组装成各协议的请求体 JSON。
 * <p>
 * 不同协议请求结构差异集中在本枚举（OpenAI messages、Claude messages、
 * Gemini contents、DashScope 异步任务 input/parameters），由 {@link GenericModelConnection}
 * 按 {@link ProtocolProfile#bodyStyle()} 调用。新增协议若请求结构与已有风格一致，
 * 只加档案不加代码。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public enum RequestBodyStyle {

    /**
     * OpenAI messages 风格（含多模态），并把自定义生成参数合并进请求体
     */
    OPENAI_MESSAGES {
        /**
         * 装配 OpenAI messages 请求体（纯文本/多模态），并合并自定义生成参数
         *
         * @param model
         *     模型定义
         * @param request
         *     统一 AI 请求
         * @return OpenAI 风格请求体
         */
        @Override
        public JSONObject assemble(ModelDefinition model, AiRequest request) {
            JSONObject body = new JSONObject();
            body.put(KEY_MODEL, model.getModelName());
            body.put(KEY_MESSAGES, buildOpenAiMessages(request));
            if (Objects.nonNull(request.getParameters())) {
                request.getParameters().forEach(body::put);
            }
            return body;
        }
    },

    /**
     * Claude Messages 风格：system 消息单独放 system 字段，max_tokens 固定
     */
    CLAUDE_MESSAGES {
        /**
         * 装配 Claude Messages 请求体：system 消息单独放 system 字段，max_tokens 固定
         *
         * @param model
         *     模型定义
         * @param request
         *     统一 AI 请求
         * @return Claude 风格请求体
         */
        @Override
        public JSONObject assemble(ModelDefinition model, AiRequest request) {
            JSONObject body = new JSONObject();
            body.put(KEY_MODEL, model.getModelName());
            body.put(KEY_MAX_TOKENS, DEFAULT_MAX_TOKENS);

            // Claude 把 system 消息单独放在 system 字段
            StringBuilder systemPrompt = new StringBuilder();
            JSONArray messages = new JSONArray();
            List<AiMessage> messageList = request.getMessages();
            if (Objects.nonNull(messageList)) {
                for (AiMessage message : messageList) {
                    if (StringUtilExt.equals(ROLE_SYSTEM, message.getRole())) {
                        if (systemPrompt.length() > 0) {
                            systemPrompt.append("\n");
                        }
                        systemPrompt.append(message.getContent());
                    } else {
                        JSONObject item = new JSONObject();
                        item.put(KEY_ROLE, message.getRole());
                        item.put(KEY_CONTENT, message.getContent());
                        messages.add(item);
                    }
                }
            }
            if (systemPrompt.length() > 0) {
                body.put(KEY_SYSTEM, systemPrompt.toString());
            }
            body.put(KEY_MESSAGES, messages);
            return body;
        }
    },

    /**
     * Gemini contents 风格：system 消息放 systemInstruction，其余角色映射为 user/model
     */
    GEMINI_CONTENTS {
        /**
         * 装配 Gemini contents 请求体：system 放 systemInstruction，角色映射为 user/model
         *
         * @param model
         *     模型定义
         * @param request
         *     统一 AI 请求
         * @return Gemini 风格请求体
         */
        @Override
        public JSONObject assemble(ModelDefinition model, AiRequest request) {
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            List<AiMessage> messageList = Objects.isNull(request.getMessages())
                    ? List.of() : request.getMessages();
            for (AiMessage message : messageList) {
                if (StringUtilExt.equals(ROLE_SYSTEM, message.getRole())) {
                    JSONObject systemInstruction = new JSONObject();
                    JSONArray systemParts = new JSONArray();
                    systemParts.add(buildTextPart(message.getContent()));
                    systemInstruction.put(KEY_PARTS, systemParts);
                    body.put(KEY_SYSTEM_INSTRUCTION, systemInstruction);
                } else {
                    JSONObject content = new JSONObject();
                    content.put(KEY_ROLE,
                            StringUtilExt.equals(ROLE_USER, message.getRole()) ? ROLE_USER : ROLE_MODEL);
                    JSONArray parts = new JSONArray();
                    parts.add(buildTextPart(message.getContent()));
                    content.put(KEY_PARTS, parts);
                    contents.add(content);
                }
            }
            body.put(KEY_CONTENTS, contents);
            return body;
        }
    },

    /**
     * DashScope 异步任务风格：统一包 model/input/parameters，按模型类型区分 input 结构
     */
    DASHSCOPE_ASYNC_TASK {
        /**
         * 装配 DashScope 异步任务请求体：统一 model/input/parameters，按模型类型区分 input
         *
         * @param model
         *     模型定义
         * @param request
         *     统一 AI 请求
         * @return DashScope 风格请求体
         */
        @Override
        public JSONObject assemble(ModelDefinition model, AiRequest request) {
            JSONObject body = new JSONObject();
            body.put(KEY_MODEL, model.getModelName());
            body.put(KEY_INPUT, buildDashScopeInput(model, request));
            body.put(KEY_PARAMETERS, buildDashScopeParameters(request));
            return body;
        }
    };

    // ==================== 通用 JSON 键常量 ====================

    /** 请求体模型名键 */
    protected static final String KEY_MODEL = "model";

    /** 消息列表键 */
    protected static final String KEY_MESSAGES = "messages";

    /** 角色键 */
    protected static final String KEY_ROLE = "role";

    /** 内容键 */
    protected static final String KEY_CONTENT = "content";

    /** 片段类型键 */
    protected static final String KEY_TYPE = "type";

    /** 文本键 */
    protected static final String KEY_TEXT = "text";

    /** 系统字段键 */
    protected static final String KEY_SYSTEM = "system";

    /** 角色：system */
    protected static final String ROLE_SYSTEM = "system";

    /** 角色：user */
    protected static final String ROLE_USER = "user";

    // ==================== OpenAI 多模态常量 ====================

    /** image_url 类型值 */
    protected static final String TYPE_IMAGE_URL = "image_url";

    /** image_url 对象键 */
    protected static final String KEY_IMAGE_URL = "image_url";

    /** URL 键 */
    protected static final String KEY_URL = "url";

    // ==================== Claude 常量 ====================

    /** max_tokens 键 */
    protected static final String KEY_MAX_TOKENS = "max_tokens";

    /** Claude 默认最大输出 token */
    protected static final int DEFAULT_MAX_TOKENS = 4096;

    // ==================== Gemini 常量 ====================

    /** contents 键 */
    protected static final String KEY_CONTENTS = "contents";

    /** parts 键 */
    protected static final String KEY_PARTS = "parts";

    /** systemInstruction 键 */
    protected static final String KEY_SYSTEM_INSTRUCTION = "systemInstruction";

    /** Gemini 模型角色 */
    protected static final String ROLE_MODEL = "model";

    // ==================== DashScope 常量 ====================

    /** input 键 */
    protected static final String KEY_INPUT = "input";

    /** parameters 键 */
    protected static final String KEY_PARAMETERS = "parameters";

    /** prompt 键 */
    protected static final String KEY_PROMPT = "prompt";

    /** ref_img 键 */
    protected static final String KEY_REF_IMG = "ref_img";

    /** ref_img 中单图的 image 键 */
    protected static final String KEY_IMAGE = "image";

    /** person_image_url 键 */
    protected static final String KEY_PERSON_IMAGE_URL = "person_image_url";

    /** garment_image_url 键 */
    protected static final String KEY_GARMENT_IMAGE_URL = "garment_image_url";

    /** 尺寸键 */
    protected static final String KEY_SIZE = "size";

    /** 默认图片尺寸 */
    protected static final String DEFAULT_IMAGE_SIZE = "1024*1024";

    /** 张数键 */
    protected static final String KEY_COUNT = "n";

    /** 默认生成张数 */
    protected static final int DEFAULT_COUNT = 1;

    /**
     * 按协议风格装配请求体
     *
     * @param model
     *     模型定义
     * @param request
     *     统一 AI 请求
     * @return 厂商请求体 JSON
     */
    public abstract JSONObject assemble(ModelDefinition model, AiRequest request);

    /**
     * 构建 OpenAI messages 数组（纯文本或多模态）
     *
     * @param request
     *     统一 AI 请求
     * @return messages 数组
     */
    private static JSONArray buildOpenAiMessages(AiRequest request) {
        JSONArray messages = new JSONArray();
        List<AiMessage> messageList = request.getMessages();
        if (Objects.isNull(messageList)) {
            return messages;
        }
        for (AiMessage message : messageList) {
            JSONObject item = new JSONObject();
            item.put(KEY_ROLE, message.getRole());
            if (CollectionUtilExt.isNotEmpty(message.getParts())) {
                // 多模态内容
                JSONArray content = new JSONArray();
                for (AiMessage.ContentPart part : message.getParts()) {
                    JSONObject partJson = new JSONObject();
                    partJson.put(KEY_TYPE, part.getType());
                    if (StringUtilExt.equals(KEY_TEXT, part.getType())) {
                        partJson.put(KEY_TEXT, part.getText());
                    } else if (StringUtilExt.equals(TYPE_IMAGE_URL, part.getType())) {
                        JSONObject urlObject = new JSONObject();
                        urlObject.put(KEY_URL, part.getImageUrl());
                        partJson.put(KEY_IMAGE_URL, urlObject);
                    }
                    content.add(partJson);
                }
                item.put(KEY_CONTENT, content);
            } else {
                item.put(KEY_CONTENT, message.getContent());
            }
            messages.add(item);
        }
        return messages;
    }

    /**
     * 构建单个 Gemini 文本 part
     *
     * @param text
     *     文本
     * @return part JSON
     */
    private static JSONObject buildTextPart(String text) {
        JSONObject part = new JSONObject();
        part.put(KEY_TEXT, text);
        return part;
    }

    /**
     * 构建 DashScope input：文生图带 prompt/参考图，虚拟试衣带人物图/服装图
     *
     * @param model
     *     模型定义
     * @param request
     *     统一 AI 请求
     * @return input JSON
     */
    private static JSONObject buildDashScopeInput(ModelDefinition model, AiRequest request) {
        JSONObject input = new JSONObject();
        if (model.getModelType() == ModelType.IMAGE_GENERATION) {
            input.put(KEY_PROMPT, request.getPrompt());
            if (CollectionUtilExt.isNotEmpty(request.getReferenceImageUrls())) {
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
     * 构建 DashScope parameters：文生图默认尺寸/张数，再合并自定义参数
     *
     * @param request
     *     统一 AI 请求
     * @return parameters JSON
     */
    private static JSONObject buildDashScopeParameters(AiRequest request) {
        JSONObject parameters = new JSONObject();
        parameters.put(KEY_SIZE, DEFAULT_IMAGE_SIZE);
        parameters.put(KEY_COUNT, DEFAULT_COUNT);
        // 合并业务层传入的自定义参数
        if (Objects.nonNull(request.getParameters())) {
            request.getParameters().forEach(parameters::put);
        }
        return parameters;
    }
}
