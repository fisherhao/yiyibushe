package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.dao.mapper.PluginMapper;
import com.dayu.yiyibushe.dao.mybatis.PluginMybatisMapper;
import com.dayu.yiyibushe.dao.po.PluginPO;
import com.dayu.yiyibushe.infra.ai.definition.PluginDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 插件 Mapper 的 MySQL 实现，负责领域定义与 PO 的双向转换。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlPluginMapper implements PluginMapper {

    /** 布尔标记：是 */
    private static final int FLAG_TRUE = 1;

    /** 布尔标记：否 */
    private static final int FLAG_FALSE = 0;

    @Autowired
    private PluginMybatisMapper pluginMybatisMapper;

    @Override
    public PluginDefinition selectByPluginCode(String pluginCode) {
        if (Objects.isNull(pluginCode)) {
            return null;
        }
        return toDefinition(pluginMybatisMapper.selectByPluginCode(pluginCode));
    }

    @Override
    public List<PluginDefinition> selectAll() {
        return CollectionUtilExt.toStream(pluginMybatisMapper.selectAll())
                .map(this::toDefinition)
                .toList();
    }

    @Override
    public int insert(PluginDefinition pluginDefinition) {
        if (Objects.isNull(pluginDefinition)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        pluginDefinition.setPluginId(IdUtil.nextId());
        return pluginMybatisMapper.insert(toPO(pluginDefinition));
    }

    @Override
    public int updateByPluginId(PluginDefinition pluginDefinition) {
        if (Objects.isNull(pluginDefinition) || Objects.isNull(pluginDefinition.getPluginId())) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        return pluginMybatisMapper.updateByPluginId(toPO(pluginDefinition));
    }

    /**
     * 领域定义转 PO
     *
     * @param definition 插件定义
     * @return 插件 PO
     */
    private PluginPO toPO(PluginDefinition definition) {
        PluginPO pluginPO = new PluginPO();
        pluginPO.setPluginId(definition.getPluginId());
        pluginPO.setPluginCode(definition.getPluginCode());
        pluginPO.setDisplayName(definition.getDisplayName());
        pluginPO.setDescription(definition.getDescription());
        pluginPO.setIcon(definition.getIcon());
        pluginPO.setVersion(definition.getVersion());
        pluginPO.setAuthor(definition.getAuthor());
        pluginPO.setOwner(definition.getOwner());
        pluginPO.setVisibility(definition.getVisibility());
        pluginPO.setSourceType(definition.getSourceType());
        pluginPO.setSourceRef(definition.getSourceRef());
        pluginPO.setCategory(definition.getCategory());
        pluginPO.setTags(definition.getTags());
        pluginPO.setItems(definition.getItems());
        pluginPO.setPermissionConfig(definition.getPermissionConfig());
        pluginPO.setCredentialRequired(definition.isCredentialRequired() ? FLAG_TRUE : FLAG_FALSE);
        pluginPO.setPublishStatus(definition.getPublishStatus());
        pluginPO.setRuntimeStatus(definition.getRuntimeStatus());
        pluginPO.setRemark(definition.getRemark());
        return pluginPO;
    }

    /**
     * PO 转领域定义
     *
     * @param pluginPO 插件 PO
     * @return 插件定义，入参为 null 时返回 null
     */
    private PluginDefinition toDefinition(PluginPO pluginPO) {
        if (Objects.isNull(pluginPO)) {
            return null;
        }
        PluginDefinition definition = new PluginDefinition();
        definition.setPluginId(pluginPO.getPluginId());
        definition.setPluginCode(pluginPO.getPluginCode());
        definition.setDisplayName(pluginPO.getDisplayName());
        definition.setDescription(pluginPO.getDescription());
        definition.setIcon(pluginPO.getIcon());
        definition.setVersion(pluginPO.getVersion());
        definition.setAuthor(pluginPO.getAuthor());
        definition.setOwner(pluginPO.getOwner());
        definition.setVisibility(pluginPO.getVisibility());
        definition.setSourceType(pluginPO.getSourceType());
        definition.setSourceRef(pluginPO.getSourceRef());
        definition.setCategory(pluginPO.getCategory());
        definition.setTags(pluginPO.getTags());
        definition.setItems(pluginPO.getItems());
        definition.setPermissionConfig(pluginPO.getPermissionConfig());
        definition.setCredentialRequired(Objects.equals(pluginPO.getCredentialRequired(), FLAG_TRUE));
        definition.setPublishStatus(pluginPO.getPublishStatus());
        definition.setRuntimeStatus(pluginPO.getRuntimeStatus());
        definition.setRemark(pluginPO.getRemark());
        return definition;
    }
}
