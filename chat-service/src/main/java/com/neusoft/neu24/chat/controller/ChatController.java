package com.neusoft.neu24.chat.controller;

import cn.hutool.core.bean.BeanUtil;
import com.neusoft.neu24.chat.service.IChatService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Message;
import com.neusoft.neu24.chat.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

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
        map.put("userId", jwtUtil.getUserIdByToken((String) map.get("token")));
        map.remove("token");
        // 1. 获取用户发送的消息并保存
        Message sendMessage = BeanUtil.fillBeanWithMap(map, new Message(), false);
        // 消息不能为空
        if( sendMessage.getContent().isEmpty() ) {
            return HttpResponseEntity.CONTENT_NULL;
        }
        sendMessage.setRecipient(0); // 系统接收的消息
        HttpResponseEntity httpResponseEntity =  chatService.saveChatMessage(sendMessage);

        // 2. 调用AI接口生成对话
        String generate = chatClient.call(sendMessage.getContent());
        // 3. 保存AI生成的消息
        Message generateMessage = new Message();
        generateMessage.setMessageId(null);
        generateMessage.setUserId(sendMessage.getUserId());
        generateMessage.setRecipient(1); // 用户接收的消息
        generateMessage.setContent(generate);
        HttpResponseEntity httpResponseEntity1 =  chatService.saveChatMessage(generateMessage);

        return new HttpResponseEntity<String>().success(generate);
    }

    /**
     * 根据用户加载历史消息
     * @param token 用户token
     * @return 历史消息列表
     */
    @PostMapping(value = "/load")
    public HttpResponseEntity<List<Message>> loadHistory(@RequestParam("token") String token) {
        String userId = jwtUtil.getUserIdByToken(token);
        System.out.println("/chat/load:从token中解析出的userId:" + userId);
        return chatService.loadHistory(userId);
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
