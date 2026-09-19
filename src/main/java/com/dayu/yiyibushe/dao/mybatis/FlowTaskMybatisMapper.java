package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.infra.flowtask.FlowTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 说明：任务主表 flow_task 的 MyBatis XML Mapper（仅存任务自身，不含节点记录）。
 * <p>
 * 主键 id 无语义、由发号器生成（插入前外部赋值）；业务任务 ID 为 task_id，
 * 查询与更新一律按 task_id 进行；gmt_create 由数据库默认填充、gmt_modify 更新时自动刷新。
 * <p>
 * 节点执行记录独立落在 task_node 表，通过 task_id 关联、node_order 排序，
 * 因此本 Mapper 只负责 flow_task 这一行的增删改查。
 * 捞取/分发是调用方的职责（只捞 INIT 且到点的任务），框架不提供批量扫描查询。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Mapper
public interface FlowTaskMybatisMapper {

    /**
     * 新增任务（主键 id 与业务 task_id 均需在调用前由发号器赋值；时间列由数据库默认填充）
     *
     * @param task
     *             待新增任务
     * @return 受影响行数
     */
    int insert(FlowTask task);

    /**
     * 按业务任务 ID 更新任务（gmt_modify 由 ON UPDATE 自动刷新）
     *
     * @param task
     *             待更新任务
     * @return 受影响行数
     */
    int updateByTaskId(FlowTask task);

    /**
     * 按业务任务 ID 查询任务
     *
     * @param taskId
     *               业务任务 ID
     * @return 任务，不存在返回 null
     */
    FlowTask selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 清空全部任务（测试清理用）
     *
     * @return 受影响行数
     */
    int deleteAll();
}
