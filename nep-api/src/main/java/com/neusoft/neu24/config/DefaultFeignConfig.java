package com.neusoft.neu24.config;

import com.neusoft.neu24.client.fallback.UserClientFallbackFactory;
import com.neusoft.neu24.utils.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.neusoft.neu24.config.FeignConstants.FEIGN_SECRET_KEY;

@Configuration
public class DefaultFeignConfig {

    /**
     * <b>Feign请求拦截器,传递用户信息<b/>
     */
    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("userId", UserContext.getUser());
//            requestTemplate.header("Api-Key", FEIGN_SECRET_KEY);
        };
    }

    /**
     * <b>User Service Feign请求失败回调工厂<b/>
     */
    @Bean
    public UserClientFallbackFactory userClientFallbackFactory() {
        return new UserClientFallbackFactory();
    }

}
