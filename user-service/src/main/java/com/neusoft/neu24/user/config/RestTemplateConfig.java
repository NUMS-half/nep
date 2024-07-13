package com.neusoft.neu24.user.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * <b>RestTemplate 配置类</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建 RestTemplate 实例，并存入 spring 容器中
     */
    @Bean
    @LoadBalanced // 开启负载均衡
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
