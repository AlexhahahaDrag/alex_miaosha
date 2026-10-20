package com.alex.gateway.filter;

import com.alex.gateway.config.GatewayAudience;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试套件 2:
 * 网关全局 IP 黑名单快速短路拦截与代理穿透解析测试
 */
class GatewayIpBlacklistSecurityIT {

    private GatewayAudience audience;
    private IpBlacklistGlobalFilter ipFilter;

    @BeforeEach
    void setUp() {
        audience = new GatewayAudience();
        audience.setIpBlackList(List.of("192.168.1.100", "10.0.0.*", "203.0.113.88"));
        ipFilter = new IpBlacklistGlobalFilter(audience);
    }

    @Test
    @DisplayName("黑名单拦截: 命中单 IP 黑名单时返回 403 并且短路下游过滤器链")
    void filter_whenIpInBlacklist_blocksWith403AndShortCircuits() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-user/api/v1/user/info")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean downstreamCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamCalled.set(true);
            return Mono.empty();
        };

        ipFilter.filter(exchange, chain).block();

        // 验证下游被短路
        assertFalse(downstreamCalled.get(), "黑名单 IP 必须阻断，严禁调用下游过滤器链");
        // 验证返回状态码为 403
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("通配符匹配: 命中通配符前缀 (10.0.0.*) 的 IP 被成功拦截阻断")
    void filter_whenIpMatchesWildcardPattern_blocks() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/am-finance/api/v1/gift/add")
                .remoteAddress(new InetSocketAddress("10.0.0.55", 9090))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean downstreamCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamCalled.set(true);
            return Mono.empty();
        };

        ipFilter.filter(exchange, chain).block();

        assertFalse(downstreamCalled.get());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("合法放行: 未在黑名单中的合法客户端 IP 顺利通过进入后续链条")
    void filter_whenIpNotInBlacklist_proceedsNormally() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-user/api/v1/user/list")
                .remoteAddress(new InetSocketAddress("172.16.0.1", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean downstreamCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamCalled.set(true);
            return Mono.empty();
        };

        ipFilter.filter(exchange, chain).block();

        assertTrue(downstreamCalled.get(), "合法 IP 必须正常放行至下游链条");
        assertNotEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("多级代理解析: X-Forwarded-For 多级代理时准确提取第一个真实 IP 并拦截")
    void filter_resolvesRealIpFromXForwardedForMultiProxy() {
        // 第一个为黑名单 IP: 203.0.113.88，后续为中继代理 IP
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-oss/api/v1/file/upload")
                .header("X-Forwarded-For", "203.0.113.88, 198.51.100.1, 10.244.0.1")
                .remoteAddress(new InetSocketAddress("10.244.0.1", 80))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean downstreamCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamCalled.set(true);
            return Mono.empty();
        };

        ipFilter.filter(exchange, chain).block();

        assertFalse(downstreamCalled.get(), "多级反向代理首个真实 IP 命中黑名单必须被拦截");
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("空配置防御: 未配置黑名单时平滑放行所有请求")
    void filter_whenIpBlacklistEmpty_allowsAll() {
        audience.setIpBlackList(Collections.emptyList());

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/am-user/api/v1/user/info")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean downstreamCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = mutatedExchange -> {
            downstreamCalled.set(true);
            return Mono.empty();
        };

        ipFilter.filter(exchange, chain).block();

        assertTrue(downstreamCalled.get());
    }

    @Test
    @DisplayName("优先级校验: IP 过滤器优先级紧随 TraceId 之后 (HIGHEST_PRECEDENCE + 1)")
    void order_isImmediatelyAfterTraceId() {
        assertEquals(Ordered.HIGHEST_PRECEDENCE + 1, ipFilter.getOrder());
    }
}
