package com.dayu.yiyibushe.infra.ai.service;

import com.dayu.yiyibushe.common.constant.TaskStatus;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.connection.AgentScopeModelFactory;
import com.dayu.yiyibushe.infra.ai.connection.AsyncDashScopeClient;
import com.dayu.yiyibushe.infra.ai.core.AiMessage;
import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.core.ModelType;
import com.dayu.yiyibushe.infra.ai.credential.ApiCredential;
import com.dayu.yiyibushe.infra.ai.credential.CredentialManager;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import com.dayu.yiyibushe.infra.ai.registry.ProviderInfo;
import io.agentscope.core.message.ImageBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.URLSource;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AI 平台统一服务，是业务层调用模型能力的唯一入口。
 * <p>
 * 提供：文本对话、图片生成、虚拟试衣、异步任务提交 / 轮询 / 等待、
 * 免注册直连。业务层只给模型编码或厂商凭证，不关心协议差异。
 * <p>
 * 内部实现：对话模型全部委托 AgentScope {@link Model}（协议适配 / 鉴权 / 解析由框架完成），
 * 万相异步生图 / 试衣由 {@link AsyncDashScopeClient} 承载。对外方法签名保持稳定。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Service
public class AiPlatformService {

    /** 对话调用的阻塞超时（毫秒） */
    private static final long CHAT_TIMEOUT_MS = 120_000L;

    /** 模型注册表 */
    @Autowired
    private ModelRegistry modelRegistry;

    /** AgentScope 模型工厂 */
    @Autowired
    private AgentScopeModelFactory modelFactory;

    /** 凭证管理器 */
    @Autowired
    private CredentialManager credentialManager;

    /**
     * 单轮文本对话
     *
     * @param modelCode
     *                    模型编码
     * @param userMessage
     *                    用户消息
     * @return 模型回复
     */
    public String chat(String modelCode, String userMessage) {
        return chat(modelCode, List.of(AiMessage.user(userMessage)));
    }

    /**
     * 多轮文本对话：统一消息转 AgentScope Msg 后由 AgentScope 模型执行
     *
     * @param modelCode
     *                  模型编码
     * @param messages
     *                  消息列表
     * @return 模型回复
     */
    public String chat(String modelCode, List<AiMessage> messages) {
        return chatWithModel(modelRegistry.require(modelCode), messages);
    }

    /**
     * 文生图（阻塞，返回图片 URL）
     *
     * @param modelCode
     *                           模型编码
     * @param prompt
     *                           文字描述
     * @param referenceImageUrls
     *                           参考图 URL（可选）
     * @return 图片 URL 列表
     */
    public List<String> generateImage(String modelCode, String prompt, List<String> referenceImageUrls) {
        AiRequest request = new AiRequest();
        request.setModelCode(modelCode);
        request.setPrompt(prompt);
        request.setReferenceImageUrls(referenceImageUrls);
        AiResponse response = executeAsyncTask(modelCode, request);
        return response.getImageUrls();
    }

    /**
     * 虚拟试衣（阻塞）
     *
     * @param modelCode
     *                        模型编码
     * @param personImageUrl
     *                        人物图 URL
     * @param garmentImageUrl
     *                        服装图 URL
     * @return 合成图 URL 列表
     */
    public List<String> virtualTryOn(String modelCode, String personImageUrl, String garmentImageUrl) {
        AiRequest request = new AiRequest();
        request.setModelCode(modelCode);
        request.setPersonImageUrl(personImageUrl);
        request.setGarmentImageUrl(garmentImageUrl);
        AiResponse response = executeAsyncTask(modelCode, request);
        return response.getImageUrls();
    }

    /**
     * 异步提交文生图任务
     *
     * @param modelCode
     *                           模型编码
     * @param prompt
     *                           文字描述
     * @param referenceImageUrls
     *                           参考图 URL（可选）
     * @return 任务 ID
     */
    public String submitImageGeneration(String modelCode, String prompt, List<String> referenceImageUrls) {
        AiRequest request = new AiRequest();
        request.setModelCode(modelCode);
        request.setPrompt(prompt);
        request.setReferenceImageUrls(referenceImageUrls);
        return buildAsyncClient(modelCode).submit(request);
    }

    /**
     * 异步提交虚拟试衣任务
     *
     * @param modelCode
     *                        模型编码
     * @param personImageUrl
     *                        人物图 URL
     * @param garmentImageUrl
     *                        服装图 URL
     * @return 任务 ID
     */
    public String submitVirtualTryOn(String modelCode, String personImageUrl, String garmentImageUrl) {
        AiRequest request = new AiRequest();
        request.setModelCode(modelCode);
        request.setPersonImageUrl(personImageUrl);
        request.setGarmentImageUrl(garmentImageUrl);
        return buildAsyncClient(modelCode).submit(request);
    }

