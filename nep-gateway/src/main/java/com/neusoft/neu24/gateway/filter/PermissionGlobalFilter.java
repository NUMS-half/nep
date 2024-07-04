package com.neusoft.neu24.gateway.filter;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.gateway.config.PermissionProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Component
@EnableConfigurationProperties(PermissionProperties.class)
public class PermissionGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(PermissionGlobalFilter.class);

    private PermissionProperties permissionProperties;

    private WebClient.Builder webClientBuilder;

    private PermissionGlobalFilter() {}

    @Autowired
    public PermissionGlobalFilter(WebClient.Builder webClientBuilder, PermissionProperties permissionProperties) {
        this.permissionProperties = permissionProperties;
//        this.webClient = webClientBuilder.baseUrl(permissionProperties.getRoleServiceBaseUrl()).build();
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1. 获取请求
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getPath().toString();
        String method = request.getMethod().name();

//        // 如果是服务内部调用，则直接放行
//        String apiKey = request.getHeaders().getFirst("Api-Key");
//        if ( apiKey != null && apiKey.equals(FEIGN_SECRET_KEY) ) {
//            return chain.filter(exchange);
//        }

        Integer roleId = null;
        List<String> roles = request.getHeaders().get("roleId");
        if ( roles != null && !roles.isEmpty() ) {
            roleId = Integer.parseInt(roles.get(0));
        }

        logger.info("当前请求为: {} {} , 角色ID为: {}", method, path, roleId);

        // 2. 判断路径是否需要登录拦截，不需要时直接放行
        if ( isExcluded(path) ) {
            return chain.filter(exchange);
        }

        return webClientBuilder.build().get()
                .uri("http://role-service/role/select?roleId={roleId}", roleId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<HttpResponseEntity<List<SystemNode>>>() {})
                .flatMap(response -> {
                    if ( response.getCode() != 200 ) {
                        ServerHttpResponse serverHttpResponse = exchange.getResponse();
                        serverHttpResponse.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        logger.error("获取权限列表失败");
                        return serverHttpResponse.setComplete();
                    }

                    List<SystemNode> list = response.getData();
                    for ( SystemNode node : list ) {
                        if ( node.getMethod() == null || node.getPath() == null ) {
                            continue;
                        }
                        if ( node.getMethod().equals(method) && node.getPath().equals(path) ) {
                            logger.info("当前角色权限校验通过, Method: {}, Path: {}", method, path);
                            return chain.filter(exchange);
                        }
                    }
                    ServerHttpResponse serverHttpResponse = exchange.getResponse();
                    serverHttpResponse.setStatusCode(HttpStatus.FORBIDDEN);
                    logger.info("当前角色无权限访问, Method: {}, Path: {}", method, path);
                    return serverHttpResponse.setComplete();
                })
                .onErrorResume(e -> {
                    logger.error("获取权限时发生异常", e);
                    ServerHttpResponse serverHttpResponse = exchange.getResponse();
                    serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return serverHttpResponse.setComplete();
                });
    }

    @Override
    public int getOrder() {
        return 1;
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
