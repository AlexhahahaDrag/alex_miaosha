package com.alex.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试套件 1:
 * 网关全局微服务链路追踪 TraceId 生成、注入与透传测试
 */
class GatewayTraceIdPropagationIT {

    private TraceIdGlobalFilter traceIdFilter;

    @BeforeEach
    void setUp() {
        traceIdFilter = new TraceIdGlobalFilter();
    }

    @Test
    @DisplayName("TraceId 生成与注入: 客户端未传 TraceId 时自动生成 32 位唯一 ID 并透传下游")
    void filter_whenTraceIdMissing_generatesUniqueTraceIdAndPropagates() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-user/api/v1/user/info")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> downstreamTraceId = new AtomicReference<>();
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamTraceId.set(mutatedExchange.getRequest().getHeaders().getFirst(TraceIdGlobalFilter.TRACE_ID_HEADER));
            return Mono.empty();
        };

        traceIdFilter.filter(exchange, chain).block();

        String traceId = downstreamTraceId.get();
        assertNotNull(traceId, "下游请求头必须注入 X-Trace-Id");
        assertEquals(32, traceId.length(), "生成的 TraceId 必须是 32 位 Hex");

        // 验证对外响应头回写了 X-Trace-Id
        String responseTraceId = exchange.getResponse().getHeaders().getFirst(TraceIdGlobalFilter.TRACE_ID_HEADER);
        assertEquals(traceId, responseTraceId, "对外响应头必须回写一致的 X-Trace-Id");
    }

    @Test
    @DisplayName("TraceId 忠实透传: 客户端已带合规 X-Trace-Id 时保持原样透传下游")
    void filter_whenTraceIdProvided_faithfullyPropagatesExisting() {
        String existingTraceId = "my-custom-trace-uuid-999";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-finance/api/v1/gift/page")
                .header(TraceIdGlobalFilter.TRACE_ID_HEADER, existingTraceId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> downstreamTraceId = new AtomicReference<>();
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamTraceId.set(mutatedExchange.getRequest().getHeaders().getFirst(TraceIdGlobalFilter.TRACE_ID_HEADER));
            return Mono.empty();
        };

        traceIdFilter.filter(exchange, chain).block();

        assertEquals(existingTraceId, downstreamTraceId.get(), "现有 TraceId 必须保持原样透传");
        assertEquals(existingTraceId, exchange.getResponse().getHeaders().getFirst(TraceIdGlobalFilter.TRACE_ID_HEADER));
    }

    @Test
    @DisplayName("兼容性透传: 客户端传入 X-Request-Id 时能平滑采纳为 TraceId")
    void filter_whenRequestIdProvided_usesRequestIdAsTraceId() {
        String requestId = "req-id-8888";
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/am-ai/api/v1/chat")
                .header(TraceIdGlobalFilter.REQUEST_ID_HEADER, requestId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> downstreamTraceId = new AtomicReference<>();
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamTraceId.set(mutatedExchange.getRequest().getHeaders().getFirst(TraceIdGlobalFilter.TRACE_ID_HEADER));
            return Mono.empty();
        };

        traceIdFilter.filter(exchange, chain).block();

        assertEquals(requestId, downstreamTraceId.get(), "应当平滑兼容 X-Request-Id");
        assertEquals(requestId, exchange.getResponse().getHeaders().getFirst(TraceIdGlobalFilter.TRACE_ID_HEADER));
    }

    @Test
    @DisplayName("优先级校验: TraceId 过滤器必须为 HIGHEST_PRECEDENCE 保证优先于所有业务过滤器")
    void order_isHighestPrecedence() {
        assertEquals(Ordered.HIGHEST_PRECEDENCE, traceIdFilter.getOrder());
    }
}
