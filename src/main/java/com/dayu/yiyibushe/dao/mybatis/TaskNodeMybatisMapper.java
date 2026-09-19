package com.dayu.yiyibushe.dao.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 说明：节点执行记录表 task_node 的 MyBatis XML Mapper。
 * <p>
 * task_node 通过 task_id 关联所属任务，通过 node_order 排序决定执行顺序；
 * 与 flow_task 主表构成「任务 + 节点执行记录」双表模型。
 * 主键 id 无语义、由发号器生成；业务节点记录 ID 为 task_node_id，更新按它进行。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Mapper
public interface TaskNodeMybatisMapper {

    /**
     * 新增节点执行记录（主键 id 与业务 task_node_id 均已由发号器在外部赋值；时间列由数据库默认填充）
     *
     * @param nodePO
     *               待新增节点记录
     * @return 受影响行数
     */
    int insert(TaskNodePO nodePO);

    /**
     * 按业务节点记录 ID 更新节点执行记录（gmt_modify 由 ON UPDATE 自动刷新）
     *
     * @param nodePO
     *               待更新节点记录
     * @return 受影响行数
     */
    int updateByTaskNodeId(TaskNodePO nodePO);

    /**
     * 按所属任务 ID 查询全部节点记录（按 node_order 升序排序，即执行顺序）
     *
     * @param taskId
     *               业务任务 ID
     * @return 节点记录列表
     */
    List<TaskNodePO> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 清空全部节点记录（测试清理用）
     *
     * @return 受影响行数
     */
    int deleteAll();
}
