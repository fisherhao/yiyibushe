package com.dayu.yiyibushe.infra.mq;

/**
 * 说明：RocketMQ 发送门面，对齐 rocketmq-spring 的 {@code RocketMQTemplate} 概念。
 * <p>
 * 业务代码统一依赖本接口发送消息，不感知底层 broker 实现。
 * 消息体经 JSON 序列化进入官方 {@code Message} body（与真实集群网络序列化语义一致）。
 * 当前实现为 {@link LocalRocketMqBroker}（本地注册、本地发送、本地消费）；
 * 未来引入真实集群后由官方客户端接管，使用方代码不动。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface RocketMqTemplate {

    /**
     * 同步发送一条消息到指定 topic（对齐 RocketMQTemplate#syncSend）
     *
     * @param topic
     *     目标 topic
     * @param message
     *     消息体（发送方决定类型，消费方按约定类型接收）
     */
    void syncSend(String topic, Object message);
}
