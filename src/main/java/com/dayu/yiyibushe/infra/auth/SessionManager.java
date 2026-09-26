package com.dayu.yiyibushe.infra.auth;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理器：维护 sessionId 与 {@link SessionEntry} 的映射。
 * <p>
 * 会话存内存，具备三项能力：
 * <ol>
 * <li>过期：不活跃超过 {@code session.timeout-seconds}（默认 2 小时）自动失效，
 * 每次有效访问滑动续期；后台定时清理过期会话；</li>
 * <li>踢登：{@link #kickByUserId(Long)} 立即销毁某用户的全部会话；</li>
 * <li>登出：{@link #remove(String)} 销毁单个会话。</li>
 * </ol>
 * 生产环境可替换为 Redis 实现以支持多实例共享。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class SessionManager {

    /** 过期会话清理周期（秒） */
    private static final long PURGE_INTERVAL_SECONDS = 60L;

    /** 会话表：sessionId -> 会话条目 */
    private final Map<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    /**
     * 会话不活跃超时（秒）
     */
    @Value("${session.timeout-seconds:7200}")
    private long timeoutSeconds;

    /** 会话不活跃超时（毫秒） */
    private long timeoutMillis;

    /** 过期清理调度器（守护线程，不阻止 JVM 退出） */
    private final ScheduledExecutorService purgeExecutor = Executors
            .newSingleThreadScheduledExecutor(SessionManager::createPurgeThread);

    /**
     * 创建清理调度器的守护线程
     *
     * @param runnable
     *                 线程任务
     *
     * @return 守护线程
     */
    private static Thread createPurgeThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "session-purge");
        thread.setDaemon(true);
        return thread;
    }

    /**
     * 初始化超时毫秒数并启动定时清理任务
     */
    @PostConstruct
    public void startPurgeTask() {
        timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);
        purgeExecutor.scheduleAtFixedRate(this::purgeExpired,
                PURGE_INTERVAL_SECONDS, PURGE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 创建会话
     *
     * @param principal
     *                  登录主体
     * @return 会话 ID
     */
    public String create(LoginPrincipal principal) {
        if (Objects.isNull(principal)) {
            throw new BizException(ParamErrorCode.PARAM_NULL);
        }
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        sessions.put(sessionId, new SessionEntry(principal, nextExpireAt()));
        return sessionId;
    }

    /**
     * 查询会话；过期会话自动删除，有效会话滑动续期
     *
     * @param sessionId
     *                  会话 ID
     * @return 登录主体，不存在或已过期返回 null
     */
    public LoginPrincipal get(String sessionId) {
        if (Objects.isNull(sessionId)) {
            return null;
        }
        SessionEntry entry = sessions.get(sessionId);
        if (Objects.isNull(entry)) {
            return null;
        }
        if (isExpired(entry)) {
            sessions.remove(sessionId);
            return null;
        }
        // 滑动续期：每次访问把过期时间向后推一个超时周期
        sessions.put(sessionId, new SessionEntry(entry.principal(), nextExpireAt()));
        return entry.principal();
    }

    /**
     * 销毁单个会话
     *
     * @param sessionId
     *                  会话 ID
     */
    public void remove(String sessionId) {
        if (Objects.nonNull(sessionId)) {
            sessions.remove(sessionId);
        }
    }

    /**
     * 强制踢登：销毁指定用户的全部会话，被踢后该用户所有请求都会被要求重新登录
     *
     * @param userId
     *               用户 ID
     * @return 被销毁的会话数量
     */
    public int kickByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return 0;
        }
        int kickedCount = 0;
        Iterator<Map.Entry<String, SessionEntry>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SessionEntry> entry = iterator.next();
            if (Objects.equals(entry.getValue().principal().userId(), userId)) {
                iterator.remove();
                kickedCount++;
            }
        }
        return kickedCount;
    }

    /**
     * 清理全部过期会话
     */
    private void purgeExpired() {
        sessions.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    /**
     * 判断会话是否已过期
     *
     * @param entry
     *              会话条目
     * @return true 已过期
     */
    private boolean isExpired(SessionEntry entry) {
        return System.currentTimeMillis() >= entry.expireAt();
    }

    /**
     * 计算新的过期时间点
     *
     * @return 过期时间戳（毫秒）
     */
    private long nextExpireAt() {
        return System.currentTimeMillis() + timeoutMillis;
    }

    /**
     * 容器停止时关闭清理调度器
     */
    @PreDestroy
    public void shutdown() {
        purgeExecutor.shutdownNow();
    }

    /**
     * 会话条目：登录主体 + 过期时间戳。
     *
     * @param principal
     *                  登录主体
     * @param expireAt
     *                  过期时间戳（毫秒）
     */
    private record SessionEntry(LoginPrincipal principal, long expireAt) {
    }
}
