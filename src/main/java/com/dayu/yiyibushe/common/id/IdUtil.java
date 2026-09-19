package com.dayu.yiyibushe.common.id;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.dao.sequence.SequenceDao;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 说明：基于数据库号段模式的 ID 生成工具类，返回定长 18 位 long。
 * <p>
 * 结构：{@code [7 位时间][9 位序号][2 位后缀]}：
 * <ul>
 * <li><b>时间部分（7 位）</b>：从 2026-01-01 00:00:00 UTC 起按小时累计，加一个 7 位基数保证首位非零；
 * 7 位最大约 1000 万小时，可用约 1027 年。</li>
 * <li><b>序号部分（9 位）</b>：复用 sequence 表 common 序列按号段取（步长 STEP=1000），本地内存逐段分发；
 * 超过 10^9 减 10^9 回绕（时间前缀不同不会重复），每小时最多 10 亿个 ID。</li>
 * <li><b>后缀部分（2 位）</b>：无参版本随机 0~99；带 userId 版本取 userId % 100，用于分库分表。</li>
 * </ul>
 * 组合：{@code id = timePart * 10^11 + seqPart * 100 + suffix}，整体固定 18 位。
 * <p>
 * <b>使用方式</b>：对外提供<b>静态方法</b> {@code IdUtil.nextId()} /
 * {@code IdUtil.nextId(Long userId)}，调用方无需注入；
 * 内部通过构造器从 Spring 注入 {@link SequenceDao} 并存入静态引用（应用启动时完成一次）。
 * <p>
 * 号段缓冲按 bizType 用 {@link ConcurrentHashMap} 隔离，取段用
 * {@link ConcurrentMap#compute} 原子换段，段内序号用锁递增，保证并发安全。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Component
public class IdUtil {

    /** 默认业务标识 */
    public static final String DEFAULT_BIZ_TYPE = "common";

    /** 号段步长：一次从库取多少号 */
    public static final int STEP = 1000;

    /** 序号的取模回绕周期：10^9，减 10^9 回绕 */
    private static final long SEQ_MODULUS = 1_000_000_000L;

    /** 序号与后缀的分隔乘数：10^2 */
    private static final long SUFFIX_SEPARATOR = 100L;

    /** 时间与序号的分隔乘数：10^11 */
    private static final long TIME_SEPARATOR = 100_000_000_000L;

    /** 时间纪元基准：2026-01-01 00:00:00 UTC 的毫秒时间戳 */
    private static final long TIME_EPOCH_MILLIS = 1767225600000L;

    /** 每小时毫秒数 */
    private static final long HOUR_MILLIS = 3_600_000L;

    /** 时间部分最低基数（确保 7 位范围下限，首位非零） */
    private static final long TIME_BASE = 1_000_000L;

    /** 发号序列 DAO（复用 sequence 表的号段取号能力）；由 Spring 启动时注入静态引用 */
    private static volatile SequenceDao sequenceDao;

    /** bizType -> 当前号段缓冲（含当前序号 / 最大序号 / 锁对象） */
    private static final ConcurrentMap<String, IdSegmentBuffer> bufferByBiz = new ConcurrentHashMap<>();

    /**
     * 构造器注入发号序列 DAO：Spring 启动时把注入的实例存入静态引用，
     * 供静态方法 {@code nextId()} 直接使用（无需调用方注入本类）。
     *
     * @param sequenceDao
     *                    发号序列 DAO（sequence 表号段取号）
     */
    public IdUtil(SequenceDao sequenceDao) {
        IdUtil.sequenceDao = sequenceDao;
    }

    /**
     * 生成一个 18 位 ID（默认业务 common，无参便捷入口，直接静态调用）。
     * 最后 2 位后缀随机 0~99，适用于 user_id / task_id / task_node_id 等无明确用户归属的场景。
     *
     * @return 18 位 long 型 ID
     */
    public static long nextId() {
        return nextId(DEFAULT_BIZ_TYPE, ThreadLocalRandom.current().nextInt(100));
    }

    /**
     * 生成一个带用户分片后缀的 18 位 ID。
     * 最后 2 位后缀 = {@code userId % 100}，保证同一用户的业务 ID 落在同一分片。
     *
     * @param userId
     *               归属用户 ID（18 位）
     * @return 18 位 long 型 ID
     */
    public static long nextId(Long userId) {
        int suffix = Objects.isNull(userId) ? ThreadLocalRandom.current().nextInt(100)
                : (int) (Math.abs(userId) % 100L);
        return nextId(DEFAULT_BIZ_TYPE, suffix);
    }

    /**
     * 生成一个 18 位 ID：先算时间部分（7 位小时数），再取 9 位序号，最后拼 2 位后缀。
     *
     * @param bizType
     *                业务标识（预留多业务扩展）
     * @param suffix
     *                2 位后缀（0~99）
     * @return 18 位 long 型 ID
     */
    private static long nextId(String bizType, int suffix) {
        if (StringUtilExt.isBlank(bizType)) {
            throw new BizException(ParamErrorCode.SEQUENCE_NAME_BLANK);
        }
        long timePart = computeTimePartHours();
        long seqPart = nextSeqPart(bizType);
        return timePart * TIME_SEPARATOR + seqPart * SUFFIX_SEPARATOR + suffix;
    }

    /**
     * 计算时间部分（小时制，7 位）：纪元到当前的整小时数 + 基数。
     * 基数保证结果恒落在 [1_000_000, 9_999_999]，首位非零，满足 7 位不丢位。
     *
     * @return 7 位时间（一小时粒度）
     */
    private static long computeTimePartHours() {
        long elapsedHours = (System.currentTimeMillis() - TIME_EPOCH_MILLIS) / HOUR_MILLIS;
        return TIME_BASE + elapsedHours;
    }

    /**
     * 从号段缓冲取下一个 9 位序号：无缓冲或段耗尽则先取新段，段内序号用锁递增，超 10^9 回绕。
     * <p>
     * <b>不会死循环</b>：每轮要么当前段仍可用直接返回，要么段耗尽、换段（fetchNewSegment 必然拿到
     * 一整段全新号）后下一轮必返回；最多再多走一轮就退出循环。
     *
     * @param bizType
     *                业务标识
     * @return 9 位序号（0 ~ 10^9-1）
     */
    private static long nextSeqPart(String bizType) {
        // 循环直到拿到一个可用序号：段内取号用 tryNext 原子尝试，耗尽返回 -1 则强制换段重试
        while (true) {
            IdSegmentBuffer buffer = bufferByBiz.compute(bizType,
                    (key, existing) -> Objects.isNull(existing) ? fetchNewSegment(bizType) : existing);
            long seq = buffer.tryNextSeq();
            if (seq >= 0) {
                return seq >= SEQ_MODULUS ? seq - SEQ_MODULUS : seq;
            }
            // 本段已耗尽：用原段作为 value 做条件换段，避免并发重复取段
            bufferByBiz.compute(bizType, (key, existing) -> existing == buffer ? fetchNewSegment(bizType) : existing);
        }
    }

    /**
     * 从 sequence 表取一个新号段（compute 在锁内执行，天然隔离并发，不会重复取段）
     *
     * @param bizType
     *                业务标识
     * @return 新号段缓冲
     */
    private static IdSegmentBuffer fetchNewSegment(String bizType) {
        // 复用 sequence 表号段取号：bizType 作为序列名（行不存在会自动建初始行）
        long segmentUpper = sequenceDao.nextSegment(bizType, STEP);
        long minValue = segmentUpper - STEP + 1L;
        return new IdSegmentBuffer(minValue, segmentUpper);
    }

    /**
     * 说明：IdUtil 的号段缓冲——一段内存号（含当前序号、最大序号、锁对象）。
     * <p>
     * 同一段内的序号用 synchronized 递增，保证并发取号不重复；段耗尽后由 IdUtil 重新取段。
     *
     * @author Witty·Kid Fisher
     * @version 0.0.1
     */
    private static final class IdSegmentBuffer {

        /** 本段起始序号（含） */
        private final long minSeq;

        /** 本段结束序号（含） */
        private final long maxSeq;

        /** 当前已发到的序号 */
        private long currentSeq;

        /**
         * 构造号段缓冲，当前序号初始化为 minSeq - 1（首个 nextSeq 返回 minSeq）
         *
         * @param minSeq
         *               本段起始序号（含）
         * @param maxSeq
         *               本段结束序号（含）
         */
        IdSegmentBuffer(long minSeq, long maxSeq) {
            this.minSeq = minSeq;
            this.maxSeq = maxSeq;
            this.currentSeq = minSeq - 1;
        }

        /**
         * 原子尝试取本段下一个序号并 +1：段内可用返回序号，段耗尽返回 -1（调用方据此换段）。
         * 判断耗尽与取号在一个 synchronized 块内完成，避免并发读旧值导致重复取号。
         *
         * @return 下一个序号；本段耗尽返回 -1
         */
        synchronized long tryNextSeq() {
            if (currentSeq >= maxSeq) {
                return -1L;
            }
            currentSeq = currentSeq + 1;
            return currentSeq;
        }
    }
}
