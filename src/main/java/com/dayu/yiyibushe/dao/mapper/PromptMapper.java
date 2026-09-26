package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.infra.ai.definition.PromptDefinition;

import java.util.List;

/**
 * AI 提示词数据访问接口。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface PromptMapper {

    /**
     * 查询全部提示词定义
     *
     * @return 提示词定义列表
     */
    List<PromptDefinition> selectAll();

    /**
     * 按提示词编码查询
     *
     * @param promptCode 提示词编码
     * @return 提示词定义，不存在返回 null
     */
    PromptDefinition selectByPromptCode(String promptCode);

    /**
     * 新增提示词
     *
     * @param promptDefinition 提示词定义
     * @return 影响行数
     */
    int insert(PromptDefinition promptDefinition);
}
