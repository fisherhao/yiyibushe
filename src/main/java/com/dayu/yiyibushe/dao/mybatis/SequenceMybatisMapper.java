package com.dayu.yiyibushe.dao.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 说明：发号序列表 sequence 的 MyBatis XML Mapper。
 * <p>
 * 表结构遵循全库铁律：主键 id（无语义、由发号器生成）、业务唯一键 name、
 * gmt_create / gmt_modify 由数据库维护。
 * <p>
 * 取号采用阿里系 sequence DAO 的经典写法：事务内先
 * {@code SELECT current_value ... FOR UPDATE} 锁行，再累加步长写回；
 * 行不存在时插入初始行（主键 id 数据库自增）。
 * 并发安全由 InnoDB 行锁 + 事务隔离保证。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Mapper
public interface SequenceMybatisMapper {

    /**
     * 锁行读取当前序列值（FOR UPDATE 行锁，须在事务内调用；行不存在返回 null）
     *
     * @param sequenceName
     *                     序列名（约定为业务表名）
     * @return 当前已发号最大值，序列行不存在返回 null
     */
    Long selectCurrentValueForUpdate(@Param("sequenceName") String sequenceName);

    /**
     * 插入序列初始行（主键 id 由数据库自增；并发场景由事务间隙锁保证只有一个事务插入成功）
     *
     * @param sequenceName
     *                      序列名
     * @param initialValue
     *                      初始值（即首个发出的号码）
     * @param incrementStep
     *                      步长（后续每次取号递增量）
     * @return 受影响行数
     */
    int insertInitial(@Param("sequenceName") String sequenceName,
            @Param("initialValue") long initialValue,
            @Param("incrementStep") int incrementStep);

    /**
     * 当前序列值累加步长
     *
     * @param sequenceName
     *                      序列名
     * @param incrementStep
     *                      步长
     * @return 受影响行数
     */
    int addStep(@Param("sequenceName") String sequenceName, @Param("incrementStep") int incrementStep);
}
