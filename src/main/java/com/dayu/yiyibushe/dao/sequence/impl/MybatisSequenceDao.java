package com.dayu.yiyibushe.dao.sequence.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.dao.mybatis.SequenceMybatisMapper;
import com.dayu.yiyibushe.dao.sequence.SequenceDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 说明：{@link SequenceDao} 的 MyBatis 实现，委托 {@link SequenceMybatisMapper} 操作
 * sequence 表（供 {@code IdUtil} 生成 18 位业务 ID 的序号部分）。
 * <p>
 * 取段采用事务内 {@code SELECT ... FOR UPDATE} 行锁方案：先锁行读取当前已发最大值；
 * 行不存在则插入初始行（主键 id 数据库自增）；存在则累加段长写回，返回累加后的新值作为本段上界。
 * 并发安全由 InnoDB 行锁/间隙锁 + 事务隔离保证。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.5
 */
@Repository
public class MybatisSequenceDao implements SequenceDao {

    /** sequence 表的 MyBatis Mapper */
    private final SequenceMybatisMapper sequenceMybatisMapper;

    /**
     * 构造器注入 Mapper
     *
     * @param sequenceMybatisMapper
     *                              sequence 表 Mapper
     */
    public MybatisSequenceDao(SequenceMybatisMapper sequenceMybatisMapper) {
        this.sequenceMybatisMapper = sequenceMybatisMapper;
    }

    /**
     * 按序列名取下一段号码的上界（号段区间为 [返回上界 - 段长 + 1, 返回上界]）。
     * 事务内先锁行读取当前已发最大值，行不存在则插入初始行（首段从 1 到段长），
     * 存在则累加段长写回并返回累加后的新值作为本段上界。
     *
     * @param sequenceName
     *                     序列名
     * @param segmentSize
     *                     段长（一次从库取多少号）
     * @return 本段号区间上界（含）
     */
    @Override
    @Transactional
    public long nextSegment(String sequenceName, int segmentSize) {
        if (StringUtilExt.isBlank(sequenceName)) {
            throw new BizException(ParamErrorCode.SEQUENCE_NAME_BLANK);
        }
        Long currentValue = sequenceMybatisMapper.selectCurrentValueForUpdate(sequenceName);
        if (Objects.isNull(currentValue)) {
            // 新序列注册：主键 id 由数据库自增，这里只插入初始行
            sequenceMybatisMapper.insertInitial(sequenceName, segmentSize, segmentSize);
            return segmentSize;
        }
        sequenceMybatisMapper.addStep(sequenceName, segmentSize);
        return currentValue + segmentSize;
    }
}
