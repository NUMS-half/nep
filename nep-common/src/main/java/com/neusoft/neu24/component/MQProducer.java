package com.neusoft.neu24.component;

import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息队列生产者模板类
 *
 * @param <T> 消息类型
 */
@Component
public class MQProducer<T> {

    @Resource
    private AmqpTemplate amqpTemplate;

    /**
     * 发送消息到指定队列
     *
     * @param routingKey 队列名称
     * @param message    消息
     */
    public void sendToReportQueue(String routingKey, Class<T> t, T message) {
        amqpTemplate.convertAndSend(t.getName() + "@" + routingKey, message);
    }
}
