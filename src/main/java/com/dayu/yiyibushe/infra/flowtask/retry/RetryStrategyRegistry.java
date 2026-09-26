package com.dayu.yiyibushe.infra.flowtask.retry;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.SystemErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 说明：重试策略注册解析器，维护「策略 code -> 策略实现」的映射。
 * <p>
 * 容器中所有 {@link RetryStrategy} Bean 启动时自动注册，无需手工登记；
 * 任务上的 retryStrategyCode 为空时返回全局默认策略
 * （配置 {@code flowtask.retry.default-strategy}，默认 FIXED）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class RetryStrategyRegistry {

    private static final Logger log = LogUtilExt.getLogger(RetryStrategyRegistry.class);

    /** 策略 code -> 策略实现 */
    private final Map<String, RetryStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 容器中全部重试策略 Bean
     */
    @Autowired
    private List<RetryStrategy> strategies;

    /**
     * 全局默认策略 code
     */
    @Value("${flowtask.retry.default-strategy:FIXED}")
    private String defaultCode;

    /** 全局默认策略 */
    private RetryStrategy defaultStrategy;

    /**
     * 注册全部重试策略并校验默认策略存在
     */
    @PostConstruct
    public void registerStrategies() {
        for (RetryStrategy strategy : strategies) {
            if (Objects.isNull(strategy) || StringUtilExt.isBlank(strategy.getCode())) {
                // 策略 code 是框架契约，为空只可能是实现错误，跳过并提示
                LogUtilExt.warn(log, "[FlowTask] 忽略 code 为空的重试策略");
                continue;
            }
            if (Objects.nonNull(strategyMap.put(strategy.getCode(), strategy))) {
                throw new BizException(SystemErrorCode.SYSTEM_ERROR);
            }
        }
        this.defaultStrategy = strategyMap.get(defaultCode);
        if (Objects.isNull(this.defaultStrategy)) {
            // 默认策略配置错误属于系统配置问题，启动直接失败暴露问题
            LogUtilExt.error(log, "[FlowTask] 默认重试策略不存在 defaultCode={0}", defaultCode);
            throw new BizException(SystemErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 按 code 解析策略：code 为空返回全局默认策略，code 未注册抛业务异常
     *
     * @param code
     *     任务指定的策略 code
     * @return 重试策略
     */
    public RetryStrategy resolve(String code) {
        if (StringUtilExt.isBlank(code)) {
            return defaultStrategy;
        }
        RetryStrategy strategy = strategyMap.get(code);
        if (Objects.isNull(strategy)) {
            throw new BizException(BizErrorCode.TASK_RETRY_STRATEGY_MISSING);
        }
        return strategy;
    }
}