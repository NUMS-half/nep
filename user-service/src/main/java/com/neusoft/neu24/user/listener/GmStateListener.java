package com.neusoft.neu24.user.listener;

import com.neusoft.neu24.user.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GmStateListener {

    @Resource
    private IUserService userService;

    /**
     * 监听指派成功的消息，对网格员的工作进行状态更新
     * @param gmUserId 网格员ID
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "gm_user.assign.queue", durable = "true"),
            exchange = @Exchange(name = "user.exchange", type = ExchangeTypes.TOPIC),
            key = {"assign.success"}
    ))
    public void listenAssignSuccess(String gmUserId) {
        userService.changeGmState(gmUserId, 1);
    }

    /**
     * 监听监测信息确认成功的消息，对网格员的工作进行状态更新
     * @param gmUserId 网格员ID
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "gm_user.confirm.queue", durable = "true"),
            exchange = @Exchange(name = "user.exchange", type = ExchangeTypes.TOPIC),
            key = {"confirm.success"}
    ))
    public void listenConfirmSuccess(String gmUserId) {
        userService.changeGmState(gmUserId, 0);
    }
}
