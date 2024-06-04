package com.neusoft.neu24.ai.controller;

import com.neusoft.neu24.entity.HttpResponseEntity;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController("/ai")
public class ChatController {

    @Resource
    private OpenAiChatClient chatClient;

    /**
     * 直接生成对话
     * @param message 对话问题
     * @return 对话结果
     */
    @GetMapping("/generate")
    public HttpResponseEntity<String> generate(@RequestParam(value = "message", defaultValue = "请用中文为我解释一下空气质量指数AQI是什么") String message) {
        return new HttpResponseEntity<String>().success(chatClient.call(message));
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
