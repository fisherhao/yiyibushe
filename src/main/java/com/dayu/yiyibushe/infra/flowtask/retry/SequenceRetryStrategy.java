package com.dayu.yiyibushe.infra.flowtask.retry;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 说明：自定义序列重试策略——按配置的分钟序列决定每次重试间隔，
 * 例如配置 {@code flowtask.retry.sequence-minutes=3,5,6} 后，
 * 第 1/2/3 次失败分别等待 3/5/6 分钟。
 * <p>
 * 失败次数超过序列长度后，统一用最后一个值兜底，不再继续增长。
 * 配置内容为空或非法时回退到默认序列 3,5,6。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class SequenceRetryStrategy implements RetryStrategy {

    /** 策略标识 */
    public static final String CODE = "SEQUENCE";

    /** 默认序列（分钟） */
    private static final String DEFAULT_MINUTES = "3,5,6";

    /** 一分钟对应的毫秒数 */
    private static final long MILLIS_PER_MINUTE = 60_000L;

    /** 有序间隔列表（毫秒） */
    private final List<Long> intervalMillisList;

    /**
     * 构造器
     *
     * @param minutesConfig
     *     逗号分隔的分钟序列，来自配置 flowtask.retry.sequence-minutes
     */
    public SequenceRetryStrategy(
            @Value("${flowtask.retry.sequence-minutes:" + DEFAULT_MINUTES + "}")
            String minutesConfig) {
        this.intervalMillisList = parseMinutes(minutesConfig);
    }

    /**
     * 获取策略标识
     *
     * @return SEQUENCE
     */
    @Override
    public String getCode() {
        return CODE;
    }

    /**
     * 按失败次数取序列对应项，超出序列长度时取最后一项
     *
     * @param retryCount
     *     已发生的失败次数（从 1 开始）
     * @return 本次等待间隔（毫秒）
     */
    @Override
    public long nextIntervalMillis(int retryCount) {
        int index = retryCount - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= intervalMillisList.size()) {
            index = intervalMillisList.size() - 1;
        }
        return intervalMillisList.get(index);
    }

    /**
     * 解析逗号分隔的分钟序列为毫秒列表，空白与非正数段落忽略；
     * 若没有任何有效项则回退默认序列
     *
     * @param minutesConfig
     *     原始配置文本
     * @return 毫秒间隔列表
     */
    private static List<Long> parseMinutes(String minutesConfig) {
        List<Long> intervals = new ArrayList<>();
        if (StringUtilExt.isNotBlank(minutesConfig)) {
            String[] tokens = minutesConfig.split(",");
            for (String token : tokens) {
                String trimmed = token.trim();
                if (StringUtilExt.isBlank(trimmed)) {
                    continue;
                }
                long minutes = Long.parseLong(trimmed);
                if (minutes > 0) {
                    intervals.add(minutes * MILLIS_PER_MINUTE);
                }
            }
        }
        if (intervals.isEmpty()) {
            return parseMinutes(DEFAULT_MINUTES);
        }
        return List.copyOf(intervals);
    }
}
