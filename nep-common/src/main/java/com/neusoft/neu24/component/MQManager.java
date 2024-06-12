package com.neusoft.neu24.component;

import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

@Component
public class MQManager<T> {

    @Resource
    private AmqpAdmin amqpAdmin;

    /**
     * 创建队列，如果不存在时
     */
    public void createQueueIfNotExists(Class<T> t, String routingKey) {
        // queueName: com.neusoft.neu24.entity.xxxx@xxxxxxxx
        Queue queue = new Queue(t.getName() + "@" + routingKey, true);
        amqpAdmin.declareQueue(queue);
    }

    /**
     * 删除队列，如果存在时
     */
    public void deleteQueueIfExists(Class<T> t, String routingKey) {
        amqpAdmin.deleteQueue(t.getName() + "@" + routingKey);
    }
}
