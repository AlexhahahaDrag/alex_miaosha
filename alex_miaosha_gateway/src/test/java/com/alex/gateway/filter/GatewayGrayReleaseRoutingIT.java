package com.alex.gateway.filter;

import com.alex.gateway.config.GatewayAudience;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试套件 3:
 * 网关全局灰度发布与金丝雀（Canary）流量染色及版本路由透传测试
 */
class GatewayGrayReleaseRoutingIT {

    private GatewayAudience audience;
    private GrayReleaseGlobalFilter grayFilter;

    @BeforeEach
    void setUp() {
        audience = new GatewayAudience();
        grayFilter = new GrayReleaseGlobalFilter(audience);
    }

    @Test
    @DisplayName("灰度流量染色: 携带默认 X-Gray-Tag: gray 时注入 X-Route-Tag: gray 与版本号")
    void filter_whenGrayTagPresent_injectsGrayRouteAndVersion() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-user/api/v1/user/info")
                .header(GrayReleaseGlobalFilter.DEFAULT_GRAY_HEADER, "gray")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> routeTagRef = new AtomicReference<>();
        AtomicReference<String> serviceVersionRef = new AtomicReference<>();

        GatewayFilterChain chain = mutatedExchange -> {
            routeTagRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.ROUTE_TAG_HEADER));
            serviceVersionRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.SERVICE_VERSION_HEADER));
            return Mono.empty();
        };

        grayFilter.filter(exchange, chain).block();

        assertEquals(GrayReleaseGlobalFilter.TAG_GRAY, routeTagRef.get(), "路由标签必须染色为 gray");
        assertEquals("gray", serviceVersionRef.get(), "服务版本必须染色为 gray");
    }

    @Test
    @DisplayName("定制版本染色: 携带具体版本号 (如 v2.5.0) 时精准透传该版本号")
    void filter_whenSpecificVersionTagPresent_injectsCustomVersion() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/am-finance/api/v1/gift/add")
                .header(GrayReleaseGlobalFilter.DEFAULT_GRAY_HEADER, "v2.5.0")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> routeTagRef = new AtomicReference<>();
        AtomicReference<String> serviceVersionRef = new AtomicReference<>();

        GatewayFilterChain chain = mutatedExchange -> {
            routeTagRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.ROUTE_TAG_HEADER));
            serviceVersionRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.SERVICE_VERSION_HEADER));
            return Mono.empty();
        };

        grayFilter.filter(exchange, chain).block();

        assertEquals(GrayReleaseGlobalFilter.TAG_GRAY, routeTagRef.get());
        assertEquals("v2.5.0", serviceVersionRef.get());
    }

    @Test
    @DisplayName("平滑兜底: 未带灰度标记或标记为 default 时规范注入 default 标签")
    void filter_whenNoGrayTag_injectsDefaultTag() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-user/api/v1/user/page")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> routeTagRef = new AtomicReference<>();
        AtomicReference<String> serviceVersionRef = new AtomicReference<>();

        GatewayFilterChain chain = mutatedExchange -> {
            routeTagRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.ROUTE_TAG_HEADER));
            serviceVersionRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.SERVICE_VERSION_HEADER));
            return Mono.empty();
        };

        grayFilter.filter(exchange, chain).block();

        assertEquals(GrayReleaseGlobalFilter.TAG_DEFAULT, routeTagRef.get());
        assertEquals(GrayReleaseGlobalFilter.TAG_DEFAULT, serviceVersionRef.get());
    }

    @Test
    @DisplayName("自定义头解析: 配置自定义灰度头时能正确识别并完成染色")
    void filter_whenCustomHeaderConfigured_resolvesCustomHeader() {
        audience.setGrayHeaderName("X-Canary-Channel");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-ai/api/v1/chat")
                .header("X-Canary-Channel", "beta-vip")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> routeTagRef = new AtomicReference<>();
        AtomicReference<String> serviceVersionRef = new AtomicReference<>();

        GatewayFilterChain chain = mutatedExchange -> {
            routeTagRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.ROUTE_TAG_HEADER));
            serviceVersionRef.set(mutatedExchange.getRequest().getHeaders().getFirst(GrayReleaseGlobalFilter.SERVICE_VERSION_HEADER));
            return Mono.empty();
        };

        grayFilter.filter(exchange, chain).block();

        assertEquals(GrayReleaseGlobalFilter.TAG_GRAY, routeTagRef.get());
        assertEquals("beta-vip", serviceVersionRef.get());
    }

    @Test
    @DisplayName("优先级校验: 灰度过滤器必须在常规业务鉴权过滤器之前执行")
    void order_isEarlierThanAuth() {
        // HIGHEST_PRECEDENCE + 2 < -2 (GatewayFilter)
        assertTrue(grayFilter.getOrder() < -2, "灰度过滤器顺序必须先于常规业务鉴权过滤器");
    }
}
