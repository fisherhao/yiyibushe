package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.infra.flowtask.retry.FibonacciRetryStrategy;
import com.dayu.yiyibushe.infra.flowtask.retry.FixedIntervalRetryStrategy;
import com.dayu.yiyibushe.infra.flowtask.retry.SequenceRetryStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 说明：重试策略单元测试，验证三种内置策略的间隔计算：
 * 固定间隔、斐波那契序列、自定义分钟序列（含超长兜底）。
 * <p>
 * 策略配置改为 @Value 字段注入后，测试用 {@link ReflectionTestUtils} 替换私有配置字段
 * （spring-test 自带，模拟 Spring 注入），Sequence 再手动触发一次 @PostConstruct 解析。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
class RetryStrategyTest {

    /** 一分钟毫秒数，用于构造期望值 */
    private static final long ONE_MINUTE_MILLIS = 60_000L;

    /**
     * 固定间隔策略：任意失败次数都返回相同间隔
     */
    @Test
    void fixed_shouldReturnSameIntervalRegardlessOfRetryCount() {
        FixedIntervalRetryStrategy strategy = new FixedIntervalRetryStrategy();
        ReflectionTestUtils.setField(strategy, "intervalMillis", ONE_MINUTE_MILLIS);
        assertEquals(FixedIntervalRetryStrategy.CODE, strategy.getCode());
        assertEquals(ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(1));
        assertEquals(ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(4));
    }

    /**
     * 斐波那契策略：前五项为 1,1,2,3,5 个基数单位
     */
    @Test
    void fibonacci_shouldFollow11235Sequence() {
        FibonacciRetryStrategy strategy = new FibonacciRetryStrategy();
        ReflectionTestUtils.setField(strategy, "baseMillis", ONE_MINUTE_MILLIS);
        assertEquals(FibonacciRetryStrategy.CODE, strategy.getCode());
        assertEquals(ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(1));
        assertEquals(ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(2));
        assertEquals(2 * ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(3));
        assertEquals(3 * ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(4));
        assertEquals(5 * ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(5));
    }

    /**
     * 自定义序列策略：按 3,5,6 分钟取值，超过序列长度后用最后一个值兜底
     */
    @Test
    void sequence_shouldUseConfiguredMinutesAndClampToLastValue() {
        SequenceRetryStrategy strategy = new SequenceRetryStrategy();
        ReflectionTestUtils.setField(strategy, "minutesConfig", "3,5,6");
        strategy.initIntervals();
        assertEquals(SequenceRetryStrategy.CODE, strategy.getCode());
        assertEquals(3 * ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(1));
        assertEquals(5 * ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(2));
        assertEquals(6 * ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(3));
        assertEquals(6 * ONE_MINUTE_MILLIS, strategy.nextIntervalMillis(4));
    }
}
