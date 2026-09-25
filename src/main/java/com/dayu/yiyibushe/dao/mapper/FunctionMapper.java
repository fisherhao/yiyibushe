package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.infra.ai.definition.FunctionDefinition;

import java.util.List;

/**
 * AI 函数数据访问接口。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface FunctionMapper {

    /**
     * 按函数编码查询
     *
     * @param functionCode 函数编码
     * @return 函数定义，不存在返回 null
     */
    FunctionDefinition selectByFunctionCode(String functionCode);

    /**
     * 查询全部函数
     *
     * @return 函数定义列表
     */
    List<FunctionDefinition> selectAll();

    /**
     * 新增函数
     *
     * @param functionDefinition 函数定义
     * @return 影响行数
     */
    int insert(FunctionDefinition functionDefinition);
}
