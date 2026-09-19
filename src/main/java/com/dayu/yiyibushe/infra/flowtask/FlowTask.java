package com.dayu.yiyibushe.infra.flowtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 说明：flowtask 框架的任务主实体（持久化），一个任务对应一条有序节点链。
 * <p>
 * 核心属性：id（无语义主键，入库时由发号器分配）/ taskId（业务任务 ID，与主键并存）/
 * status / priority（优先级）/ gmtFire（调度时间，重试时改写为下次触发时间）/
 * gmtCreate / gmtModify / retryCount + maxRetry（重试预算）/
 * retryStrategyCode（重试间隔策略，空=全局默认）/ currentNodeType（当前执行到的节点类型，
 * 下次唤起就从这个节点继续）/ taskContext（任务执行上下文：taskType + 业务数据 + 节点输出，
 * 每次推进落库，下次唤起直接从库里拿；taskType 只存这里一份，不再单独成列）/
 * extInfo（业务方 JSON 备份，框架只存不动）/ nodeRecords（节点快照）。
 * <p>
 * 任务创建后立即入库；外部定时任务只捞 INIT 且到点的任务，
 * for 循环调用 {@code TaskEngine.trigger(taskId)} 执行；也支持 HTTP 手动触发。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.5
 */
public class FlowTask {

    /** 默认优先级（数值越小优先级越高） */
    public static final int DEFAULT_PRIORITY = 0;

    /** 主键 ID（无业务语义，对应列 id，入库前为 null） */
    private Long id;

    /** 业务任务 ID（与主键 id 并存，对应列 task_id，入库前为 null） */
    private Long taskId;

    /** 任务状态 */
    private FlowTaskStatus status = FlowTaskStatus.INIT;

    /** 优先级：数值越小越优先，默认 0 */
    private int priority = DEFAULT_PRIORITY;

    /** 期望调度时间（毫秒时间戳）；重试时被改写为"下次重试触发时间" */
    private long gmtFire;

    /** 创建时间（毫秒时间戳） */
    private long gmtCreate;

    /** 最后修改时间（毫秒时间戳） */
    private long gmtModify;

    /** 已重试次数（任务级，重试预算判断依据） */
    private int retryCount = 0;

    /** 最大重试次数（超过后任务进入终态 FAILED） */
    private int maxRetry = 0;

    /** 重试间隔策略 code：指定按哪个 RetryStrategy 计算等待时间，空则走全局默认策略 */
    private String retryStrategyCode;

    /** 当前执行到的节点类型（指向下一个要执行的节点，空串/链尾=已走完） */
    private String currentNodeType = "";

    /** 任务执行上下文（业务数据 + 节点输出，每次推进落库，下次唤起直接从库里拿） */
    private Map<String, Object> taskContext;

    /** 业务数据备份（业务方存取，框架只存不动；落库时由 TypeHandler 序列化为 JSON 文本） */
    private Map<String, Object> extInfo;

    /** 节点快照列表（与节点链按下标一一对应） */
    private List<TaskNode> nodeRecords = new ArrayList<>();

    /**
     * 默认构造器（反序列化使用）
     */
    public FlowTask() {
    }

    /**
     * 私有构造器，统一走 {@link #init(long, int, Map, int, String)}
     *
     * @param gmtFire
     *     期望调度时间
     * @param nowMillis
     *     当前时间
     * @param priority
     *     优先级
     * @param maxRetry
     *     最大重试次数
     * @param retryStrategyCode
     *     重试间隔策略 code（空=全局默认策略）
     */
    private FlowTask(long gmtFire, long nowMillis, int priority,
            int maxRetry, String retryStrategyCode) {
        this.gmtFire = gmtFire;
        this.gmtCreate = nowMillis;
        this.gmtModify = nowMillis;
        this.priority = priority;
        this.maxRetry = maxRetry;
        this.retryStrategyCode = retryStrategyCode;
    }

    /**
     * 创建处于 INIT 状态的任务（taskId 由仓库入库时分配）。
     * 任务类型由模板在创建时写入 taskContext（键见 {@link TaskContext#KEY_TASK_TYPE}），
     * 本实体不再单独维护。
     *
     * @param gmtFire
     *     期望调度时间（毫秒时间戳）
     * @param priority
     *     优先级（数值越小越优先）
     * @param taskContext
     *     任务执行上下文（taskType + 业务数据，节点输出由引擎推进时写入）
     * @param maxRetry
     *     最大重试次数
     * @param retryStrategyCode
     *     重试间隔策略 code（空=全局默认策略）
     * @return 新任务
     */
    public static FlowTask init(long gmtFire, int priority,
            Map<String, Object> taskContext, int maxRetry, String retryStrategyCode) {
        FlowTask task = new FlowTask(gmtFire, System.currentTimeMillis(), priority,
                maxRetry, retryStrategyCode);
        task.taskContext = taskContext;
        return task;
    }

