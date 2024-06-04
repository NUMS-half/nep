package com.neusoft.neu24.gateway.filter;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.gateway.config.AuthProperties;
import com.neusoft.neu24.gateway.config.JwtProperties;
import com.neusoft.neu24.gateway.utils.JwtUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.neusoft.neu24.config.RedisConstants.LOGIN_TOKEN;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 请求路径配置信息(构造函数注入)
     */
    private final AuthProperties authProperties;

    private final JwtProperties jwtProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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


//        // TODO 5. 刷新token与redis中的token过期时间
////        String oldToken = token;
//        // 5.1 刷新 token 有效期
//        String newToken = jwtUtil.refreshToken(token, jwtProperties.getTokenTTL());
//        // 5.2 刷新 Redis 中数据的有效期
//        // 5.2.1 获取 Redis 中的用户数据
////        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_TOKEN + userId);
////        if ( !userMap.isEmpty() ) {
//            // 5.2.2 刷新新token的Redis数据过期时间
//            stringRedisTemplate.expire(LOGIN_TOKEN + userId, jwtProperties.getTokenTTL());
////        }
//
        // 6. 传递用户信息到后端服务
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header("userId", userId)) // 传递用户信息
//                .request(builder -> builder.header("Authorization", newToken)) // 传递新token
                .build();

        // 7. 放行
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
     *
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
