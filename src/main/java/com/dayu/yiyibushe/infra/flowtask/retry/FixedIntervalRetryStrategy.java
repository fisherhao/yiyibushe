package com.dayu.yiyibushe.infra.flowtask.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 说明：固定间隔重试策略——每次失败后都等待相同时间，是框架的全局默认策略。
 * <p>
 * 间隔通过 {@code flowtask.retry.fixed-interval-millis} 配置，默认 5 分钟。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /** 策略标识 */
    public static final String CODE = "FIXED";

    /** 固定重试间隔（毫秒） */
    private final long intervalMillis;

    /**
     * 构造器
     *
     * @param intervalMillis
     *     固定重试间隔（毫秒），来自配置 flowtask.retry.fixed-interval-millis
     */
    public FixedIntervalRetryStrategy(
            @Value("${flowtask.retry.fixed-interval-millis:300000}") long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    /**
     * 获取策略标识
     *
     * @return FIXED
     */
    @Override
    public String getCode() {
        return CODE;
    }

    /**
     * 固定间隔，与失败次数无关
     *
     * @param retryCount
     *     已发生的失败次数
     * @return 固定重试间隔
     */
    @Override
    public long nextIntervalMillis(int retryCount) {
        return intervalMillis;
    }
}
