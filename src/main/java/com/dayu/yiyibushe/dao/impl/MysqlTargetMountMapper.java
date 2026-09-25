package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.dao.mapper.TargetMountMapper;
import com.dayu.yiyibushe.dao.mybatis.TargetMountMybatisMapper;
import com.dayu.yiyibushe.dao.po.TargetMountPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * AI 能力挂载 Mapper 的 MySQL 实现。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Repository
public class MysqlTargetMountMapper implements TargetMountMapper {

    @Autowired
    private TargetMountMybatisMapper targetMountMybatisMapper;

    @Override
    public List<TargetMountPO> selectByTarget(String targetType, Long targetId) {
        if (Objects.isNull(targetType) || Objects.isNull(targetId)) {
            return List.of();
        }
        return targetMountMybatisMapper.selectByTarget(targetType, targetId);
    }

    @Override
    public int insert(TargetMountPO targetMountPO) {
        if (Objects.isNull(targetMountPO)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        targetMountPO.setMountId(IdUtil.nextId());
        return targetMountMybatisMapper.insert(targetMountPO);
    }

    @Override
    public int updateStatusByMountId(Long mountId, String status) {
        if (Objects.isNull(mountId) || Objects.isNull(status)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        return targetMountMybatisMapper.updateStatusByMountId(mountId, status);
    }
}
