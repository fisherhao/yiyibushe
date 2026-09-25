package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.infra.ai.definition.ToolDefinition;

import java.util.List;

/**
 * AI 工具数据访问接口。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface ToolMapper {

    /**
     * 按工具编码查询最高版本定义
     *
     * @param toolCode 工具编码
     * @return 工具定义，不存在返回 null
     */
    ToolDefinition selectByToolCode(String toolCode);

    /**
     * 查询全部工具
     *
     * @return 工具定义列表
     */
    List<ToolDefinition> selectAll();

    /**
     * 按绑定函数业务ID查询工具
     *
     * @param functionId 函数业务ID
     * @return 工具定义列表
     */
    List<ToolDefinition> selectByFunctionId(Long functionId);

    /**
     * 新增工具
     *
     * @param toolDefinition 工具定义
     * @return 影响行数
     */
    int insert(ToolDefinition toolDefinition);

    /**
     * 按业务ID更新工具
     *
     * @param toolDefinition 工具定义
     * @return 影响行数
     */
    int updateByToolId(ToolDefinition toolDefinition);
}
