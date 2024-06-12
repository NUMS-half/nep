package com.neusoft.neu24.notification.controller;

import com.rabbitmq.client.Channel;
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

@RestController
@RequestMapping("/sse")
public class SseController {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    // SseEmitter集合
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    /**
     * 建立订阅用户通知连接
     *
     * @param userId 用户ID
     * @return SseEmitter
     */
    @GetMapping(value = "/setup/{userId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter setup(@PathVariable String userId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(userId, emitter);

        logger.info("Connect established: User {} setup.", userId);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            logger.info("Connect closed by completion: User {}.", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            logger.info("Connect closed by timeout: User {}.", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            logger.error("Connect error by error : User {}.", userId);
        });

        return emitter;
    }

    /**
     * 移除用户连接
     */
    @GetMapping(value = "/close/{userId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public void close(@PathVariable("userId") String userId) {
        emitters.remove(userId);
        logger.info("User {} Connect closed.", userId);
    }

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
                logger.info("Received message: {} from {}", message, routingKey);
                emitter.send(SseEmitter.event().name("message").data(message, MediaType.APPLICATION_JSON));
                channel.basicAck(deliveryTag, false); // 消息确认
            } catch ( IOException e ) {
                emitters.remove(userId);
                try {
                    channel.basicNack(deliveryTag, false, true); // 消息拒绝并重新入队
                } catch ( IOException ioException ) {
                    logger.error("Failed to Nack message", ioException);
                }
            }
        } else {
            try {
                channel.basicNack(deliveryTag, false, true); // 消息拒绝并重新入队
            } catch ( IOException e ) {
                logger.error("Failed to Nack message", e);
            }
        }

    }
}
