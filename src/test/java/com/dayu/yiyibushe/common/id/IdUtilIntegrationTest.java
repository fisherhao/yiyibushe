package com.dayu.yiyibushe.common.id;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 说明：IdUtil 号段 ID 生成工具集成测试（真实连库 MySQL）。
 * <p>
 * 验证 18 位 ID 的分段结构（7 位时间 + 9 位序号 + 2 位后缀）核心性质：
 * <ol>
 *   <li>生成的 ID 固定 18 位、为正数；</li>
 *   <li>同一号段内连续取号：序号部分（中间 9 位）恰好递增 1，后缀落在 0~99；</li>
 *   <li>连续多次取号 ID 严格单调递增（序号进位 +100 恒大于后缀最大回退 99）；</li>
 *   <li>带 userId 版本：后缀恒等于 userId % 100。</li>
 * </ol>
 * 每个用例前把 sequence 表的 common 行 current_value 重置为 0，保证取段行为确定、可重复。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@SpringBootTest
class IdUtilIntegrationTest {

    /** 重置 common 号段行的 SQL（复用 sequence 表取段） */
    private static final String RESET_COMMON_SEGMENT_SQL =
            "UPDATE sequence SET current_value = 0 WHERE name = 'common'";

    /** 序号部分取掩码：中间 9 位（id / 100 % 10^9） */
    private static final long SEQ_PART_MASK = 1_000_000_000L;

    @Autowired
    private IdUtil idUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 每个用例前重置 common 号段，保证取段行为确定
     */
    @BeforeEach
    void resetCommonSegment() {
        jdbcTemplate.update(RESET_COMMON_SEGMENT_SQL);
    }

    /**
     * 抽取 ID 的序号部分（中间 9 位）
     *
     * @param id 18 位 ID
     * @return 序号部分
     */
    private static long extractSeqPart(long id) {
        return id / 100 % SEQ_PART_MASK;
    }

    /**
     * 抽取 ID 的后缀部分（最后 2 位）
     *
     * @param id 18 位 ID
     * @return 后缀（0~99）
     */
    private static int extractSuffix(long id) {
        return (int) (id % 100);
    }

    /**
     * 生成的 ID 应始终为 18 位正数（7 位时间 + 9 位序号 + 2 位后缀），无前导零丢位
     */
    @Test
    void nextIdShouldReturnPositive18DigitId() {
        long id = idUtil.nextId();
        assertTrue(id > 0, "ID 应为正数");
        assertEquals(18, String.valueOf(id).length(), "ID 应固定为 18 位");
    }

    /**
     * 同一号段内连续取号两次：时间部分不变、序号部分（中间 9 位）恰好递增 1，后缀落在 0~99
     */
    @Test
    void nextIdShouldIncreaseSeqWithinSameSegmentByOne() {
        long firstId = idUtil.nextId();
        long secondId = idUtil.nextId();
        assertTrue(extractSuffix(firstId) >= 0 && extractSuffix(firstId) < 100,
                "后缀应落在 0~99");
        assertEquals(extractSeqPart(firstId) + 1, extractSeqPart(secondId),
                "同段内连续取号序号部分应恰好递增 1");
    }

    /**
     * 连续多次取号：在同一小时内时间部分保持一致，ID 严格单调递增
     */
    @Test
    void nextIdShouldBeMonotonicallyIncreasing() {
        long previousId = idUtil.nextId();
        for (int index = 0; index < 50; index++) {
            long currentId = idUtil.nextId();
            assertTrue(currentId > previousId, "ID 应严格递增（序号进位 +100 恒大于后缀最大回退 99）");
            previousId = currentId;
        }
    }

    /**
     * 带 userId 版本：后缀应恒等于 userId % 100（分库分表路由键）
     */
    @Test
    void nextIdWithUserIdShouldEndWithUserIdModulo() {
        long userId = idUtil.nextId();
        long assetId = idUtil.nextId(userId);
        assertEquals((int) (userId % 100), extractSuffix(assetId),
                "带 userId 生成的 ID 后缀应等于 userId % 100");
        assertEquals(18, String.valueOf(assetId).length(), "ID 应固定为 18 位");
    }
}
