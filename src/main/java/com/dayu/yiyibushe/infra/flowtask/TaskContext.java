package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.common.util.CollectionUtilExt;

import java.util.Map;

/**
 * 说明：任务执行上下文，节点之间传递数据的载体。
 * <p>
 * 底层就是 {@link FlowTask#getTaskContext()} 那份 Map（同一引用，改动会随任务持久化）：
 * 既存任务类型与业务数据（创建任务时写入），也存已完成节点的输出（nodeType -> output），
 * 断点续跑/重启恢复时引擎直接从库里拿，不靠内存重建。
 * 任务类型（taskType）只在这份上下文里存一份：知道了 taskType 就能取到节点链，
 * 任务表与节点动作都不再单独维护。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public class TaskContext {

    /** 任务类型在上下文中的键（创建任务时由模板写入，取链的唯一依据） */
    public static final String KEY_TASK_TYPE = "taskType";

    /** 所属任务 ID */
    private Long taskId;

    /** 任务执行上下文（业务数据 + 节点输出，与 FlowTask.taskContext 同一份引用） */
    private Map<String, Object> taskContext;

    /**
     * 静态工厂：创建任务上下文
     *
     * @param taskId
     *                    所属任务 ID
     * @param taskContext
     *                    任务执行上下文
     * @return 任务上下文
     */
    public static TaskContext create(Long taskId, Map<String, Object> taskContext) {
        TaskContext context = new TaskContext();
        context.taskId = taskId;
        context.taskContext = taskContext;
        return context;
    }

    /**
     * 获取所属任务 ID
     *
     * @return 任务 ID
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * 获取任务执行上下文 Map（节点可直接读写，改动会随任务持久化）
     *
     * @return 上下文 Map
     */
    public Map<String, Object> getTaskContext() {
        return taskContext;
    }

    /**
     * 写入执行上下文数据
     *
     * @param key
     *              键
     * @param value
     *              值
     */
    public void put(String key, Object value) {
        taskContext.put(key, value);
    }

    /**
     * 读取执行上下文数据（原始类型）
     *
     * @param key
     *            键
     * @return 值，不存在返回 null
     */
    public Object get(String key) {
        return CollectionUtilExt.getObject(taskContext, key);
    }

    /**
     * 读取 String 型数据
     *
     * @param key
     *            键
     * @return 值，不存在返回 null
     */
    public String getString(String key) {
        return CollectionUtilExt.getString(taskContext, key);
    }

    /**
     * 读取 Integer 型数据
     *
     * @param key
     *            键
     * @return 值，不存在返回 null
     */
    public Integer getInteger(String key) {
        return CollectionUtilExt.getInteger(taskContext, key);
    }

    /**
     * 读取任务类型（创建任务时由模板写入上下文）
     *
     * @return 任务类型
     */
    public String getTaskType() {
        return getString(KEY_TASK_TYPE);
    }

    /**
     * 获取指定节点的历史输出（断点续跑时读上游结果）
     *
     * @param nodeType
     *                 节点类型
     * @return 输出值，不存在返回 null
     */
    public Object getNodeOutput(String nodeType) {
        return CollectionUtilExt.getObject(taskContext, nodeType);
    }
}
