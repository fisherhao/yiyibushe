package com.dayu.yiyibushe.infra.flowtask;

import java.util.Map;
import java.util.Objects;

/**
 * 说明：任务节点模型（数据层），承载 task_node 表中的一行数据。
 * <p>
 * 与之相对，{@link TaskNodeAction} 是节点动作抽象基类（行为框架层），不可序列化；
 * 引擎执行时通过 {@link TaskNodeStrategy} 按任务类型取回动作链，
 * 与本模型按链顺序一一对应，从而支持断点重试：已 SUCCESS 的节点直接跳过。
 * <p>
 * 主键 id 无语义（由发号器生成），业务节点记录 ID 为 taskNodeId，两者并存；
 * 节点没有业务 ID，在链上的业务身份是 nodeType（节点类型，链内唯一）；
 * 重试次数（retryCount）与执行/重试时间统一由 flow_task 控制，本模型不保存。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.7
 */
public class TaskNode {

    /** 主键 ID（无业务语义，由发号器生成） */
    private Long id;

    /** 业务节点记录 ID（与主键 id 并存，对应 task_node 表 task_node_id） */
    private Long taskNodeId;

    /** 所属任务 ID */
    private Long taskId;

    /** 节点类型（与编排的 TaskNodeAction 实例对应，同一任务链内唯一） */
    private String nodeType;

    /** 节点顺序（从 0 开始，由编排数组的下标决定） */
    private int nodeOrder;

    /** 节点状态 */
    private TaskNodeStatus status = TaskNodeStatus.INIT;

    /** 节点输出（成功或待回调时写入） */
    private Object output;

    /** 失败原因（失败时写入） */
    private String failMessage;

    /** 本节点业务数据备份（业务方存取，框架只存不动；落库时由 TypeHandler 序列化为 JSON 文本） */
    private Map<String, Object> extInfo;

    /**
     * 默认构造器（反序列化使用）
     */
    public TaskNode() {
    }

    /**
     * 构造初始节点模型
     *
     * @param nodeType
     *                  节点类型
     * @param nodeOrder
     *                  节点顺序（从 0 开始）
     */
    private TaskNode(String nodeType, int nodeOrder) {
        this.nodeType = nodeType;
        this.nodeOrder = nodeOrder;
    }

    /**
     * 构造处于初始状态的节点模型
     *
     * @param nodeType
     *                  节点类型
     * @param nodeOrder
     *                  节点顺序（从 0 开始）
     * @return 初始节点模型
     */
    public static TaskNode init(String nodeType, int nodeOrder) {
        return new TaskNode(nodeType, nodeOrder);
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
     * 获取节点顺序
     *
     * @return 节点顺序（从 0 开始)
     */
    public int getNodeOrder() {
        return nodeOrder;
    }

    /**
     * 设置节点顺序
     *
     * @param nodeOrder
     *                  节点顺序（从 0 开始）
     */
    public void setNodeOrder(int nodeOrder) {
        this.nodeOrder = nodeOrder;
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
     * 获取节点状态
     *
     * @return 节点状态
     */
    public TaskNodeStatus getStatus() {
        return status;
    }

    /**
     * 设置节点状态
     *
     * @param status
     *               节点状态
     */
    public void setStatus(TaskNodeStatus status) {
        this.status = status;
    }

    /**
     * 获取节点输出
     *
     * @return 节点输出，可能为 null
     */
    public Object getOutput() {
        return output;
    }

    /**
     * 设置节点输出
     *
     * @param output
     *               节点输出
     */
    public void setOutput(Object output) {
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
     * 判断节点是否处于待回调状态
     *
     * @return true 表示待回调
     */
    public boolean isWaitingCallback() {
        return status == TaskNodeStatus.WAIT;
    }

    /**
     * 获取本节点业务数据备份
     *
     * @return 业务方存入的数据（框架只存不动）
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
     * 输出调试信息
     *
     * @return 调试字符串
     */
    @Override
    public String toString() {
        return "TaskNode{nodeType=" + nodeType + ", status=" + status
                + ", output=" + output + ", failMessage=" + failMessage + "}";
    }

    /**
     * 判断节点是否属于指定节点类型
     *
     * @param targetNodeType
     *                       目标节点类型
     * @return true 表示是同一节点
     */
    public boolean matchesNode(String targetNodeType) {
        return Objects.nonNull(targetNodeType) && targetNodeType.equals(nodeType);
    }
}
