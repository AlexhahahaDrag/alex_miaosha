package com.alex.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 全局微服务链路追踪 TraceId 过滤器
 * 职责：
 * 1. 提取客户端已带 TraceId（X-Trace-Id / X-Request-Id）；
 * 2. 若未带或为空，生成高熵 32 位全局唯一 TraceId；
 * 3. 注入下游微服务请求头，并在网关对外响应头中 100% 回写 X-Trace-Id。
 */
@Component
@Slf4j
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 1. 提取或生成 TraceId
        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        }
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 2. 注入下游请求头
        String finalTraceId = traceId;
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TRACE_ID_HEADER, finalTraceId)
                .build();

        // 3. 回写对外响应头
        exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, finalTraceId);

        log.debug("链路追踪注入完成: path={}, traceId={}", request.getPath(), finalTraceId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // 最高优先级，保证在所有业务过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