    /**
     * 判断任务是否已到调度时间
     *
     * @param nowMillis
     *     当前时间（毫秒时间戳）
     * @return true 表示已到点可调度
     */
    public boolean isFireTimeReached(long nowMillis) {
        return gmtFire <= nowMillis;
    }

    /**
     * 判断任务是否还有重试预算（retryCount 未达 maxRetry）
     *
     * @return true 表示可以继续重试
     */
    public boolean hasRetryBudget() {
        return retryCount < maxRetry;
    }

    /**
     * 获取主键 ID
     *
     * @return 主键 ID（入库前为 null，无业务语义）
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键 ID
     *
     * @param id
     *     主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取业务任务 ID
     *
     * @return 业务任务 ID（入库前为 null）
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * 设置业务任务 ID
     *
     * @param taskId
     *     业务任务 ID
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * 获取任务状态
     *
     * @return 任务状态
     */
    public FlowTaskStatus getStatus() {
        return status;
    }

    /**
     * 设置任务状态
     *
     * @param status
     *     任务状态
     */
    public void setStatus(FlowTaskStatus status) {
        this.status = status;
    }

    /**
     * 获取优先级
     *
     * @return 优先级（数值越小越优先）
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 设置优先级
     *
     * @param priority
     *     优先级
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 获取期望调度时间
     *
     * @return 毫秒时间戳
     */
    public long getGmtFire() {
        return gmtFire;
    }

    /**
     * 设置期望调度时间
     *
     * @param gmtFire
     *     毫秒时间戳
     */
    public void setGmtFire(long gmtFire) {
        this.gmtFire = gmtFire;
    }

    /**
     * 获取创建时间
     *
     * @return 毫秒时间戳
     */
    public long getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 设置创建时间
     *
     * @param gmtCreate
     *     毫秒时间戳
     */
    public void setGmtCreate(long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 获取最后修改时间
     *
     * @return 毫秒时间戳
     */
    public long getGmtModify() {
        return gmtModify;
    }

    /**
     * 设置最后修改时间
     *
     * @param gmtModify
     *     毫秒时间戳
     */
    public void setGmtModify(long gmtModify) {
        this.gmtModify = gmtModify;
    }

    /**
     * 获取已重试次数
     *
     * @return 重试次数
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 设置已重试次数
     *
     * @param retryCount
     *     重试次数
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public int getMaxRetry() {
        return maxRetry;
    }

    /**
     * 设置最大重试次数
     *
     * @param maxRetry
     *     最大重试次数
     */
    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    /**
     * 获取重试间隔策略 code
     *
     * @return 策略 code（可能为空，表示走全局默认策略）
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

    /**
     * 获取当前执行到的节点类型
     *
     * @return 节点类型（指向下一个要执行的节点，空串表示未开始/已走完）
     */
    public String getCurrentNodeType() {
        return currentNodeType;
    }

    /**
     * 设置当前执行到的节点类型
     *
     * @param currentNodeType
     *     节点类型
     */
    public void setCurrentNodeType(String currentNodeType) {
        this.currentNodeType = currentNodeType;
    }

    /**
     * 获取任务执行上下文
     *
     * @return 上下文 Map（业务数据 + 节点输出）
     */
    public Map<String, Object> getTaskContext() {
        return taskContext;
    }

    /**
     * 设置任务执行上下文
     *
     * @param taskContext
     *     上下文 Map
     */
    public void setTaskContext(Map<String, Object> taskContext) {
        this.taskContext = taskContext;
    }

    /**
     * 获取业务数据备份
     *
     * @return 业务方存入的数据（框架只存不动）
     */
    public Map<String, Object> getExtInfo() {
        return extInfo;
    }

    /**
     * 设置业务数据备份
     *
     * @param extInfo
     *     业务数据
     */
    public void setExtInfo(Map<String, Object> extInfo) {
        this.extInfo = extInfo;
    }

    /**
     * 获取节点快照列表
     *
     * @return 节点快照列表
     */
    public List<TaskNode> getNodeRecords() {
        return nodeRecords;
    }

    /**
     * 设置节点快照列表
     *
     * @param nodeRecords
     *     节点快照列表
     */
    public void setNodeRecords(List<TaskNode> nodeRecords) {
        this.nodeRecords = nodeRecords;
    }

    /**
     * 输出调试信息（含状态、进度与各节点状态，便于排查流转问题）
     *
     * @return 调试字符串
     */
    @Override
    public String toString() {
        StringBuilder nodeSummary = new StringBuilder();
        for (TaskNode record : nodeRecords) {
            if (nodeSummary.length() > 0) {
                nodeSummary.append(",");
            }
            nodeSummary.append(record.getNodeType()).append(":").append(record.getStatus());
        }
        return "FlowTask{taskId=" + taskId + ", status=" + status
                + ", current=" + currentNodeType + ", retry=" + retryCount + "/" + maxRetry
                + ", nodes=[" + nodeSummary + "]}";
    }
}
