package com.dayu.yiyibushe.web.dto;

import com.dayu.yiyibushe.common.BaseRequest;

/**
 * 说明：trigger 触发任务请求 DTO，入参只有任务 ID，
 * 供 HTTP 手动触发，也与外部定时任务 for 循环调用 trigger 的语义对齐。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class TriggerFlowTaskDTO extends BaseRequest {

    private static final long serialVersionUID = 6102938475610293849L;

    /** 待触发任务 ID */
    private Long taskId;

    /**
     * 无参构造器
     */
    public TriggerFlowTaskDTO() {
    }

    /**
     * 获取任务 ID
     *
     * @return 任务 ID
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * 设置任务 ID
     *
     * @param taskId
     *     任务 ID
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
