package com.dayu.yiyibushe.dao.sequence;

/**
 * 说明：发号序列 DAO（阿里系 sequence DAO 风格，号段缓存取段）。
 * <p>
 * 每个"业务表"对应 sequence 表中的一行，按序列名（约定为表名）集中发号。
 * 采用号段思想：一次性从 sequence 表取一段（如 1000 个号）放内存缓存，
 * 段内 getId 每次 +1，段耗尽再取下一段——批量生成大量 ID 只需少量库访问。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public interface SequenceDao {

    /**
     * 按序列名取下一段号码的上界（号段区间为 [返回上界 - 段长 + 1, 返回上界]）。
     * <p>
     * 事务内 {@code SELECT ... FOR UPDATE} 锁行读取当前已发最大值，累加段长写回，
     * 返回累加后的新值即这段的闭区间上界；序列行不存在时先插入初始行。
     *
     * @param sequenceName
     *     序列名（约定为业务表名，如 users / flow_task）
     * @param segmentSize
     *     段长（一次从库取多少号，如 1000）
     * @return 本段号区间上界（含）
     */
    long nextSegment(String sequenceName, int segmentSize);
}
