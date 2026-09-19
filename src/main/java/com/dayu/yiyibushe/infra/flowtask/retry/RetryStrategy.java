package com.dayu.yiyibushe.infra.flowtask.retry;

/**
 * 说明：节点失败后的重试间隔策略（策略模式）。
 * <p>
 * 框架不写死重试节奏，全部交给本策略计算：
 * <ul>
 *   <li>{@code FixedIntervalRetryStrategy}：固定间隔（全局默认 5 分钟）；</li>
 *   <li>{@code FibonacciRetryStrategy}：斐波那契退避（1,1,2,3,5... 乘以基数）；</li>
 *   <li>{@code SequenceRetryStrategy}：自定义序列（如 3,5,6 分钟）。</li>
 * </ul>
 * 任务可通过 retryStrategyCode 指定策略；不指定时走全局默认策略。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface RetryStrategy {

    /**
     * 获取策略标识（任务用它来选择策略）
     *
     * @return 策略 code
     */
    String getCode();

    /**
     * 计算第 retryCount 次失败后距离下次重试的等待间隔
     *
     * @param retryCount
     *     已发生的失败次数（从 1 开始）
     * @return 等待间隔（毫秒）
     */
    long nextIntervalMillis(int retryCount);
}
