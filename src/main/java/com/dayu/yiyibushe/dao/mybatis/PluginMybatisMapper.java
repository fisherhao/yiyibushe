package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.PluginPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 插件表 ai_plugin 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface PluginMybatisMapper {

    /**
     * 按插件编码查询最高版本记录
     *
     * @param pluginCode 插件编码
     * @return 插件 PO，不存在返回 null
     */
    PluginPO selectByPluginCode(@Param("pluginCode") String pluginCode);

    /**
     * 查询全部插件
     *
     * @return 插件 PO 列表
     */
    List<PluginPO> selectAll();

    /**
     * 新增插件（业务ID外部赋值）
     *
     * @param pluginPO 插件 PO
     * @return 受影响行数
     */
    int insert(PluginPO pluginPO);

    /**
     * 按业务ID更新插件
     *
     * @param pluginPO 插件 PO
     * @return 受影响行数
     */
    int updateByPluginId(PluginPO pluginPO);
}
