package com.dayu.yiyibushe.dao.mybatis;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 说明：节点执行记录持久化对象（task_node 表）。
 * <p>
 * 与 flow_task 主表构成「任务 + 节点执行记录」双表模型：通过 task_id 关联所属任务，
 * 通过 node_order 排序决定执行顺序。主键 id 无语义、由发号器生成（非自增），
 * 业务节点记录 ID 为 task_node_id，两者并存；
 * gmt_create 由数据库默认填充，gmt_modify 更新时数据库自动刷新。
 * <p>
 * 节点的执行/重试时间统一由 flow_task 的 gmt_fire 控制，本表不保存等待时间字段。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
public class TaskNodePO implements Serializable {

    private static final long serialVersionUID = 6183740598217364921L;

    /** 主键 ID（无语义，由发号器生成，对应列 id） */
    private Long id;

    /** 业务节点记录 ID（与主键 id 并存，对应列 task_node_id） */
    private Long taskNodeId;

    /** 所属任务 ID（业务任务 ID，关联 flow_task.task_id） */
    private Long taskId;

    /** 节点类型（与编排的 TaskNodeAction 实例对应，同一任务链内唯一，对应列 node_type） */
    private String nodeType;

    /** 节点顺序（从 0 开始，决定执行顺序） */
    private Integer nodeOrder;

    /** 节点状态：INIT/RUNNING/WAIT/SUCCESS/FAILED */
    private String status;

    /** 节点输出（成功或待回调时写入，JSON 列） */
    private Map<String, Object> output;

    /** 失败原因（失败时写入） */
    private String failMessage;

    /** 本节点业务数据备份（业务方存取，框架只存不动；落库时由 TypeHandler 序列化为 JSON 文本） */
    private Map<String, Object> extInfo;

    /** 创建时间（数据库默认填充） */
    private LocalDateTime gmtCreate;

    /** 更新时间（更新时数据库自动刷新） */
    private LocalDateTime gmtModify;

    /**
     * 无参构造器（反序列化使用）
     */
    public TaskNodePO() {
    }

    /**
     * 获取主键 ID
     *
     * @return 主键 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键 ID
     *
     * @param id
     *           主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取业务节点记录 ID
     *
     * @return 业务节点记录 ID
     */
    public Long getTaskNodeId() {
        return taskNodeId;
    }

    /**
     * 设置业务节点记录 ID
     *
     * @param taskNodeId
     *                   业务节点记录 ID
     */
    public void setTaskNodeId(Long taskNodeId) {
        this.taskNodeId = taskNodeId;
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
     * 设置所属任务 ID
     *
     * @param taskId
     *               任务 ID
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * 获取节点类型
     *
     * @return 节点类型
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * 设置节点类型
     *
     * @param nodeType
     *                 节点类型
     */
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * 获取节点顺序
     *
     * @return 节点顺序（从 0 开始）
     */
    public Integer getNodeOrder() {
        return nodeOrder;
    }

    /**
     * 设置节点顺序
     *
     * @param nodeOrder
     *                  节点顺序（从 0 开始）
     */
    public void setNodeOrder(Integer nodeOrder) {
        this.nodeOrder = nodeOrder;
    }

    /**
     * 获取节点状态
     *
     * @return 节点状态文案
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置节点状态
     *
     * @param status
     *               节点状态文案
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取节点输出
     *
     * @return 节点输出 Map，可能为 null
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * 设置节点输出
     *
     * @param output
     *               节点输出 Map
     */
    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    /**
     * 获取失败原因
     *
     * @return 失败原因，可能为 null
     */
    public String getFailMessage() {
        return failMessage;
    }

    /**
     * 设置失败原因
     *
     * @param failMessage
     *                    失败原因
     */
    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    /**
     * 获取本节点业务数据备份
     *
     * @return 业务数据
     */
    public Map<String, Object> getExtInfo() {
        return extInfo;
    }

    /**
     * 设置本节点业务数据备份
     *
     * @param extInfo
     *                业务数据
     */
    public void setExtInfo(Map<String, Object> extInfo) {
        this.extInfo = extInfo;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 设置创建时间
     *
     * @param gmtCreate
     *                  创建时间
     */
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    public LocalDateTime getGmtModify() {
        return gmtModify;
    }

    /**
     * 设置更新时间
     *
     * @param gmtModify
     *                  更新时间
     */
    public void setGmtModify(LocalDateTime gmtModify) {
        this.gmtModify = gmtModify;
    }
}
