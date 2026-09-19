package com.dayu.yiyibushe.infra.ai.service;

import com.dayu.yiyibushe.common.constant.TaskStatus;
import com.dayu.yiyibushe.common.util.StringUtilExt;

import com.dayu.yiyibushe.infra.ai.connection.ModelConnection;
import com.dayu.yiyibushe.infra.ai.connection.ModelConnectionFactory;
import com.dayu.yiyibushe.infra.ai.core.AiMessage;
import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.credential.ApiCredential;
import com.dayu.yiyibushe.infra.ai.credential.CredentialManager;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import com.dayu.yiyibushe.infra.ai.registry.ProviderInfo;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * AI 平台统一服务，是业务层调用模型能力的唯一入口。
 * <p>
 * 提供：文本对话、图片生成、虚拟试衣、异步任务提交 / 轮询 / 等待、
 * 免注册直连、流程编排执行。业务层只给模型编码或厂商凭证，不关心协议差异。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Service
public class AiPlatformService {

    private final ModelRegistry modelRegistry;
    private final ModelConnectionFactory connectionFactory;
    private final CredentialManager credentialManager;

    /**
     * 构造器
     *
     * @param modelRegistry
     *                          模型注册表
     * @param connectionFactory
     *                          连接工厂
     * @param credentialManager
     *                          凭证管理器
     */
    public AiPlatformService(ModelRegistry modelRegistry, ModelConnectionFactory connectionFactory,
            CredentialManager credentialManager) {
        this.modelRegistry = modelRegistry;
        this.connectionFactory = connectionFactory;
        this.credentialManager = credentialManager;
    }

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
     * 多轮文本对话
     *
     * @param modelCode
     *                  模型编码
     * @param messages
     *                  消息列表
     * @return 模型回复
     */
    public String chat(String modelCode, List<AiMessage> messages) {
        AiRequest request = new AiRequest();
        request.setModelCode(modelCode);
        request.setMessages(messages);
        AiResponse response = getConnection(modelCode).execute(request);
        if (!response.isSuccess()) {
            throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
        }
        return response.getText();
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
        AiResponse response = getConnection(modelCode).execute(request);
        if (!response.isSuccess()) {
            throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
        }
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
        AiResponse response = getConnection(modelCode).execute(request);
        if (!response.isSuccess()) {
            throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
        }
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
        return getConnection(modelCode).submit(request);
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
        return getConnection(modelCode).submit(request);
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
        return getConnection(modelCode).poll(taskId);
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
        ModelConnection connection = getConnection(modelCode);
        long deadline = System.currentTimeMillis() + definition.getPollTimeoutMs();
        AiResponse response;
        while (System.currentTimeMillis() < deadline) {
            response = connection.poll(taskId);
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
     * 凭证会写入运行时凭证存储，端点信息由 {@link ProviderInfo} 补齐。
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
        credentialManager.saveCredential(info.getCode(), new ApiCredential(appKey, appSecret));
        ModelDefinition definition = new ModelDefinition();
        definition.setCode("adhoc-" + info.getCode() + "-" + modelName);
        definition.setProvider(info.getCode());
        definition.setBaseUrl(info.getBaseUrl());
        definition.setChatPath(info.getChatPath());
        definition.setModelName(modelName);

        AiRequest request = new AiRequest();
        request.setModelCode(definition.getCode());
        request.setMessages(List.of(AiMessage.user(userMessage)));
        modelRegistry.register(definition);
        AiResponse response = connectionFactory.create(definition).execute(request);
        if (!response.isSuccess()) {
            throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
        }
        return response.getText();
    }

    /**
     * 获取模型连接
     *
     * @param modelCode
     *                  模型编码
     * @return 模型连接
     */
    private ModelConnection getConnection(String modelCode) {
        if (StringUtilExt.isBlank(modelCode)) {
            throw new BizException(ParamErrorCode.MODEL_CODE_BLANK);
        }
        return connectionFactory.create(modelRegistry.require(modelCode));
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
