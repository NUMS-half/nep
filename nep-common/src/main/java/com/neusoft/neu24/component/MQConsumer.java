package com.neusoft.neu24.component;

import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MQConsumer<T> {

    @Resource
    private AmqpTemplate amqpTemplate;

    /**
     * 接收消息队列中的消息,并以队列形式返回
     */
    public List<T> getMessageFromQueue(Class<T> t, String routingKey) {
        String queueName = t.getName() + "@" + routingKey;
        // 创建队列
        List<T> messageList = new ArrayList<>();
        // 循环接收队列中的消息，直到队列为空
        T message = (T) amqpTemplate.receiveAndConvert(queueName);
        while ( message != null ) {
            messageList.add(message);
            message = (T) amqpTemplate.receiveAndConvert(queueName);
        }
        // 返回队列
        return messageList;
    }

}
