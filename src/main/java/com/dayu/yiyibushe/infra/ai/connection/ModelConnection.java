package com.dayu.yiyibushe.infra.ai.connection;

import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;

/**
 * 说明：模型连接接口，定义与某一厂商某一模型通信的统一契约。
 * <p>
 * 连接层只负责「把 AiRequest 翻译成厂商 API 请求、把厂商响应归一化成 AiResponse」，
 * 不承载业务编排。新增厂商只需实现本接口并在 {@link ModelConnectionFactory} 注册。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface ModelConnection {

    /**
     * 获取本连接对应的模型定义
     *
     * @return 模型定义
     */
    ModelDefinition getModel();

    /**
     * 同步执行请求，阻塞直到返回最终结果。
     * <p>
     * 对异步模型（图片生成类），内部会自动 submit + poll；
     * 对同步模型（文本对话类），直接发起请求并返回。
     *
     * @param request
     *     统一 AI 请求
     * @return 统一 AI 响应
     */
    AiResponse execute(AiRequest request);

    /**
     * 异步提交任务，返回任务 ID（仅异步模型支持）
     *
     * @param request
     *     统一 AI 请求
     * @return 任务 ID
     */
    String submit(AiRequest request);

    /**
     * 轮询异步任务结果（仅异步模型支持）
     *
     * @param taskId
     *     任务 ID
     * @return 统一 AI 响应
     */
    AiResponse poll(String taskId);

    /**
     * 是否支持异步任务模式
     *
     * @return true 支持异步
     */
    boolean supportsAsync();
}
