package com.neusoft.neu24.gateway.filter;

import com.neusoft.neu24.gateway.config.PermissionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(PermissionGlobalFilter.class);

    private final PermissionProperties permissionProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1. 获取请求路径
        ServerHttpRequest request = exchange.getRequest();

        Integer roleId = null;
        List<String> headers = request.getHeaders().get("roleId");
        if ( headers != null && !headers.isEmpty() ) {
            roleId = Integer.parseInt(headers.get(0));
        }

        logger.info("请求的角色ID: {}", roleId);

        // 2. 判断路径是否需要登录拦截，不需要时直接放行
        if ( isExcluded(request.getPath().toString()) ) {
            return chain.filter(exchange);
        }


        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
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
        for ( String pattern : permissionProperties.getExcludePaths() ) {
            if ( antPathMatcher.match(pattern, path) ) {
                return true;
            }
        }
        return false;
    }
}
