package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.dao.mapper.PromptMapper;
import com.dayu.yiyibushe.dao.mybatis.PromptMybatisMapper;
import com.dayu.yiyibushe.dao.po.PromptPO;
import com.dayu.yiyibushe.infra.ai.definition.PromptDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 提示词 Mapper 的 MySQL 实现，负责领域定义与 PO 的双向转换。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlPromptMapper implements PromptMapper {

    /** 默认版本号 */
    private static final int DEFAULT_VERSION = 1;

    /** 默认状态 */
    private static final String DEFAULT_STATUS = "ACTIVE";

    @Autowired
    private PromptMybatisMapper promptMybatisMapper;

    @Override
    public List<PromptDefinition> selectAll() {
        return CollectionUtilExt.toStream(promptMybatisMapper.selectAll())
                .map(this::toDefinition)
                .toList();
    }

    @Override
    public PromptDefinition selectByPromptCode(String promptCode) {
        if (Objects.isNull(promptCode)) {
            return null;
        }
        return toDefinition(promptMybatisMapper.selectByPromptCode(promptCode));
    }

    @Override
    public List<PromptDefinition> selectByCategory(String category) {
        if (Objects.isNull(category)) {
            return selectAll();
        }
        return CollectionUtilExt.toStream(promptMybatisMapper.selectByCategory(category))
                .map(this::toDefinition)
                .toList();
    }

    @Override
    public int insert(PromptDefinition promptDefinition) {
        if (Objects.isNull(promptDefinition)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        if (Objects.isNull(promptDefinition.getVersion())) {
            promptDefinition.setVersion(DEFAULT_VERSION);
        }
        if (Objects.isNull(promptDefinition.getStatus())) {
            promptDefinition.setStatus(DEFAULT_STATUS);
        }
        return promptMybatisMapper.insert(toPO(promptDefinition));
    }

    @Override
    public int updateContent(String promptCode, String content) {
        if (Objects.isNull(promptCode) || Objects.isNull(content)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        return promptMybatisMapper.updateContent(promptCode, content);
    }

    @Override
    public int updateStatus(String promptCode, String status) {
        if (Objects.isNull(promptCode) || Objects.isNull(status)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        return promptMybatisMapper.updateStatus(promptCode, status);
    }

    /**
     * 领域定义转 PO
     *
     * @param definition 提示词定义
     * @return 提示词 PO
     */
    private PromptPO toPO(PromptDefinition definition) {
        PromptPO promptPO = new PromptPO();
        promptPO.setPromptCode(definition.getPromptCode());
        promptPO.setPromptName(definition.getPromptName());
        promptPO.setCategory(definition.getCategory());
        promptPO.setContent(definition.getContent());
        promptPO.setStatus(definition.getStatus());
        promptPO.setVersion(definition.getVersion());
        promptPO.setRemark(definition.getRemark());
        return promptPO;
    }

    /**
     * PO 转领域定义
     *
     * @param promptPO 提示词 PO
     * @return 提示词定义，入参为 null 时返回 null
     */
    private PromptDefinition toDefinition(PromptPO promptPO) {
        if (Objects.isNull(promptPO)) {
            return null;
        }
        PromptDefinition definition = new PromptDefinition();
        definition.setPromptCode(promptPO.getPromptCode());
        definition.setPromptName(promptPO.getPromptName());
        definition.setCategory(promptPO.getCategory());
        definition.setContent(promptPO.getContent());
        definition.setStatus(promptPO.getStatus());
        definition.setVersion(promptPO.getVersion());
        definition.setRemark(promptPO.getRemark());
        return definition;
    }
}
