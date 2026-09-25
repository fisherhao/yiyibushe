package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.PluginInstallPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 插件安装实例表 ai_plugin_install 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface PluginInstallMybatisMapper {

    /**
     * 按安装业务ID查询
     *
     * @param installId 安装业务ID
     * @return 安装 PO，不存在返回 null
     */
    PluginInstallPO selectByInstallId(@Param("installId") Long installId);

    /**
     * 按安装范围查询全部实例
     *
     * @param scopeType 范围类型 WORKSPACE/USER
     * @param scopeId   范围业务ID
     * @return 安装 PO 列表
     */
    List<PluginInstallPO> selectByScope(@Param("scopeType") String scopeType,
                                        @Param("scopeId") Long scopeId);

    /**
     * 新增安装实例（业务ID外部赋值）
     *
     * @param installPO 安装 PO
     * @return 受影响行数
     */
    int insert(PluginInstallPO installPO);

    /**
     * 按安装业务ID更新状态
     *
     * @param installId     安装业务ID
     * @param installStatus 新状态
     * @return 受影响行数
     */
    int updateStatusByInstallId(@Param("installId") Long installId,
                                @Param("installStatus") String installStatus);
}
