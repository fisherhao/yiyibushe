package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.dao.mapper.SkillMapper;
import com.dayu.yiyibushe.dao.mybatis.SkillMybatisMapper;
import com.dayu.yiyibushe.dao.po.SkillPO;
import com.dayu.yiyibushe.infra.ai.definition.SkillDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 技能 Mapper 的 MySQL 实现，负责领域定义与 PO 的双向转换。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlSkillMapper implements SkillMapper {

    /** 默认版本号 */
    private static final int DEFAULT_VERSION = 1;

    /** 默认累计调用次数 */
    private static final long DEFAULT_USAGE_COUNT = 0L;

    @Autowired
    private SkillMybatisMapper skillMybatisMapper;

    @Override
    public SkillDefinition selectBySkillCode(String skillCode) {
        if (Objects.isNull(skillCode)) {
            return null;
        }
        return toDefinition(skillMybatisMapper.selectBySkillCode(skillCode));
    }

    @Override
    public List<SkillDefinition> selectAll() {
        return skillMybatisMapper.selectAll()
                .stream()
                .map(this::toDefinition)
                .toList();
    }

    @Override
    public int insert(SkillDefinition skillDefinition) {
        if (Objects.isNull(skillDefinition)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        skillDefinition.setSkillId(IdUtil.nextId());
        if (Objects.isNull(skillDefinition.getVersion())) {
            skillDefinition.setVersion(DEFAULT_VERSION);
        }
        if (Objects.isNull(skillDefinition.getUsageCount())) {
            skillDefinition.setUsageCount(DEFAULT_USAGE_COUNT);
        }
        return skillMybatisMapper.insert(toPO(skillDefinition));
    }

    @Override
    public int updateBySkillId(SkillDefinition skillDefinition) {
        if (Objects.isNull(skillDefinition) || Objects.isNull(skillDefinition.getSkillId())) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        return skillMybatisMapper.updateBySkillId(toPO(skillDefinition));
    }

    /**
     * 领域定义转 PO
     *
     * @param definition 技能定义
     * @return 技能 PO
     */
    private SkillPO toPO(SkillDefinition definition) {
        SkillPO skillPO = new SkillPO();
        skillPO.setSkillId(definition.getSkillId());
        skillPO.setSkillCode(definition.getSkillCode());
        skillPO.setDisplayName(definition.getDisplayName());
        skillPO.setDescription(definition.getDescription());
        skillPO.setInstructions(definition.getInstructions());
        skillPO.setTools(definition.getTools());
        skillPO.setTriggerExamples(definition.getTriggerExamples());
        skillPO.setCategory(definition.getCategory());
        skillPO.setTags(definition.getTags());
        skillPO.setUsageCount(definition.getUsageCount());
        skillPO.setLicense(definition.getLicense());
        skillPO.setCompatibility(definition.getCompatibility());
        skillPO.setMetadataJson(definition.getMetadataJson());
        skillPO.setSortNo(definition.getSortNo());
        skillPO.setPublishStatus(definition.getPublishStatus());
        skillPO.setRuntimeStatus(definition.getRuntimeStatus());
        skillPO.setVersion(definition.getVersion());
        skillPO.setRemark(definition.getRemark());
        return skillPO;
    }

    /**
     * PO 转领域定义
     *
     * @param skillPO 技能 PO
     * @return 技能定义，入参为 null 时返回 null
     */
    private SkillDefinition toDefinition(SkillPO skillPO) {
        if (Objects.isNull(skillPO)) {
            return null;
        }
        SkillDefinition definition = new SkillDefinition();
        definition.setSkillId(skillPO.getSkillId());
        definition.setSkillCode(skillPO.getSkillCode());
        definition.setDisplayName(skillPO.getDisplayName());
        definition.setDescription(skillPO.getDescription());
        definition.setInstructions(skillPO.getInstructions());
        definition.setTools(skillPO.getTools());
        definition.setTriggerExamples(skillPO.getTriggerExamples());
        definition.setCategory(skillPO.getCategory());
        definition.setTags(skillPO.getTags());
        definition.setUsageCount(skillPO.getUsageCount());
        definition.setLicense(skillPO.getLicense());
        definition.setCompatibility(skillPO.getCompatibility());
        definition.setMetadataJson(skillPO.getMetadataJson());
        definition.setSortNo(skillPO.getSortNo());
        definition.setPublishStatus(skillPO.getPublishStatus());
        definition.setRuntimeStatus(skillPO.getRuntimeStatus());
        definition.setVersion(skillPO.getVersion());
        definition.setRemark(skillPO.getRemark());
        return definition;
    }
}