    /**
     * 单次轮询异步任务
     *
     * @param modelCode
     *                  模型编码
     * @param taskId
     *                  任务 ID
     * @return 任务响应
     */
    public AiResponse pollTask(String modelCode, String taskId) {
        return buildAsyncClient(modelCode).poll(taskId);
    }

    /**
     * 阻塞等待异步任务完成（按模型定义的轮询间隔与超时）
     *
     * @param modelCode
     *                  模型编码
     * @param taskId
     *                  任务 ID
     * @return 成功时的任务响应
     */
    public AiResponse waitTask(String modelCode, String taskId) {
        ModelDefinition definition = modelRegistry.require(modelCode);
        AsyncDashScopeClient client = buildAsyncClient(definition);
        long deadline = System.currentTimeMillis() + definition.getPollTimeoutMs();
        AiResponse response;
        while (System.currentTimeMillis() < deadline) {
            response = client.poll(taskId);
            String status = response.getStatus();
            if (StringUtilExt.equals(TaskStatus.SUCCEEDED, status)) {
                return response;
            }
            if (StringUtilExt.equals(TaskStatus.FAILED, status)) {
                throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
            }
            sleep(definition.getPollIntervalMs());
        }
        throw new BizException(BizErrorCode.DASHSCOPE_TASK_TIMEOUT);
    }

    /**
     * 免注册直连：只给厂商名 + appKey/appSecret + 模型名，立即对话。
     * <p>
     * 凭证会写入运行时凭证存储，端点信息由 {@link ProviderInfo} 补齐，
     * 对话执行与 {@link #chat(String, List)} 一样走 AgentScope 模型。
     *
     * @param providerCode
     *                     厂商名（如 deepseek、openai、claude、gemini、qwen、doubao、moonshot）
     * @param appKey
     *                     应用标识（部分厂商可空）
     * @param appSecret
     *                     应用密钥
     * @param modelName
     *                     模型名或模型 ID
     * @param userMessage
     *                     用户消息
     * @return 模型回复
     */
    public String connectAndChat(String providerCode, String appKey, String appSecret,
            String modelName, String userMessage) {
        ProviderInfo info = ProviderInfo.fromCode(providerCode);
        if (Objects.isNull(info)) {
            throw new BizException(BizErrorCode.PROVIDER_UNKNOWN);
        }
        credentialManager.saveCredential(info.getCode(), ApiCredential.create(appKey, appSecret));
        ModelDefinition definition = new ModelDefinition();
        definition.setCode("adhoc-" + info.getCode() + "-" + modelName);
        definition.setProvider(info.getCode());
        definition.setModelType(ModelType.TEXT_CHAT);
        definition.setBaseUrl(info.getBaseUrl());
        definition.setChatPath(info.getChatPath());
        definition.setModelName(modelName);
        modelRegistry.register(definition);
        return chatWithModel(definition, List.of(AiMessage.user(userMessage)));
    }

    /**
     * 用指定模型定义执行对话：AiMessage → AgentScope Msg → AgentScope 模型 → 提取文本
     *
     * @param definition
     *                   模型定义
     * @param messages
     *                   统一消息列表
     * @return 模型回复文本
     */
    private String chatWithModel(ModelDefinition definition, List<AiMessage> messages) {
        Model model = modelFactory.createChatModel(definition);
        ChatResponse response = model.stream(toAgentScopeMessages(messages), null, null)
                .blockLast(Duration.ofMillis(CHAT_TIMEOUT_MS));
        String text = extractText(response);
        if (StringUtilExt.isBlank(text)) {
            throw new BizException(BizErrorCode.DASHSCOPE_RESPONSE_INVALID);
        }
        return text;
    }

    /**
     * 统一消息转 AgentScope 消息：system → SYSTEM，user（含多模态图文）→ USER，
     * assistant → ASSISTANT；图片片段转 {@link ImageBlock}
     *
     * @param messages
     *                 统一消息列表
     * @return AgentScope 消息列表
     */
    private List<Msg> toAgentScopeMessages(List<AiMessage> messages) {
        List<Msg> agentMessages = new ArrayList<>();
        if (CollectionUtilExt.isEmpty(messages)) {
            return agentMessages;
        }
        for (AiMessage message : messages) {
            MsgRole role = resolveRole(message.getRole());
            if (CollectionUtilExt.isNotEmpty(message.getParts())) {
                List<ContentBlock> blocks = new ArrayList<>();
                for (AiMessage.ContentPart part : message.getParts()) {
                    blocks.add(toContentBlock(part));
                }
                agentMessages.add(Msg.builder().role(role).content(blocks).build());
            } else {
                agentMessages.add(Msg.builder().role(role).textContent(message.getContent()).build());
            }
        }
        return agentMessages;
    }

