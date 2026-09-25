package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.dao.mapper.ModelMapper;
import com.dayu.yiyibushe.dao.mybatis.ModelMybatisMapper;
import com.dayu.yiyibushe.dao.po.ModelPO;
import com.dayu.yiyibushe.infra.ai.core.ModelType;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 模型 Mapper 的 MySQL 实现，负责 {@link ModelDefinition} 与 {@link ModelPO} 的双向转换，
 * 新增时由 {@link IdUtil} 分配业务ID。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlModelMapper implements ModelMapper {

    /** 默认版本号 */
    private static final int DEFAULT_VERSION = 1;

    /** 启用标识：是 */
    private static final int FLAG_TRUE = 1;

    /** 启用标识：否 */
    private static final int FLAG_FALSE = 0;

    @Autowired
    private ModelMybatisMapper modelMybatisMapper;

    @Override
    public ModelDefinition selectByModelCode(String modelCode) {
        if (Objects.isNull(modelCode)) {
            return null;
        }
        return toDefinition(modelMybatisMapper.selectByModelCode(modelCode));
    }

    @Override
    public List<ModelDefinition> selectAll() {
        return CollectionUtilExt.mapToList(modelMybatisMapper.selectAll(), this::toDefinition);
    }

    @Override
    public int insert(ModelDefinition modelDefinition) {
        if (Objects.isNull(modelDefinition)) {
            throw new BizException(ParamErrorCode.MODEL_DEF_BLANK);
        }
        modelDefinition.setModelId(IdUtil.nextId());
        if (Objects.isNull(modelDefinition.getVersion())) {
            modelDefinition.setVersion(DEFAULT_VERSION);
        }
        return modelMybatisMapper.insert(toPO(modelDefinition));
    }

    @Override
    public int updateByModelId(ModelDefinition modelDefinition) {
        if (Objects.isNull(modelDefinition) || Objects.isNull(modelDefinition.getModelId())) {
            throw new BizException(ParamErrorCode.MODEL_DEF_BLANK);
        }
        return modelMybatisMapper.updateByModelId(toPO(modelDefinition));
    }

    /**
     * 领域定义转 PO
     *
     * @param definition 模型定义
     * @return 模型 PO
     */
    private ModelPO toPO(ModelDefinition definition) {
        ModelPO modelPO = new ModelPO();
        modelPO.setModelId(definition.getModelId());
        modelPO.setModelCode(definition.getCode());
        modelPO.setDisplayName(definition.getDisplayName());
        modelPO.setProvider(definition.getProvider());
        if (Objects.nonNull(definition.getModelType())) {
            modelPO.setModelType(definition.getModelType().name());
        }
        modelPO.setProtocol(definition.getProtocol());
        modelPO.setBaseUrl(definition.getBaseUrl());
        modelPO.setChatPath(definition.getChatPath());
        modelPO.setVendorModelName(definition.getModelName());
        modelPO.setAsyncFlag(definition.isAsync() ? FLAG_TRUE : FLAG_FALSE);
        modelPO.setPollIntervalMs(definition.getPollIntervalMs());
        modelPO.setPollTimeoutMs(definition.getPollTimeoutMs());
        modelPO.setContextWindow(definition.getContextWindow());
        modelPO.setMaxOutputTokens(definition.getMaxOutputTokens());
        modelPO.setInputModalities(definition.getInputModalities());
        modelPO.setOutputModalities(definition.getOutputModalities());
        modelPO.setSupportedParams(definition.getSupportedParams());
        modelPO.setDefaultParams(definition.getDefaultParams());
        modelPO.setIcon(definition.getIcon());
        modelPO.setCategory(definition.getCategory());
        modelPO.setTags(definition.getTags());
        modelPO.setPublishStatus(definition.getPublishStatus());
        modelPO.setRuntimeStatus(definition.getRuntimeStatus());
        modelPO.setVisibility(definition.getVisibility());
        modelPO.setSourceType(definition.getSourceType());
        modelPO.setSourceRef(definition.getSourceRef());
        modelPO.setOwner(definition.getOwner());
        modelPO.setVersion(definition.getVersion());
        modelPO.setRemark(definition.getRemark());
        return modelPO;
    }

    /**
     * PO 转领域定义
     *
     * @param modelPO 模型 PO
     * @return 模型定义，入参为 null 时返回 null
     */
    private ModelDefinition toDefinition(ModelPO modelPO) {
        if (Objects.isNull(modelPO)) {
            return null;
        }
        ModelDefinition definition = new ModelDefinition();
        definition.setModelId(modelPO.getModelId());
        definition.setCode(modelPO.getModelCode());
        definition.setDisplayName(modelPO.getDisplayName());
        definition.setProvider(modelPO.getProvider());
        definition.setModelType(ModelType.valueOf(modelPO.getModelType()));
        definition.setProtocol(modelPO.getProtocol());
        definition.setBaseUrl(modelPO.getBaseUrl());
        definition.setChatPath(modelPO.getChatPath());
        definition.setModelName(modelPO.getVendorModelName());
        definition.setAsync(Objects.equals(modelPO.getAsyncFlag(), FLAG_TRUE));
        definition.setPollIntervalMs(modelPO.getPollIntervalMs());
        definition.setPollTimeoutMs(modelPO.getPollTimeoutMs());
        definition.setContextWindow(modelPO.getContextWindow());
        definition.setMaxOutputTokens(modelPO.getMaxOutputTokens());
        definition.setInputModalities(modelPO.getInputModalities());
        definition.setOutputModalities(modelPO.getOutputModalities());
        definition.setSupportedParams(modelPO.getSupportedParams());
        definition.setDefaultParams(modelPO.getDefaultParams());
        definition.setIcon(modelPO.getIcon());
        definition.setCategory(modelPO.getCategory());
        definition.setTags(modelPO.getTags());
        definition.setPublishStatus(modelPO.getPublishStatus());
        definition.setRuntimeStatus(modelPO.getRuntimeStatus());
        definition.setVisibility(modelPO.getVisibility());
        definition.setSourceType(modelPO.getSourceType());
        definition.setSourceRef(modelPO.getSourceRef());
        definition.setOwner(modelPO.getOwner());
        definition.setVersion(modelPO.getVersion());
        definition.setRemark(modelPO.getRemark());
        return definition;
    }
}
