package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.dao.po.PluginInstallPO;

import java.util.List;

/**
 * AI 插件安装实例数据访问接口，直接以 PO 承载（安装记录无独立领域模型）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface PluginInstallMapper {

    /**
     * 按安装业务ID查询
     *
     * @param installId 安装业务ID
     * @return 安装 PO，不存在返回 null
     */
    PluginInstallPO selectByInstallId(Long installId);

    /**
     * 按安装范围查询
     *
     * @param scopeType 范围类型 WORKSPACE/USER
     * @param scopeId   范围业务ID
     * @return 安装 PO 列表
     */
    List<PluginInstallPO> selectByScope(String scopeType, Long scopeId);

    /**
     * 新增安装实例
     *
     * @param pluginInstallPO 安装 PO
     * @return 影响行数
     */
    int insert(PluginInstallPO pluginInstallPO);

    /**
     * 按安装业务ID更新状态
     *
     * @param installId     安装业务ID
     * @param installStatus 新状态
     * @return 影响行数
     */
    int updateStatusByInstallId(Long installId, String installStatus);
}