    /**
     * 统一角色映射为 AgentScope 角色，未知角色按 user 处理
     *
     * @param role
     *             统一角色名
     * @return AgentScope 角色
     */
    private MsgRole resolveRole(String role) {
        if (StringUtilExt.equals("system", role)) {
            return MsgRole.SYSTEM;
        }
        if (StringUtilExt.equals("assistant", role)) {
            return MsgRole.ASSISTANT;
        }
        return MsgRole.USER;
    }

    /**
     * 多模态片段转 AgentScope 内容块：文本 → {@link TextBlock}，图片 → {@link ImageBlock}
     *
     * @param part
     *             统一内容片段
     * @return AgentScope 内容块
     */
    private ContentBlock toContentBlock(AiMessage.ContentPart part) {
        if (StringUtilExt.equals("image_url", part.getType())) {
            return ImageBlock.builder().source(new URLSource(part.getImageUrl())).build();
        }
        return TextBlock.builder().text(part.getText()).build();
    }

    /**
     * 从 AgentScope 响应中提取文本内容
     *
     * @param response
     *                 AgentScope 对话响应
     * @return 文本，无内容返回 null
     */
    private String extractText(ChatResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<TextBlock> textBlocks = CollectionUtilExt.toStream(response.getContent())
                .filter(TextBlock.class::isInstance)
                .map(TextBlock.class::cast)
                .toList();
        if (CollectionUtilExt.isEmpty(textBlocks)) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (TextBlock textBlock : textBlocks) {
            text.append(textBlock.getText());
        }
        return text.toString();
    }

    /**
     * 提交并阻塞等待异步任务完成
     *
     * @param modelCode
     *                  模型编码
     * @param request
     *                  统一 AI 请求
     * @return 成功时的任务响应
     */
    private AiResponse executeAsyncTask(String modelCode, AiRequest request) {
        ModelDefinition definition = modelRegistry.require(modelCode);
        AsyncDashScopeClient client = buildAsyncClient(definition);
        return pollUntilDone(definition, client, client.submit(request));
    }

    /**
     * 轮询直到任务完成或超时（按模型定义的轮询间隔与超时）
     *
     * @param definition
     *                   模型定义
     * @param client
     *                   异步任务客户端
     * @param taskId
     *                   任务 ID
     * @return 成功时的任务响应
     */
    private AiResponse pollUntilDone(ModelDefinition definition, AsyncDashScopeClient client, String taskId) {
        long deadline = System.currentTimeMillis() + definition.getPollTimeoutMs();
        AiResponse response;
        while (System.currentTimeMillis() < deadline) {
            response = client.poll(taskId);
            String status = response.getStatus();
            if (StringUtilExt.equals(TaskStatus.SUCCEEDED, status)) {
                return response;
            }
            if (StringUtilExt.equals(TaskStatus.FAILED, status)) {
                throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
            }
            sleep(definition.getPollIntervalMs());
        }
        throw new BizException(BizErrorCode.DASHSCOPE_TASK_TIMEOUT);
    }

    /**
     * 构建异步任务客户端（校验模型编码并取厂商密钥）
     *
     * @param modelCode
     *                  模型编码
     * @return 异步任务客户端
     */
    private AsyncDashScopeClient buildAsyncClient(String modelCode) {
        if (StringUtilExt.isBlank(modelCode)) {
            throw new BizException(ParamErrorCode.MODEL_CODE_BLANK);
        }
        return buildAsyncClient(modelRegistry.require(modelCode));
    }

    /**
     * 按模型定义构建异步任务客户端
     *
     * @param definition
     *                   模型定义
     * @return 异步任务客户端
     */
    private AsyncDashScopeClient buildAsyncClient(ModelDefinition definition) {
        if (!definition.isAsync()) {
            throw new BizException(BizErrorCode.MODEL_SYNC_NO_SUBMIT);
        }
        return AsyncDashScopeClient.create(definition,
                credentialManager.requireSecret(definition.getProvider()));
    }

    /**
     * 休眠
     *
     * @param millis
     *               毫秒
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
