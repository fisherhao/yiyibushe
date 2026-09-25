package com.dayu.yiyibushe.infra.mq;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.SystemErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.JsonUtilExt;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.common.message.Message;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 说明：本地内存消息代理，RocketMQ 官方形态的本地实现——「本地注册、本地发送、本地消费」。
 * <p>
 * 协议与官方 RocketMQ 对齐，只是不出网：
 * <ul>
 * <li>消息载体：官方 {@link Message}（org.apache.rocketmq.common.message.Message），
 * 消息体经 JSON 序列化进入 body，与真实集群的网络序列化语义一致；</li>
 * <li>发送端：实现 {@link RocketMqTemplate}，等价于 RocketMQTemplate#syncSend；</li>
 * <li>消费端：业务 bean 实现 {@link RocketMqListener} 并标注
 * {@link RocketMqMessageListener}，
 * 本类启动时扫描容器自动注册订阅（自己注册自己消费），
 * 消费时按 listener 泛型把 body 反序列化成目标类型再回调——与官方
 * rocketmq-spring 监听容器行为一致。</li>
 * </ul>
 * 投递链路：syncSend 序列化入内存队列，后台守护线程按 topic 分发给消费者，
 * 单个消费者异常不影响分发线程与其他消费者。
 * 未来接入真实集群：RocketMQ 集群按同样的 topic / 消息体 / 泛型约定接管，业务代码不动。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class LocalRocketMqBroker implements RocketMqTemplate {

    private static final Logger log = LogUtilExt.getLogger(LocalRocketMqBroker.class);

    /** 消费分发线程名 */
    private static final String BROKER_THREAD_NAME = "local-rocketmq-broker";

    /** 消费绑定：消费组 + 监听器 + 监听器声明的消息类型（泛型实参） */
    private record ListenerBinding(String consumerGroup, RocketMqListener<Object> listener, Class<?> messageClass) {
    }

    /** 待消费消息队列（官方 Message 信封，body 为 JSON） */
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    /** topic -> 消费绑定列表 */
    private final Map<String, List<ListenerBinding>> bindingMap = new ConcurrentHashMap<>();

    /** Spring 容器：用于扫描带 @RocketMqMessageListener 的消费者 bean */
    @Autowired
    private ApplicationContext applicationContext;

    /** 分发线程开关 */
    private volatile boolean running = true;

    /** 消费分发线程 */
    private Thread dispatcherThread;

    /**
     * 启动：扫描容器内全部 @RocketMqMessageListener 消费者并注册订阅，
     * 再启动后台分发线程（守护线程，随 JVM 退出）
     */
    @PostConstruct
    public void start() {
        registerAllListeners();
        dispatcherThread = new Thread(this::deliverLoop, BROKER_THREAD_NAME);
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
        LogUtilExt.info(log, "[RocketMQ-Local] 本地消息代理已启动（官方 Message 协议，本地发送本地消费）");
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
     * 扫描容器内带 {@link RocketMqMessageListener} 注解的 bean 并注册为订阅者
     */
    @SuppressWarnings("unchecked")
    private void registerAllListeners() {
        Map<String, Object> listenerBeans = applicationContext.getBeansWithAnnotation(RocketMqMessageListener.class);
        for (Map.Entry<String, Object> entry : listenerBeans.entrySet()) {
            Object bean = entry.getValue();
            if (!(bean instanceof RocketMqListener)) {
                LogUtilExt.warn(log, "[RocketMQ-Local] bean 未实现 RocketMqListener，跳过注册 bean={0}", entry.getKey());
                continue;
            }
            RocketMqMessageListener annotation = AnnotatedElementUtils.findMergedAnnotation(bean.getClass(),
                    RocketMqMessageListener.class);
            register(annotation.topic(), annotation.consumerGroup(), (RocketMqListener<Object>) bean);
        }
    }

    /**
     * 注册一个消费者订阅（等价于 RocketMQ 的订阅关系）
     *
     * @param topic
     *                      监听的 topic
     * @param consumerGroup
     *                      消费组
     * @param listener
     *                      消费监听器
     */
    private void register(String topic, String consumerGroup, RocketMqListener<Object> listener) {
        Class<?> messageClass = resolveMessageType(listener.getClass());
        if (Objects.isNull(messageClass)) {
            LogUtilExt.warn(log, "[RocketMQ-Local] 监听器未声明消息泛型，无法反序列化消息体，跳过注册 listener={0}",
                    listener.getClass().getName());
            return;
        }
        bindingMap.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>())
                .add(new ListenerBinding(consumerGroup, listener, messageClass));
        LogUtilExt.info(log, "[RocketMQ-Local] 注册消费者 topic={0} consumerGroup={1} listener={2} messageType={3}",
                topic, consumerGroup, listener.getClass().getSimpleName(), messageClass.getSimpleName());
    }

    /**
     * 解析监听器实现的 {@link RocketMqListener} 泛型实参（即消息体类型）
     *
     * @param listenerClass
     *                      监听器实现类
     * @return 消息体类型；未声明泛型时返回 null
     */
    private Class<?> resolveMessageType(Class<?> listenerClass) {
        return ResolvableType.forClass(listenerClass)
                .as(RocketMqListener.class)
                .getGeneric(0)
                .resolve();
    }

    /**
     * 同步发送：消息体 JSON 序列化进官方 {@link Message} 后入内存队列，
     * 等价于 RocketMQTemplate#syncSend
     *
     * @param topic
     *                目标 topic
     * @param message
     *                消息体（发送方决定类型，消费方按 listener 泛型接收）
     */
    @Override
    public void syncSend(String topic, Object message) {
        try {
            byte[] body = JsonUtilExt.toJsonString(message).getBytes(StandardCharsets.UTF_8);
            messageQueue.put(new Message(topic, body));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(SystemErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 分发主循环：阻塞取消息并按 topic 投递给消费者
     */
    private void deliverLoop() {
        while (running) {
            try {
                Message message = messageQueue.take();
                dispatch(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * 把一条消息投递给该 topic 的全部消费绑定：
     * body JSON 反序列化成监听器声明的消息类型后回调（对齐官方监听容器行为）
     *
     * @param message
     *                待分发消息（官方 Message）
     */
    private void dispatch(Message message) {
        List<ListenerBinding> bindings = bindingMap.get(message.getTopic());
        if (CollectionUtilExt.isEmpty(bindings)) {
            LogUtilExt.warn(log, "[RocketMQ-Local] topic 无消费者，消息丢弃 topic={0}", message.getTopic());
            return;
        }
        for (ListenerBinding binding : bindings) {
            try {
                String body = new String(message.getBody(), StandardCharsets.UTF_8);
                Object payload = JsonUtilExt.parseObject(body, binding.messageClass());
                binding.listener().onMessage(payload);
            } catch (Exception e) {
                // 单个消费者异常不能拖垮分发线程
                LogUtilExt.error(log, "[RocketMQ-Local] 消费异常 topic={0} consumerGroup={1} listener={2}",
                        message.getTopic(), binding.consumerGroup(),
                        binding.listener().getClass().getSimpleName(), e);
            }
        }
    }
}