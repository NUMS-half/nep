package com.neusoft.neu24.gateway.filter;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.gateway.config.AuthProperties;
import com.neusoft.neu24.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 请求路径配置信息(构造函数注入)
     */
    private final AuthProperties authProperties;

    /**
     * Jwt工具类(构造函数注入)
     */
    private final JwtUtil jwtUtil;

    /**
     * 过滤逻辑
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1. 获取请求路径
        ServerHttpRequest request = exchange.getRequest();

        // 2. 判断路径是否需要登录拦截，不需要时直接放行
        if ( isExcluded(request.getPath().toString()) ) {
            return chain.filter(exchange);
        }

        // 3. 获取请求头中的token
        String token = null;
        List<String> headers = request.getHeaders().get("Authorization");
        if ( headers != null && !headers.isEmpty() ) {
            token = headers.get(0);
        }

        // 4. 校验token是否有效
        HttpResponseEntity<String> response = jwtUtil.parseToken(token);
        if ( response.getCode() != 200 ) {
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            return serverHttpResponse.setComplete();
        }
        String userId = response.getData();

        // 5. 传递用户信息到后端服务
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header("userId", userId)) // 传递用户信息
                .build();

        // 6. 放行
        return chain.filter(newExchange);
    }

    /**
     * <b>设置过滤器优先级，保证在服务转发之前执行拦截<b/>
     *
     * @return 优先级
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 判断请求路径是否需要登录拦截
     * @param path 请求路径
     * @return 是否需要登录拦截
     */
    private boolean isExcluded(String path) {
        // 校验带有通配符的路径字符串
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        for ( String pattern : authProperties.getExcludePaths() ) {
            if ( antPathMatcher.match(pattern, path) ) {
                return true;
            }
        }
        return false;
    }
}
