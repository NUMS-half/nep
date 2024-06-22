package com.neusoft.neu24.chat.controller;

import cn.hutool.core.bean.BeanUtil;
import com.neusoft.neu24.chat.service.IChatService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Message;
import com.neusoft.neu24.chat.utils.JwtUtil;
import com.neusoft.neu24.entity.ResponseEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Transactional
@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private OpenAiChatClient chatClient;

    @Resource
    IChatService chatService;

    @Resource
    JwtUtil jwtUtil;

    /**
     * 直接生成对话
     * @param map 对话问题
     * @return 对话结果
     */
    @PostMapping(value = "/generate", headers = "Accept=application/json")
    public HttpResponseEntity<String> generate(@RequestBody Map<String,Object> map) {
        // 1. 获取用户发送的消息并保存
        Message sendMessage = BeanUtil.fillBeanWithMap(map, new Message(), false);
        // 消息不能为空
        if( sendMessage.getContent().isEmpty() ) {
            return new HttpResponseEntity<String>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        sendMessage.setRecipient(0); // 系统接收的消息
        HttpResponseEntity<Message> saveResponse1 =  chatService.saveChatMessage(sendMessage);

        // 2. 调用AI接口生成对话
        String generate = chatClient.call(sendMessage.getContent());
        // 3. 保存AI生成的消息
        Message generateMessage = new Message();
        generateMessage.setMessageId(null);
        generateMessage.setUserId(sendMessage.getUserId());
        generateMessage.setRecipient(1); // 用户接收的消息
        generateMessage.setContent(generate);
        HttpResponseEntity<Message> saveResponse2 =  chatService.saveChatMessage(generateMessage);

        return new HttpResponseEntity<String>().success(generate);
    }

    /**
     * 根据用户加载历史消息
     * @param userId 用户ID
     * @return 历史消息列表
     */
    @PostMapping(value = "/load")
    public HttpResponseEntity<List<Message>> loadHistory(@RequestParam("userId") String userId) {
        try {
            return chatService.loadHistory(userId);
        } catch ( Exception e ) {
            logger.error("Load chat history error: {}", e.getMessage());
            return new HttpResponseEntity<List<Message>>().serverError(null);
        }
    }

    /**
     * 流式生成对话
     * @param message 对话问题
     * @return 流式输出对话结果
     */
    @GetMapping("/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatClient.stream(prompt);
    }
}
