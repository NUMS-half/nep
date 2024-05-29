package com.neusoft.neu24.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {
    /**
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        // TODO: 实现登录校验逻辑
//        // exchange 中存储请求的上下文全部信息
//        // 登录的token信息一般存储在请求的headers中
//        ServerHttpRequest request = (ServerHttpRequest) exchange.getRequest();
//        HttpHeaders headers = request.getHeaders();
//        // 对获取的header进行操作
//        System.out.println(headers);
        // 放行
        return chain.filter(exchange);
    }

    /**
     * <b>设置过滤器优先级，保证在服务转发之前执行拦截<b/>
     *
     * @return 优先级
     */
    public int getOrder() {
        // 优先级为int类型的数据，越小优先级越高
        return 0;
    }
}
