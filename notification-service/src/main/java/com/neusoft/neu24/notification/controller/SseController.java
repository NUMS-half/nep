package com.neusoft.neu24.notification.controller;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/sse")
public class SseController implements RabbitListenerConfigurer {

    @Resource
    private RabbitListenerEndpointRegistry registry;

    @Resource
    private RabbitListenerContainerFactory<?> rabbitListenerContainerFactory;

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

        System.out.println("Connect established: User " + userId + " setup");

        startListening(userId);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            stopListening(userId);
            System.out.println("Connect closed: User " + userId + " 【Completion】");
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            stopListening(userId);
            System.out.println("Connect timeout: User " + userId + " 【Timeout】");
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            stopListening(userId);
            System.out.println("Connect error: User " + userId + " 【error】");
        });

        return emitter;
    }

    /**
     * 移除用户连接
     */
    @GetMapping(value = "/close/{userId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public void close(@PathVariable("userId") String userId) {
        emitters.remove(userId);
        stopListening(userId);
        System.out.println("User Connect closed: " + userId);
    }

    /**
     * 开始监听用户通知
     * @param userId 用户ID
     */
    private void startListening(String userId) {
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId("listener-" + userId);
        endpoint.setQueueNames("user.statistics.queue");
        endpoint.setMessageListener(message -> {
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            String userFromKey = routingKey.substring(routingKey.lastIndexOf('.') + 1);
            if ( userId.equals(userFromKey) ) {
                SseEmitter emitter = emitters.get(userId);
                if ( emitter != null ) {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(message, MediaType.APPLICATION_JSON));
                    } catch ( IOException e ) {
                        emitters.remove(userId);
                    }
                }
            }
        });
        registry.registerListenerContainer(endpoint, rabbitListenerContainerFactory, true);
    }

    /**
     * 停止监听用户通知
     * @param userId 用户ID
     */
    private void stopListening(String userId) {
        MessageListenerContainer container = registry.getListenerContainer("listener-" + userId);
        container.stop();
        registry.unregisterListenerContainer("listener-" + userId);
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        // This can be left empty if not using default listeners
    }

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "user.queue", durable = "true"),
//            exchange = @Exchange(name = "user.exchange", type = ExchangeTypes.TOPIC),
//            key = "" + userList.stream().iterator()
//    ))
//    public void listenUserNotification(Map<String, Object> message, @Header("amqp_receivedRoutingKey") String routingKey) {
//        System.out.println("Received message: " + message + " from " + routingKey);
//        String userId = routingKey.substring(routingKey.lastIndexOf('.') + 1);
//        SseEmitter emitter = emitters.get(userId);
//        if ( emitter != null ) {
//            try {
//                emitter.send(SseEmitter.event().name("message").data(message, MediaType.APPLICATION_JSON));
//            } catch ( IOException e ) {
//                emitters.remove(userId);
//            }
//        }
//    }

}
