package com.dayu.yiyibushe.infra.mq;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.SystemErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 说明：本地内存消息代理，模拟 RocketMQ 的「Producer -> Broker -> Consumer」链路，
 * 实现"本地发送、本地消费"（未接入真实 broker 时的本地实现）。
 * <p>
 * 发送：{@link #send(String, Object)} 把消息放入内存队列；
 * 消费：后台守护线程从队列取消息，按 topic 分发给已注册的 {@link MessageListener}。
 * API 形状与 RocketMQ 对齐（topic / send / listener），
 * 未来接入真实 RocketMQ 时，新增基于 rocketmq-client 的实现即可，使用方不动。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class LocalMessageBroker {

    /** 消费分发线程名 */
    private static final String BROKER_THREAD_NAME = "local-mq-broker";

    /** 消息信封：topic + 消息体 */
    private record PendingMessage(String topic, Object payload) {
    }

    /** 待消费消息队列 */
    private final BlockingQueue<PendingMessage> messageQueue = new LinkedBlockingQueue<>();

    /** topic -> 监听器列表 */
    private final Map<String, List<MessageListener>> listenerMap = new ConcurrentHashMap<>();

    /** Spring 容器里全部 MessageListener 实现（自动注册） */
    private final List<MessageListener> messageListeners;

    /** 分发线程开关 */
    private volatile boolean running = true;

    /** 消费分发线程 */
    private Thread dispatcherThread;

    /**
     * 构造器：Spring 自动注入容器中所有 {@link MessageListener} 实现
     *
     * @param messageListeners
     *     消息监听器列表
     */
    public LocalMessageBroker(List<MessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    /**
     * 启动后台分发线程，并自动注册容器里的全部监听器（守护线程，随 JVM 退出）
     */
    @PostConstruct
    public void start() {
        for (MessageListener listener : messageListeners) {
            registerListener(listener);
        }
        dispatcherThread = new Thread(this::deliverLoop, BROKER_THREAD_NAME);
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
        System.out.println("[MQ] 本地消息代理已启动");
    }

    /**
     * 停止分发线程（应用关闭时调用）
     */
    @PreDestroy
    public void stop() {
        running = false;
        if (Objects.nonNull(dispatcherThread)) {
            dispatcherThread.interrupt();
        }
    }

    /**
     * 注册消息监听器（等价于 RocketMQ 的订阅）
     *
     * @param listener
     *     消息监听器
     */
    public void registerListener(MessageListener listener) {
        listenerMap.computeIfAbsent(listener.getTopic(), key -> new CopyOnWriteArrayList<>())
                .add(listener);
        System.out.println("[MQ] 注册监听器 topic=" + listener.getTopic());
    }

    /**
     * 发送一条消息（等价于 RocketMQTemplate 的 send）
     *
     * @param topic
     *     目标 topic
     * @param payload
     *     消息体
     */
    public void send(String topic, Object payload) {
        try {
            messageQueue.put(new PendingMessage(topic, payload));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(SystemErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 分发主循环：阻塞取消息并按 topic 投递给监听器
     */
    private void deliverLoop() {
        while (running) {
            try {
                PendingMessage message = messageQueue.take();
                dispatch(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * 把一条消息投递给该 topic 的全部监听器
     *
     * @param message
     *     待分发消息
     */
    private void dispatch(PendingMessage message) {
        List<MessageListener> listeners = listenerMap.get(message.topic());
        if (CollectionUtilExt.isEmpty(listeners)) {
            System.out.println("[MQ] 无监听者，消息丢弃 topic=" + message.topic());
            return;
        }
        for (MessageListener listener : listeners) {
            try {
                listener.onMessage(message.payload());
            } catch (Exception e) {
                // 单个监听器异常不能拖垮分发线程
                System.out.println("[MQ] 监听器消费异常 topic=" + message.topic()
                        + " listener=" + listener.getClass().getSimpleName()
                        + " error=" + e.getMessage());
            }
        }
    }
}
