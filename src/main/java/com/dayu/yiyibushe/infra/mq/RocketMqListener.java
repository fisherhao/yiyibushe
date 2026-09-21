package com.dayu.yiyibushe.infra.mq;

/**
 * 说明：RocketMQ 消费监听器接口，对齐 rocketmq-spring 的
 * {@code org.apache.rocketmq.spring.core.RocketMQListener}。
 * <p>
 * 实现类用 {@link RocketMqMessageListener} 注解声明 topic 与消费组，
 * 由 MQ 框架自动注册订阅，收到消息后回调 {@link #onMessage(Object)}。
 * 当前由 {@link LocalRocketMqBroker} 本地投递；未来接入真实 RocketMQ 后语义不变。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 *
 * @param <T>
 *     消息体类型：broker 按 JSON 把 body 反序列化成该类型再回调，
 *     与官方 rocketmq-spring 监听容器行为一致
 */
public interface RocketMqListener<T> {

    /**
     * 消费一条消息
     *
     * @param message
     *     消息体
     */
    void onMessage(T message);
}
