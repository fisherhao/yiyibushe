package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.infra.ai.definition.PluginDefinition;

import java.util.List;

/**
 * AI 插件数据访问接口。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface PluginMapper {

    /**
     * 按插件编码查询最高版本定义
     *
     * @param pluginCode 插件编码
     * @return 插件定义，不存在返回 null
     */
    PluginDefinition selectByPluginCode(String pluginCode);

    /**
     * 查询全部插件
     *
     * @return 插件定义列表
     */
    List<PluginDefinition> selectAll();

    /**
     * 新增插件
     *
     * @param pluginDefinition 插件定义
     * @return 影响行数
     */
    int insert(PluginDefinition pluginDefinition);

    /**
     * 按业务ID更新插件
     *
     * @param pluginDefinition 插件定义
     * @return 影响行数
     */
    int updateByPluginId(PluginDefinition pluginDefinition);
}
