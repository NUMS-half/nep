package com.neusoft.neu24.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * NEP 跨域配置类
 */
@Configuration
public class NepCorsConfig {

    /**
     * 跨域过滤器
     * @return CorsWebFilter
     */
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");  // 是什么请求方法，GET POST PUT DELETE ...
        config.addAllowedOrigin("*");  // 来自哪个域名的请求，*号表示所有
        config.addAllowedHeader("*");  // 是什么请求头
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
