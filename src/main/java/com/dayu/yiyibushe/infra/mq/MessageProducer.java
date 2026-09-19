package com.dayu.yiyibushe.infra.mq;

import org.springframework.stereotype.Component;

/**
 * 说明：消息生产者，对齐 RocketMQ 的 Producer / RocketMQTemplate 概念。
 * <p>
 * 业务代码统一通过本类发送消息，不直接依赖 {@link LocalMessageBroker}，
 * 未来切换真实 RocketMQ 时只需替换本类实现。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class MessageProducer {

    private final LocalMessageBroker messageBroker;

    /**
     * 构造器
     *
     * @param messageBroker
     *     本地消息代理
     */
    public MessageProducer(LocalMessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    /**
     * 向指定 topic 发送一条消息
     *
     * @param topic
     *     目标 topic
     * @param payload
     *     消息体
     */
    public void send(String topic, Object payload) {
        messageBroker.send(topic, payload);
    }
}
