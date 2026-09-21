package com.dayu.yiyibushe.infra.mq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 说明：消费监听声明注解，对齐 rocketmq-spring 的
 * {@code org.apache.rocketmq.spring.annotation.RocketMQMessageListener}。
 * <p>
 * 标注在 {@link RocketMqListener} 实现类上：{@link LocalRocketMqBroker} 启动时
 * 扫描容器内带本注解的 bean 并自动注册订阅（自己注册自己消费）；
 * 未来接入真实 RocketMQ 后由官方starter按同样语义接管。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RocketMqMessageListener {

    /**
     * 监听的 topic
     *
     * @return topic 名称
     */
    String topic();

    /**
     * 消费组名称（真实 RocketMQ 用于负载均衡；本地实现仅用于日志标识）
     *
     * @return 消费组
     */
    String consumerGroup();
}
