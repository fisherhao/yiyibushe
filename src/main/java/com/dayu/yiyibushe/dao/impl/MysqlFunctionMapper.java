package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.dao.mapper.FunctionMapper;
import com.dayu.yiyibushe.dao.mybatis.FunctionMybatisMapper;
import com.dayu.yiyibushe.dao.po.FunctionPO;
import com.dayu.yiyibushe.infra.ai.definition.FunctionDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 函数 Mapper 的 MySQL 实现，负责领域定义与 PO 的双向转换。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlFunctionMapper implements FunctionMapper {

    /** 默认版本号 */
    private static final int DEFAULT_VERSION = 1;

    @Autowired
    private FunctionMybatisMapper functionMybatisMapper;

    @Override
    public FunctionDefinition selectByFunctionCode(String functionCode) {
        if (Objects.isNull(functionCode)) {
            return null;
        }
        return toDefinition(functionMybatisMapper.selectByFunctionCode(functionCode));
    }

    @Override
    public List<FunctionDefinition> selectAll() {
        return CollectionUtilExt.mapToList(functionMybatisMapper.selectAll(), this::toDefinition);
    }

    @Override
    public int insert(FunctionDefinition functionDefinition) {
        if (Objects.isNull(functionDefinition)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        functionDefinition.setFunctionId(IdUtil.nextId());
        if (Objects.isNull(functionDefinition.getVersion())) {
            functionDefinition.setVersion(DEFAULT_VERSION);
        }
        return functionMybatisMapper.insert(toPO(functionDefinition));
    }

    /**
     * 领域定义转 PO
     *
     * @param definition 函数定义
     * @return 函数 PO
     */
    private FunctionPO toPO(FunctionDefinition definition) {
        FunctionPO functionPO = new FunctionPO();
        functionPO.setFunctionId(definition.getFunctionId());
        functionPO.setFunctionCode(definition.getFunctionCode());
        functionPO.setFunctionName(definition.getFunctionName());
        functionPO.setDescription(definition.getDescription());
        functionPO.setExecutorType(definition.getExecutorType());
        functionPO.setExecutorConfig(definition.getExecutorConfig());
        functionPO.setOutputSchema(definition.getOutputSchema());
        functionPO.setStatus(definition.getStatus());
        functionPO.setVersion(definition.getVersion());
        functionPO.setRemark(definition.getRemark());
        return functionPO;
    }

    /**
     * PO 转领域定义
     *
     * @param functionPO 函数 PO
     * @return 函数定义，入参为 null 时返回 null
     */
    private FunctionDefinition toDefinition(FunctionPO functionPO) {
        if (Objects.isNull(functionPO)) {
            return null;
        }
        FunctionDefinition definition = new FunctionDefinition();
        definition.setFunctionId(functionPO.getFunctionId());
        definition.setFunctionCode(functionPO.getFunctionCode());
        definition.setFunctionName(functionPO.getFunctionName());
        definition.setDescription(functionPO.getDescription());
        definition.setExecutorType(functionPO.getExecutorType());
        definition.setExecutorConfig(functionPO.getExecutorConfig());
        definition.setOutputSchema(functionPO.getOutputSchema());
        definition.setStatus(functionPO.getStatus());
        definition.setVersion(functionPO.getVersion());
        definition.setRemark(functionPO.getRemark());
        return definition;
    }
}
