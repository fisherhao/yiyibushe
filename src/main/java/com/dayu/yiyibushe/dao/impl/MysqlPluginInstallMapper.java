package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.dao.mapper.PluginInstallMapper;
import com.dayu.yiyibushe.dao.mybatis.PluginInstallMybatisMapper;
import com.dayu.yiyibushe.dao.po.PluginInstallPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 插件安装实例 Mapper 的 MySQL 实现。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlPluginInstallMapper implements PluginInstallMapper {

    @Autowired
    private PluginInstallMybatisMapper pluginInstallMybatisMapper;

    @Override
    public PluginInstallPO selectByInstallId(Long installId) {
        if (Objects.isNull(installId)) {
            return null;
        }
        return pluginInstallMybatisMapper.selectByInstallId(installId);
    }

    @Override
    public List<PluginInstallPO> selectByScope(String scopeType, Long scopeId) {
        if (Objects.isNull(scopeType) || Objects.isNull(scopeId)) {
            return List.of();
        }
        return pluginInstallMybatisMapper.selectByScope(scopeType, scopeId);
    }

    @Override
    public int insert(PluginInstallPO pluginInstallPO) {
        if (Objects.isNull(pluginInstallPO)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        pluginInstallPO.setInstallId(IdUtil.nextId());
        return pluginInstallMybatisMapper.insert(pluginInstallPO);
    }

    @Override
    public int updateStatusByInstallId(Long installId, String installStatus) {
        if (Objects.isNull(installId) || Objects.isNull(installStatus)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        return pluginInstallMybatisMapper.updateStatusByInstallId(installId, installStatus);
    }
}
