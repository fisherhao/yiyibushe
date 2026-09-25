package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.SkillPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 技能表 ai_skill 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface SkillMybatisMapper {

    /**
     * 按技能编码查询最高版本记录
     *
     * @param skillCode 技能编码
     * @return 技能 PO，不存在返回 null
     */
    SkillPO selectBySkillCode(@Param("skillCode") String skillCode);

    /**
     * 查询全部技能
     *
     * @return 技能 PO 列表
     */
    List<SkillPO> selectAll();

    /**
     * 新增技能（业务ID外部赋值）
     *
     * @param skillPO 技能 PO
     * @return 受影响行数
     */
    int insert(SkillPO skillPO);

    /**
     * 按业务ID更新技能
     *
     * @param skillPO 技能 PO
     * @return 受影响行数
     */
    int updateBySkillId(SkillPO skillPO);
}
