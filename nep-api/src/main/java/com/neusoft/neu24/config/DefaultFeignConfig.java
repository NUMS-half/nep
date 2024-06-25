package com.neusoft.neu24.config;

import com.neusoft.neu24.client.fallback.UserClientFallbackFactory;
import com.neusoft.neu24.utils.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultFeignConfig {

    /**
     * <b>Feign请求拦截器,传递用户信息<b/>
     */
    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        return requestTemplate -> requestTemplate.header("userId", UserContext.getUser());
    }

    /**
     * <b>Feign请求失败回调工厂<b/>
     */
    @Bean
    public UserClientFallbackFactory userClientFallbackFactory() {
        return new UserClientFallbackFactory();
    }

}
