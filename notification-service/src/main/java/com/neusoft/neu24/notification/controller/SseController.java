package com.neusoft.neu24.notification.controller;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>服务器通知与SSE连接前端控制器</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    /**
     * SseEmitter集合
     */
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    /**
     * 建立订阅用户通知连接
     *
     * @param userId 用户ID
     * @return SseEmitter
     */
    @GetMapping(value = "/setup/{userId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter setup(@PathVariable String userId) {
        if ( emitters.containsKey(userId) ) {
            logger.info("【连接已存在】用户 {} 连接已存在", userId);
            return emitters.get(userId);
        }
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(userId, emitter);

        logger.info("【连接建立】成功,用户 {} 已连接", userId);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            logger.info("【连接关闭】连接完成,用户: {} 断开连接", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            logger.info("【连接关闭】连接超时,用户: {} 断开连接", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            logger.error("【连接关闭】发生异常,用户 {} 断开连接", userId);
        });

        return emitter;
    }

    /**
     * 移除用户连接
     *
     * @param userId 用户ID
     */
    @GetMapping(value = "/close/{userId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public void close(@PathVariable("userId") String userId) {
        emitters.remove(userId);
        logger.info("【连接关闭】用户 {} 主动断开连接", userId);
    }

    /**
     * 监听用户队列通知
     * @param message     消息
     * @param routingKey  路由键(以用户ID区分)
     * @param deliveryTag 传递标签
     * @param channel     通道
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "user.queue", durable = "true"),
            exchange = @Exchange(name = "user.exchange", type = ExchangeTypes.TOPIC),
            key = {"notification.#"}
    ))
    public void listenUserNotification(@Payload Map<String, Object> message,
                                       @Header("amqp_receivedRoutingKey") String routingKey,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                       Channel channel) {
        // 获取routineKey中的用户ID
        String userId = routingKey.substring(routingKey.lastIndexOf('.') + 1);
        // 在emitters中查找对应ID的SseEmitter
        SseEmitter emitter = emitters.get(userId);

        if ( emitter != null ) {
            try {
                logger.info("从消息队列收到: 来自 {} 的通知消息 {}", routingKey, message);
                emitter.send(SseEmitter.event().name("message").data(message, MediaType.APPLICATION_JSON));
                channel.basicAck(deliveryTag, false); // 消息确认
            } catch ( IOException e ) {
                logger.error("发生异常，消息发送失败: {}", e.getMessage(), e);
//                emitters.remove(userId);
                try {
                    emitter.send(SseEmitter.event().name("error").data("消息发送失败"));
                    channel.basicNack(deliveryTag, false, true); // 消息拒绝并重新入队
                } catch ( IOException ioException ) {
                    logger.error("拒绝消费消息失败", ioException);
                }
            }
        } else {
            try {
                channel.basicNack(deliveryTag, false, true); // 消息拒绝并重新入队
            } catch ( IOException e ) {
                logger.error("拒绝消费消息失败", e);
            }
        }
    }

    /**
     * 监听权限更改消息
     * @param message     消息
     * @param routingKey  路由键
     * @param deliveryTag 传递标签
     * @param channel     通道
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "permission.queue", durable = "true"),
            exchange = @Exchange(name = "permission.exchange", type = ExchangeTypes.TOPIC),
            key = {"permission.change.#"}
    ))
    public void listenPermissionChange(@Payload Map<String, Object> message,
                                       @Header("amqp_receivedRoutingKey") String routingKey,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                       Channel channel) {
        try {
            try {
                for ( Map.Entry<String, SseEmitter> entry : emitters.entrySet() ) {
                    SseEmitter emitter = entry.getValue();
                    logger.info("从消息队列收到: 来自 {} 的权限更改消息 {}", routingKey, message);
                    emitter.send(SseEmitter.event().name("updatePermissions").data(message, MediaType.APPLICATION_JSON));
                }
                channel.basicAck(deliveryTag, false); // 消息确认
            } catch ( IOException e ) {
                logger.error("发生异常，消息发送失败: {}", e.getMessage(), e);
                for ( Map.Entry<String, SseEmitter> entry : emitters.entrySet() ) {
                    SseEmitter emitter = entry.getValue();
                    emitter.send(SseEmitter.event().name("error").data("消息发送失败"));
                }
                channel.basicNack(deliveryTag, false, true); // 消息拒绝并重新入队
            }
        } catch ( IOException e ) {
            logger.error("拒绝消费消息失败", e);
        }
    }

    /**
     * 监听角色更改消息
     * @param message     消息
     * @param routingKey  路由键
     * @param deliveryTag 传递标签
     * @param channel     通道
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "role.queue", durable = "true"),
            exchange = @Exchange(name = "role.exchange", type = ExchangeTypes.TOPIC),
            key = {"role.change.#"}
    ))
    public void listenRoleChange(@Payload Map<String, Object> message,
                                 @Header("amqp_receivedRoutingKey") String routingKey,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                 Channel channel) {
        // 获取routineKey中的用户ID
        String userId = routingKey.substring(routingKey.lastIndexOf('.') + 1);
        // 在emitters中查找对应ID的SseEmitter
        SseEmitter emitter = emitters.get(userId);
        if ( emitter != null ) {
            try {
                logger.info("从消息队列收到: 来自 {} 的角色更改消息 {}", routingKey, message);
                emitter.send(SseEmitter.event().name("updateRole").data(message, MediaType.APPLICATION_JSON));
                channel.basicAck(deliveryTag, false); // 消息确认
            } catch ( IOException e ) {
                try {
                    logger.error("发生异常，消息发送失败: {}", e.getMessage(), e);
                    emitter.send(SseEmitter.event().name("error").data("消息发送失败"));
                    channel.basicNack(deliveryTag, false, true); // 消息拒绝并重新入队
                } catch ( IOException ioe ) {
                    logger.error("拒绝消费消息失败", ioe);
                }
            }
        } else {
            try {
                channel.basicNack(deliveryTag, false, true); // 消息拒绝并重新入队
            } catch ( IOException e ) {
                logger.error("拒绝消费消息失败", e);
            }
        }
    }
}
