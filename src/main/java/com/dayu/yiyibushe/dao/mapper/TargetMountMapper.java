package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.dao.po.TargetMountPO;

import java.util.List;

/**
 * AI 能力挂载数据访问接口，直接以 PO 承载。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface TargetMountMapper {

    /**
     * 按挂载对象查询启用中的挂载
     *
     * @param targetType 对象类型 AGENT/FLOW_NODE
     * @param targetId   对象业务ID
     * @return 挂载 PO 列表
     */
    List<TargetMountPO> selectByTarget(String targetType, Long targetId);

    /**
     * 新增挂载
     *
     * @param targetMountPO 挂载 PO
     * @return 影响行数
     */
    int insert(TargetMountPO targetMountPO);

    /**
     * 按挂载业务ID更新状态
     *
     * @param mountId 挂载业务ID
     * @param status  新状态
     * @return 影响行数
     */
    int updateStatusByMountId(Long mountId, String status);
}
