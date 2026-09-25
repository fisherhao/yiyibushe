package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.infra.ai.definition.SkillDefinition;

import java.util.List;

/**
 * AI 技能数据访问接口。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface SkillMapper {

    /**
     * 按技能编码查询最高版本定义
     *
     * @param skillCode 技能编码
     * @return 技能定义，不存在返回 null
     */
    SkillDefinition selectBySkillCode(String skillCode);

    /**
     * 查询全部技能
     *
     * @return 技能定义列表
     */
    List<SkillDefinition> selectAll();

    /**
     * 新增技能
     *
     * @param skillDefinition 技能定义
     * @return 影响行数
     */
    int insert(SkillDefinition skillDefinition);

    /**
     * 按业务ID更新技能
     *
     * @param skillDefinition 技能定义
     * @return 影响行数
     */
    int updateBySkillId(SkillDefinition skillDefinition);
}
