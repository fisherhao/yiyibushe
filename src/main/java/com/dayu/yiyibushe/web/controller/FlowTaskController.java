package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.common.ApiResult;
import com.dayu.yiyibushe.common.ExecuteTemplate;
import com.dayu.yiyibushe.infra.flowtask.FlowTask;
import com.dayu.yiyibushe.infra.flowtask.FlowTaskTemplate;
import com.dayu.yiyibushe.infra.flowtask.TaskEngine;
import com.dayu.yiyibushe.web.dto.CreateFlowTaskDTO;
import com.dayu.yiyibushe.web.dto.TriggerFlowTaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 说明：FlowTask 任务框架的 HTTP 入口，统一通过 {@link ExecuteTemplate#send} 调用。
 * <p>
 * 提供四个端点：create 创建任务、trigger 按任务 ID 触发执行（手动触发，
 * 也与外部定时任务 for 循环调用 trigger 的语义对齐）、callback 手动唤醒待回调节点、
 * detail 查询任务详情。任务 ID 等标识统一由 POST 请求体携带。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@RestController
@RequestMapping("/api/flow-tasks")
public class FlowTaskController {

    /** 任务模板：创建任务、查询详情 */
    @Autowired
    private FlowTaskTemplate flowTaskTemplate;

    /** 任务引擎：触发执行、接收回调 */
    @Autowired
    private TaskEngine taskEngine;

    /**
     * 创建任务：按任务类型生成节点链快照并入库存档，返回自增任务 ID
     *
     * @param request
     *     创建任务请求
     * @return 统一返回体，data 为任务 ID
     */
    @PostMapping("/create")
    public ApiResult<Long> createTask(@RequestBody CreateFlowTaskDTO request) {
        return ExecuteTemplate.send(request, req -> {
            long fireMillis = Objects.requireNonNullElseGet(req.getGmtFireMillis(),
                    System::currentTimeMillis);
            int priority = Objects.requireNonNullElse(req.getPriority(),
                    FlowTask.DEFAULT_PRIORITY);
            int maxRetry = Objects.requireNonNullElse(req.getMaxRetry(),
                    FlowTaskTemplate.DEFAULT_MAX_RETRY);
            return flowTaskTemplate.createTask(req.getTaskType(), fireMillis, priority,
                    req.getContext(), maxRetry, req.getRetryStrategyCode());
        });
    }

    /**
     * 触发任务执行：入参只有任务 ID。外部定时任务捞起到期任务后也是循环调用
     * 引擎的 trigger，本端点是它的手动 HTTP 入口。
     *
     * @param request
     *     触发请求
     * @return 统一返回体
     */
    @PostMapping("/trigger")
    public ApiResult<Void> triggerTask(@RequestBody TriggerFlowTaskDTO request) {
        return ExecuteTemplate.send(request, req -> {
            taskEngine.trigger(req.getTaskId());
            return null;
        });
    }

    /**
     * 手动唤醒待回调节点：等价于 MQ 回调推送，入参只有任务 ID——
     * 引擎按 currentNodeType 直接定位 WAIT 节点执行 receipt。
     *
     * @param request
     *     触发请求（taskId 在请求体中）
     * @return 统一返回体
     */
    @PostMapping("/callback")
    public ApiResult<Void> callback(@RequestBody TriggerFlowTaskDTO request) {
        return ExecuteTemplate.send(request, req -> {
            taskEngine.callback(req.getTaskId());
            return null;
        });
    }

    /**
     * 查询任务详情：包含任务状态、节点快照与业务数据
     *
     * @param request
     *     触发请求（taskId 在请求体中）
     * @return 统一返回体，data 为任务详情
     */
    @PostMapping("/detail")
    public ApiResult<FlowTask> getTaskDetail(@RequestBody TriggerFlowTaskDTO request) {
        return ExecuteTemplate.send(request,
                req -> flowTaskTemplate.requireTask(req.getTaskId()));
    }
}
