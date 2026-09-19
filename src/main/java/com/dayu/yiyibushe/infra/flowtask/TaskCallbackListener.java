package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.infra.mq.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 说明：任务回调消息监听器，把 MQ 里 {@link TaskCallbackMessage} 投递给引擎，
 * 是"外部回调 -> 节点续跑"链路的消费端（自己发送自己消费）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class TaskCallbackListener implements MessageListener {

    private final TaskEngine taskEngine;

    /**
     * 构造器
     *
     * @param taskEngine
     *     任务执行引擎
     */
    public TaskCallbackListener(TaskEngine taskEngine) {
        this.taskEngine = taskEngine;
    }

    /**
     * 声明监听 topic：固定为回调消息 topic
     *
     * @return topic 名称
     */
    @Override
    public String getTopic() {
        return TaskCallbackMessage.TOPIC;
    }

    /**
     * 消费回调消息并交给引擎处理，非本框架的消息直接忽略
     *
     * @param message
     *     消息体
     */
    @Override
    public void onMessage(Object message) {
        if (message instanceof TaskCallbackMessage callbackMessage) {
            taskEngine.callback(callbackMessage.getTaskId());
            return;
        }
        if (Objects.nonNull(message)) {
            System.out.println("[FlowTask] 忽略非回调消息 type="
                    + message.getClass().getSimpleName());
        }
    }
}
