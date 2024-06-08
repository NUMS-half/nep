package com.neusoft.neu24.component;

import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.PriorityQueue;
import java.util.Queue;

@Component
public class MQConsumer<T> {

    @Resource
    private AmqpTemplate amqpTemplate;

    /**
     * 接收消息队列中的消息,并以队列形式返回
     */
    public Queue<T> getReportsFromQueue(Class<T> t, String routingKey) {
        String queueName = t.getName() + "@" + routingKey;
        // 创建队列
        Queue<T> messageQueue = new PriorityQueue<>();
        // 循环接收队列中的消息，直到队列为空
        T message = (T) amqpTemplate.receiveAndConvert(queueName);
        while ( message != null ) {
            messageQueue.add(message);
            message = (T) amqpTemplate.receiveAndConvert(queueName);
        }
        // 返回队列
        return messageQueue;
    }

}
