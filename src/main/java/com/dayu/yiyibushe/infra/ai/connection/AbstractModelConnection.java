package com.dayu.yiyibushe.infra.ai.connection;

import com.dayu.yiyibushe.common.constant.TaskStatus;
import com.dayu.yiyibushe.common.util.StringUtilExt;

import com.dayu.yiyibushe.infra.ai.core.AiRequest;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.JsonUtilExt;

/**
 * 说明：模型连接抽象基类，封装异步模型的「submit + 轮询直到完成」通用逻辑。
 * <p>
 * 子类只需实现 {@link #doSubmit(AiRequest)} 和 {@link #doPoll(String)}，
 * {@link #execute(AiRequest)} 会自动按模型定义的轮询间隔与超时驱动任务完成。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public abstract class AbstractModelConnection implements ModelConnection {

    /** 模型定义 */
    protected final ModelDefinition model;

    /**
     * 构造器
     *
     * @param model
     *     模型定义
     */
    protected AbstractModelConnection(ModelDefinition model) {
        this.model = model;
    }

    /**
     * 获取模型定义
     *
     * @return 模型定义
     */
    @Override
    public ModelDefinition getModel() {
        return model;
    }

    /**
     * 是否支持异步
     *
     * @return 由模型定义的 async 字段决定
     */
    @Override
    public boolean supportsAsync() {
        return model.isAsync();
    }

    /**
     * 同步执行：异步模型走 submit+poll，同步模型走 doExecute
     *
     * @param request
     *     统一 AI 请求
     * @return 统一 AI 响应
     */
    @Override
    public AiResponse execute(AiRequest request) {
        if (model.isAsync()) {
            String taskId = submit(request);
            return pollUntilDone(taskId);
        }
        return doExecute(request);
    }

    /**
     * 异步提交（子类实现）
     *
     * @param request
     *     统一 AI 请求
     * @return 任务 ID
     */
    @Override
    public String submit(AiRequest request) {
        return doSubmit(request);
    }

    /**
     * 轮询单次结果（子类实现）
     *
     * @param taskId
     *     任务 ID
     * @return 统一 AI 响应
     */
    @Override
    public AiResponse poll(String taskId) {
        return doPoll(taskId);
    }

    /**
     * 轮询直到任务完成或超时
     *
     * @param taskId
     *     任务 ID
     * @return 最终响应
     */
    protected AiResponse pollUntilDone(String taskId) {
        long deadline = System.currentTimeMillis() + model.getPollTimeoutMs();
        while (System.currentTimeMillis() < deadline) {
            AiResponse resp = doPoll(taskId);
            String status = resp.getStatus();
            if (StringUtilExt.equals(TaskStatus.SUCCEEDED, status)) {
                return resp;
            }
            if (StringUtilExt.equals(TaskStatus.FAILED, status)) {
                throw new BizException(BizErrorCode.DASHSCOPE_TASK_FAILED);
            }
            // PENDING / RUNNING / UNKNOWN 继续轮询
            sleep(model.getPollIntervalMs());
        }
        throw new BizException(BizErrorCode.DASHSCOPE_TASK_TIMEOUT);
    }

    /**
     * 同步模型的执行逻辑（文本对话类子类实现）
     *
     * @param request
     *     统一 AI 请求
     * @return 统一 AI 响应
     */
    protected abstract AiResponse doExecute(AiRequest request);

    /**
     * 异步提交逻辑（图片生成类子类实现）
     *
     * @param request
     *     统一 AI 请求
     * @return 任务 ID
     */
    protected abstract String doSubmit(AiRequest request);

    /**
     * 单次轮询逻辑（图片生成类子类实现）
     *
     * @param taskId
     *     任务 ID
     * @return 统一 AI 响应
     */
    protected abstract AiResponse doPoll(String taskId);

    /**
     * 休眠指定毫秒
     *
     * @param ms
     *     毫秒数
     */
    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 工具方法：把对象转 JSON 字符串（统一走 JsonUtilExt）
     *
     * @param obj
     *     对象
     * @return JSON 字符串
     */
    protected String toJson(Object obj) {
        return JsonUtilExt.toJsonString(obj);
    }
}
