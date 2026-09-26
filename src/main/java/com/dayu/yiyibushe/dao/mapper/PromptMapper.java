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
     * 按分类查询提示词
     *
     * @param category 分类编码
     * @return 提示词定义列表
     */
    List<PromptDefinition> selectByCategory(String category);

    /**
     * 新增提示词
     *
     * @param promptDefinition 提示词定义
     * @return 影响行数
     */
    int insert(PromptDefinition promptDefinition);

    /**
     * 更新提示词内容并递增版本号（触发热更新）
     *
     * @param promptCode 提示词编码
     * @param content    新内容
     * @return 影响行数
     */
    int updateContent(String promptCode, String content);

    /**
     * 更新提示词状态（ACTIVE / INACTIVE）并递增版本号
     *
     * @param promptCode 提示词编码
     * @param status     新状态
     * @return 影响行数
     */
    int updateStatus(String promptCode, String status);
}
