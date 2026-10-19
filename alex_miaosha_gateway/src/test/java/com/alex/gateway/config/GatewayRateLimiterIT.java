package com.alex.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 网关多维度限流 KeyResolver 集成与契约测试
 */
class GatewayRateLimiterIT {

    private final RateLimiterConfiguration config = new RateLimiterConfiguration();

    @Test
    @DisplayName("IP 限流：多级反向代理 X-Forwarded-For 逗号分隔时提取首个真实 IP")
    void ipKeyResolver_withXForwardedFor_shouldExtractFirstClientIp() {
        KeyResolver resolver = config.ipKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/test")
                .header("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals("203.0.113.195", key);
    }

    @Test
    @DisplayName("IP 限流：X-Real-IP 代理头正常提取")
    void ipKeyResolver_withXRealIp_shouldExtractIp() {
        KeyResolver resolver = config.ipKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-finance/test")
                .header("X-Real-IP", "198.51.100.42")
                .remoteAddress(new InetSocketAddress("10.0.0.2", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals("198.51.100.42", key);
    }

    @Test
    @DisplayName("IP 限流：无代理头时从 RemoteAddress 获取 Host 地址")
    void ipKeyResolver_withRemoteAddress_shouldExtractIp() {
        KeyResolver resolver = config.ipKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-product/test")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals("192.168.1.100", key);
    }

    @Test
    @DisplayName("IP 限流：极值边界-无代理头且无 RemoteAddress 时回退为 unknown")
    void ipKeyResolver_withNullAddress_shouldFallbackToUnknown() {
        KeyResolver resolver = config.ipKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-oss/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals(RateLimiterConfiguration.UNKNOWN_IP, key);
        assertEquals(RateLimiterConfiguration.UNKNOWN_IP, RateLimiterConfiguration.resolveClientIp(null));
    }

    @Test
    @DisplayName("用户限流：默认 Authorization Header 正确提取凭据")
    void userKeyResolver_withDefaultAuthorization_shouldExtractToken() {
        GatewayAudience audience = new GatewayAudience();
        KeyResolver resolver = config.userKeyResolver(audience);

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/api/v1/user/info")
                .header("Authorization", "Bearer test-jwt-token-abcdef")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals("Bearer test-jwt-token-abcdef", key);
    }

    @Test
    @DisplayName("用户限流：自定义 tokenHeader 配置正确提取")
    void userKeyResolver_withCustomTokenHeader_shouldExtractToken() {
        GatewayAudience audience = new GatewayAudience();
        audience.setTokenHeader("X-Access-Token");
        KeyResolver resolver = config.userKeyResolver(audience);

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-finance/test")
                .header("X-Access-Token", "custom-token-987654")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals("custom-token-987654", key);
    }

    @Test
    @DisplayName("用户限流：无 Token 时优雅兜底为 anonymous 匿名标识")
    void userKeyResolver_withoutToken_shouldFallbackToAnonymous() {
        GatewayAudience audience = new GatewayAudience();
        KeyResolver resolver = config.userKeyResolver(audience);

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/api/v1/user/info").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals(RateLimiterConfiguration.ANONYMOUS_USER, key);
    }

    @Test
    @DisplayName("API 路径限流：正确提取规范请求路径")
    void apiKeyResolver_shouldExtractRequestPath() {
        KeyResolver resolver = config.apiKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-ai/api/v1/ai/chat/stream").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        String key = resolver.resolve(exchange).block();
        assertEquals("/am-ai/api/v1/ai/chat/stream", key);
    }
}
