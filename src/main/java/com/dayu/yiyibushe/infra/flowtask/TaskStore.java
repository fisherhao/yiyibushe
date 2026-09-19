package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.dao.mybatis.FlowTaskMybatisMapper;
import com.dayu.yiyibushe.dao.mybatis.TaskNodeMybatisMapper;
import com.dayu.yiyibushe.dao.mybatis.TaskNodePO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 说明：FlowTask 持久化仓库，MySQL 双表落库。
 * <ul>
 *   <li>flow_task 主表：存任务自身（任务类型/状态/优先级/调度时间/重试预算/上下文等）；</li>
 *   <li>task_node 节点记录表：任务每经过一个节点在此落一条执行记录，通过 task_id 关联所属任务。</li>
 * </ul>
 * 应用重启后依据 flow_task 主表的 currentNodeType 与 task_context 恢复断点，
 * 节点的执行记录从 task_node 表按 task_id 读取、按 node_order 排序得到执行顺序。
 * 主键 id 由数据库自增；业务 ID（task_id / task_node_id）由 {@link IdUtil} 发 18 位分段 ID，两者并存。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.6
 */
@Component
public class TaskStore {

    /** 发号说明：主键 id 由数据库自增；业务 ID（task_id / task_node_id）由 {@link IdUtil} 生成 18 位分段 ID */

    /** 任务主表 Mapper（XML 形式） */
    private final FlowTaskMybatisMapper flowTaskMybatisMapper;

    /** 节点记录表 Mapper（XML 形式） */
    private final TaskNodeMybatisMapper taskNodeMybatisMapper;

    /**
     * 构造器注入两个 Mapper
     *
     * @param flowTaskMybatisMapper
     *     任务主表 Mapper
     * @param taskNodeMybatisMapper
     *     节点记录表 Mapper
     */
    public TaskStore(FlowTaskMybatisMapper flowTaskMybatisMapper,
            TaskNodeMybatisMapper taskNodeMybatisMapper) {
        this.flowTaskMybatisMapper = flowTaskMybatisMapper;
        this.taskNodeMybatisMapper = taskNodeMybatisMapper;
    }

    /**
     * 新增或更新任务主表行：taskId 为空时由 {@link IdUtil} 发 18 位业务 ID（主键 id 由数据库自增），
     * 之后按业务 task_id upsert。<strong>只存 flow_task 这一行</strong>，
     * 节点记录由引擎按需调用 {@link #saveNode(TaskNode)} 单条落库，避免每次把整条链重写一遍。
     *
     * @param task
     *     待保存任务
     * @return 业务任务 ID（无论新增还是更新都返回，便于调用方直接使用）
     */
    @Transactional
    public Long save(FlowTask task) {
        Objects.requireNonNull(task, "任务不能为空");
        if (Objects.isNull(task.getTaskId())) {
            task.setTaskId(IdUtil.nextId());
            flowTaskMybatisMapper.insert(task);
        } else {
            flowTaskMybatisMapper.updateByTaskId(task);
        }
        return task.getTaskId();
    }

    /**
     * 单条落库一个节点记录：taskNodeId 为空则由 {@link IdUtil} 发 18 位业务 ID 并新增
     * （主键 id 由数据库自增），否则按 task_node_id 更新。
     * 引擎每处理一个节点只把<strong>发生变化的那一条</strong>传进来，不做全链重写。
     *
     * @param nodeRecord
     *     节点记录
     */
    @Transactional
    public void saveNode(TaskNode nodeRecord) {
        Objects.requireNonNull(nodeRecord, "节点记录不能为空");
        if (Objects.isNull(nodeRecord.getTaskNodeId())) {
            nodeRecord.setTaskNodeId(IdUtil.nextId());
            taskNodeMybatisMapper.insert(toTaskNodePO(nodeRecord));
        } else {
            taskNodeMybatisMapper.updateByTaskNodeId(toTaskNodePO(nodeRecord));
        }
    }

    /**
     * 按业务任务 ID 查询任务：加载 flow_task 主表行，再按 task_id 载入节点记录并按 node_order 排序
     *
     * @param taskId
     *     业务任务 ID
     * @return 任务，不存在返回 null
     */
    public FlowTask findByTaskId(Long taskId) {
        FlowTask task = flowTaskMybatisMapper.selectByTaskId(taskId);
        if (Objects.isNull(task)) {
            return null;
        }
        List<TaskNodePO> nodePOList = taskNodeMybatisMapper.selectByTaskId(taskId);
        task.setNodeRecords(CollectionUtilExt.mapToList(nodePOList, TaskStore::toNodeRecord));
        return task;
    }

    /**
     * 查询全部任务
     *
     * @return 任务列表
     */
    public List<FlowTask> findAll() {
        throw new UnsupportedOperationException("findAll 未实现：flow_task 全量查询见 FlowTaskMybatisMapper，如需遍历请自行扩展查询条件");
    }

    /**
     * 把节点记录实体转成 task_node 持久化对象
     *
     * @param nodeRecord
     *     节点记录
     * @return 持久化对象
     */
    private static TaskNodePO toTaskNodePO(TaskNode nodeRecord) {
        TaskNodePO nodePO = new TaskNodePO();
        nodePO.setId(nodeRecord.getId());
        nodePO.setTaskNodeId(nodeRecord.getTaskNodeId());
        nodePO.setTaskId(nodeRecord.getTaskId());
        nodePO.setNodeType(nodeRecord.getNodeType());
        nodePO.setNodeOrder(nodeRecord.getNodeOrder());
        nodePO.setStatus(nodeRecord.getStatus().name());
        nodePO.setFailMessage(nodeRecord.getFailMessage());
        Object output = nodeRecord.getOutput();
        if (Objects.nonNull(output)) {
            Map<String, Object> outputMap = new HashMap<>();
            outputMap.put("value", output);
            nodePO.setOutput(outputMap);
        }
        nodePO.setExtInfo(nodeRecord.getExtInfo());
        return nodePO;
    }

    /**
     * 把 task_node 持久化对象还原成节点记录实体
     *
     * @param nodePO
     *     持久化对象
     * @return 节点记录
     */
    private static TaskNode toNodeRecord(TaskNodePO nodePO) {
        TaskNode nodeRecord = TaskNode.init(nodePO.getNodeType(),
                nodePO.getNodeOrder() == null ? 0 : nodePO.getNodeOrder());
        nodeRecord.setId(nodePO.getId());
        nodeRecord.setTaskNodeId(nodePO.getTaskNodeId());
        nodeRecord.setTaskId(nodePO.getTaskId());
        nodeRecord.setStatus(TaskNodeStatus.valueOf(nodePO.getStatus()));
        nodeRecord.setFailMessage(nodePO.getFailMessage());
        Map<String, Object> outputMap = nodePO.getOutput();
        if (Objects.nonNull(outputMap)) {
            nodeRecord.setOutput(outputMap.get("value"));
        }
        nodeRecord.setExtInfo(nodePO.getExtInfo());
        return nodeRecord;
    }
}
