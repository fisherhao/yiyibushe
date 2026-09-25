package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.dao.mapper.ToolMapper;
import com.dayu.yiyibushe.dao.mybatis.ToolMybatisMapper;
import com.dayu.yiyibushe.dao.po.ToolPO;
import com.dayu.yiyibushe.infra.ai.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 工具 Mapper 的 MySQL 实现，负责领域定义与 PO 的双向转换。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlToolMapper implements ToolMapper {

    /** 默认版本号 */
    private static final int DEFAULT_VERSION = 1;

    /** 默认累计调用次数 */
    private static final long DEFAULT_USAGE_COUNT = 0L;

    /** 布尔标记：是 */
    private static final int FLAG_TRUE = 1;

    /** 布尔标记：否 */
    private static final int FLAG_FALSE = 0;

    @Autowired
    private ToolMybatisMapper toolMybatisMapper;

    @Override
    public ToolDefinition selectByToolCode(String toolCode) {
        if (Objects.isNull(toolCode)) {
            return null;
        }
        return toDefinition(toolMybatisMapper.selectByToolCode(toolCode));
    }

    @Override
    public List<ToolDefinition> selectAll() {
        return CollectionUtilExt.mapToList(toolMybatisMapper.selectAll(), this::toDefinition);
    }

    @Override
    public List<ToolDefinition> selectByFunctionId(Long functionId) {
        if (Objects.isNull(functionId)) {
            return List.of();
        }
        return CollectionUtilExt.mapToList(toolMybatisMapper.selectByFunctionId(functionId),
                this::toDefinition);
    }

    @Override
    public int insert(ToolDefinition toolDefinition) {
        if (Objects.isNull(toolDefinition)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        toolDefinition.setToolId(IdUtil.nextId());
        if (Objects.isNull(toolDefinition.getVersion())) {
            toolDefinition.setVersion(DEFAULT_VERSION);
        }
        if (Objects.isNull(toolDefinition.getUsageCount())) {
            toolDefinition.setUsageCount(DEFAULT_USAGE_COUNT);
        }
        return toolMybatisMapper.insert(toPO(toolDefinition));
    }

    @Override
    public int updateByToolId(ToolDefinition toolDefinition) {
        if (Objects.isNull(toolDefinition) || Objects.isNull(toolDefinition.getToolId())) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        return toolMybatisMapper.updateByToolId(toPO(toolDefinition));
    }

    /**
     * 领域定义转 PO
     *
     * @param definition 工具定义
     * @return 工具 PO
     */
    private ToolPO toPO(ToolDefinition definition) {
        ToolPO toolPO = new ToolPO();
        toolPO.setToolId(definition.getToolId());
        toolPO.setToolCode(definition.getToolCode());
        toolPO.setFunctionId(definition.getFunctionId());
        toolPO.setDisplayName(definition.getDisplayName());
        toolPO.setDescription(definition.getDescription());
        toolPO.setInputSchema(definition.getInputSchema());
        toolPO.setProvides(definition.getProvides());
        toolPO.setRequires(definition.getRequires());
        toolPO.setDiscoverKeywords(definition.getDiscoverKeywords());
        toolPO.setCategory(definition.getCategory());
        toolPO.setTags(definition.getTags());
        toolPO.setUsageCount(definition.getUsageCount());
        toolPO.setConcurrencySafe(definition.isConcurrencySafe() ? FLAG_TRUE : FLAG_FALSE);
        toolPO.setReadOnly(definition.isReadOnly() ? FLAG_TRUE : FLAG_FALSE);
        toolPO.setExternalExecution(definition.isExternalExecution() ? FLAG_TRUE : FLAG_FALSE);
        toolPO.setDestructiveHint(definition.isDestructiveHint() ? FLAG_TRUE : FLAG_FALSE);
        toolPO.setIdempotentHint(definition.isIdempotentHint() ? FLAG_TRUE : FLAG_FALSE);
        toolPO.setTimeoutSeconds(definition.getTimeoutSeconds());
        toolPO.setRetryCount(definition.getRetryCount());
        toolPO.setCacheTtlSeconds(definition.getCacheTtlSeconds());
        toolPO.setIcon(definition.getIcon());
        toolPO.setPublishStatus(definition.getPublishStatus());
        toolPO.setRuntimeStatus(definition.getRuntimeStatus());
        toolPO.setVersion(definition.getVersion());
        toolPO.setRemark(definition.getRemark());
        return toolPO;
    }

    /**
     * PO 转领域定义
     *
     * @param toolPO 工具 PO
     * @return 工具定义，入参为 null 时返回 null
     */
    private ToolDefinition toDefinition(ToolPO toolPO) {
        if (Objects.isNull(toolPO)) {
            return null;
        }
        ToolDefinition definition = new ToolDefinition();
        definition.setToolId(toolPO.getToolId());
        definition.setToolCode(toolPO.getToolCode());
        definition.setFunctionId(toolPO.getFunctionId());
        definition.setDisplayName(toolPO.getDisplayName());
        definition.setDescription(toolPO.getDescription());
        definition.setInputSchema(toolPO.getInputSchema());
        definition.setProvides(toolPO.getProvides());
        definition.setRequires(toolPO.getRequires());
        definition.setDiscoverKeywords(toolPO.getDiscoverKeywords());
        definition.setCategory(toolPO.getCategory());
        definition.setTags(toolPO.getTags());
        definition.setUsageCount(toolPO.getUsageCount());
        definition.setConcurrencySafe(Objects.equals(toolPO.getConcurrencySafe(), FLAG_TRUE));
        definition.setReadOnly(Objects.equals(toolPO.getReadOnly(), FLAG_TRUE));
        definition.setExternalExecution(Objects.equals(toolPO.getExternalExecution(), FLAG_TRUE));
        definition.setDestructiveHint(Objects.equals(toolPO.getDestructiveHint(), FLAG_TRUE));
        definition.setIdempotentHint(Objects.equals(toolPO.getIdempotentHint(), FLAG_TRUE));
        definition.setTimeoutSeconds(toolPO.getTimeoutSeconds());
        definition.setRetryCount(toolPO.getRetryCount());
        definition.setCacheTtlSeconds(toolPO.getCacheTtlSeconds());
        definition.setIcon(toolPO.getIcon());
        definition.setPublishStatus(toolPO.getPublishStatus());
        definition.setRuntimeStatus(toolPO.getRuntimeStatus());
        definition.setVersion(toolPO.getVersion());
        definition.setRemark(toolPO.getRemark());
        return definition;
    }
}
