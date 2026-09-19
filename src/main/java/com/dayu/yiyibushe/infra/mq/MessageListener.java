package com.dayu.yiyibushe.infra.mq;

/**
 * 说明：消息监听器接口，对齐 RocketMQ 的 Consumer/MessageListener 概念。
 * <p>
 * 实现类声明自己关心的 topic，{@link LocalMessageBroker} 收到该 topic 的消息后
 * 会回调 {@link #onMessage(Object)}。当前实现是本地内存投递（自己发送自己消费），
 * 未来接入真实 RocketMQ 时，只需新增基于 rocketmq-client 的实现，业务代码不动。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface MessageListener {

    /**
     * 声明监听的 topic
     *
     * @return topic 名称
     */
    String getTopic();

    /**
     * 消费一条消息
     *
     * @param message
     *     消息体（由发送方决定类型，消费方自行判断与转换）
     */
    void onMessage(Object message);
}
