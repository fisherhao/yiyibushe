package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.TargetMountPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 能力挂载表 ai_target_mount 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface TargetMountMybatisMapper {

    /**
     * 按挂载对象查询全部启用中的挂载（按排序号升序）
     *
     * @param targetType 对象类型 AGENT/FLOW_NODE
     * @param targetId   对象业务ID
     * @return 挂载 PO 列表
     */
    List<TargetMountPO> selectByTarget(@Param("targetType") String targetType,
                                       @Param("targetId") Long targetId);

    /**
     * 新增挂载（业务ID外部赋值）
     *
     * @param targetMountPO 挂载 PO
     * @return 受影响行数
     */
    int insert(TargetMountPO targetMountPO);

    /**
     * 按挂载业务ID更新状态
     *
     * @param mountId 挂载业务ID
     * @param status  新状态 ENABLED/DISABLED
     * @return 受影响行数
     */
    int updateStatusByMountId(@Param("mountId") Long mountId,
                              @Param("status") String status);
}
