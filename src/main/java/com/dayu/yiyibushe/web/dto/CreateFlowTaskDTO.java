package com.dayu.yiyibushe.web.dto;

import com.dayu.yiyibushe.common.BaseRequest;

import java.util.Map;

/**
 * 说明：创建 flowtask 任务请求 DTO，接收任务类型、调度时间、优先级、
 * 业务数据与重试参数。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class CreateFlowTaskDTO extends BaseRequest {

    private static final long serialVersionUID = 6102938475610293848L;

    /** 任务类型（该类型下必须存在节点 Bean） */
    private String taskType;

    /** 期望调度时间（毫秒时间戳），为空按当前时间处理 */
    private Long gmtFireMillis;

    /** 优先级（数值越小越优先），为空走默认优先级 */
    private Integer priority;

    /** 任务级业务数据 */
    private Map<String, Object> context;

    /** 最大重试次数，为空走默认值 */
    private Integer maxRetry;

    /** 重试间隔策略 code，为空走全局默认策略 */
    private String retryStrategyCode;

    /**
     * 无参构造器
     */
    public CreateFlowTaskDTO() {
    }

    /**
     * 获取任务类型
     *
     * @return 任务类型
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * 设置任务类型
     *
     * @param taskType
     *     任务类型
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    /**
     * 获取期望调度时间
     *
     * @return 毫秒时间戳
     */
    public Long getGmtFireMillis() {
        return gmtFireMillis;
    }

    /**
     * 设置期望调度时间
     *
     * @param gmtFireMillis
     *     毫秒时间戳
     */
    public void setGmtFireMillis(Long gmtFireMillis) {
        this.gmtFireMillis = gmtFireMillis;
    }

    /**
     * 获取优先级
     *
     * @return 优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 设置优先级
     *
     * @param priority
     *     优先级
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * 获取任务级业务数据
     *
     * @return 业务数据 Map
     */
    public Map<String, Object> getContext() {
        return context;
    }

    /**
     * 设置任务级业务数据
     *
     * @param context
     *     业务数据 Map
     */
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public Integer getMaxRetry() {
        return maxRetry;
    }

    /**
     * 设置最大重试次数
     *
     * @param maxRetry
     *     最大重试次数
     */
    public void setMaxRetry(Integer maxRetry) {
        this.maxRetry = maxRetry;
    }

    /**
     * 获取重试间隔策略 code
     *
     * @return 策略 code
     */
    public String getRetryStrategyCode() {
        return retryStrategyCode;
    }

    /**
     * 设置重试间隔策略 code
     *
     * @param retryStrategyCode
     *     策略 code
     */
    public void setRetryStrategyCode(String retryStrategyCode) {
        this.retryStrategyCode = retryStrategyCode;
    }
}
