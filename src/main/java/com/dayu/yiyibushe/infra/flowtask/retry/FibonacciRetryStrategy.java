package com.dayu.yiyibushe.infra.flowtask.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 说明：斐波那契退避重试策略——等待间隔按斐波那契数列递增：
 * 第 1/2/3/4/5 次失败分别等待 1,1,2,3,5 个基数单位。
 * <p>
 * 基数通过 {@code flowtask.retry.fibonacci-base-millis} 配置，默认 1 分钟，
 * 即默认节奏为 1 分钟、1 分钟、2 分钟、3 分钟、5 分钟……
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class FibonacciRetryStrategy implements RetryStrategy {

    /** 策略标识 */
    public static final String CODE = "FIBONACCI";

    /** 斐波那契基数（毫秒），序列各项乘以它 */
    @Value("${flowtask.retry.fibonacci-base-millis:60000}")
    private long baseMillis;

    /**
     * 获取策略标识
     *
     * @return FIBONACCI
     */
    @Override
    public String getCode() {
        return CODE;
    }

    /**
     * 计算第 retryCount 项斐波那契数乘以基数
     *
     * @param retryCount
     *     已发生的失败次数（从 1 开始）
     * @return 本次等待间隔（毫秒）
     */
    @Override
    public long nextIntervalMillis(int retryCount) {
        return fibonacci(retryCount) * baseMillis;
    }

    /**
     * 迭代计算第 n 项斐波那契数（n 小于等于 1 时按第 1 项处理）
     *
     * @param n
     *     项数（从 1 开始）
     * @return 第 n 项斐波那契数
     */
    private static long fibonacci(int n) {
        long previous = 1L;
        long current = 1L;
        for (int index = 2; index < n; index++) {
            long next = previous + current;
            previous = current;
            current = next;
        }
        return current;
    }
}
