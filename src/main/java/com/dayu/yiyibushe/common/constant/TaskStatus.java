package com.dayu.yiyibushe.common.constant;

/**
 * 异步任务状态常量。
 * <p>
 * 领域任务结果、连接层轮询、平台等待统一使用本常量，禁止散落 "SUCCEEDED" 等魔法值。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public final class TaskStatus {

    /** 排队中 */
    public static final String PENDING = "PENDING";

    /** 执行中 */
    public static final String RUNNING = "RUNNING";

    /** 成功 */
    public static final String SUCCEEDED = "SUCCEEDED";

    /** 失败 */
    public static final String FAILED = "FAILED";

    /** 状态未知（厂商未返回状态时兜底） */
    public static final String UNKNOWN = "UNKNOWN";

    /**
     * 私有构造器：常量类不允许实例化
     */
    private TaskStatus() {
    }
}
