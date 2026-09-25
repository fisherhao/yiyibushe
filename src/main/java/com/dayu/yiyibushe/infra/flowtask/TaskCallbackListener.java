package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.infra.mq.RocketMqListener;
import com.dayu.yiyibushe.infra.mq.RocketMqMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 说明：任务回调消息监听器，把 MQ 里 {@link TaskCallbackMessage} 投递给引擎，
 * 是"外部回调 -> 节点续跑"链路的消费端（自己发送自己消费）。
 * <p>
 * 对齐 rocketmq-spring 模型：实现 {@link RocketMqListener} + 标注
 * {@link RocketMqMessageListener} 声明 topic 与消费组，由 MQ 框架自动注册订阅。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
@RocketMqMessageListener(topic = TaskCallbackMessage.TOPIC, consumerGroup = "yiyibushe-task-callback-consumer")
public class TaskCallbackListener implements RocketMqListener<TaskCallbackMessage> {

    @Autowired
    private TaskEngine taskEngine;

    /**
     * 消费回调消息并交给引擎处理
     *
     * @param message
     *     任务回调消息
     */
    @Override
    public void onMessage(TaskCallbackMessage message) {
        taskEngine.callback(message.getTaskId());
    }
}
