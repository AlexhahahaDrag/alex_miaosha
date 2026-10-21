package com.alex.common.ratelimit;

import com.alex.base.common.Result;
import com.alex.common.exception.RateLimitException;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("分布式限流 Redis 故障自动优雅降级与 HTTP 429 协议契约集成测试")
class RateLimiterFallbackAndSecurityIT {

    @Test
    @DisplayName("1. Redis 宕机或网络异常时，系统自动优雅降级为本地 Caffeine 限流保护业务不中断")
    void testRedisFailureGracefulFallbackToCaffeine() {
        RedisSlidingWindowRateLimiter brokenRedisLimiter = mock(RedisSlidingWindowRateLimiter.class);
        // 模拟 Redis 执行限流时发生连接失败或超时异常
        when(brokenRedisLimiter.tryAcquire(anyString(), anyInt(), anyInt()))
                .thenThrow(new RedisConnectionFailureException("Redis 服务连接拒绝或网络分区"));

        CaffeineFallbackRateLimiter localLimiter = new CaffeineFallbackRateLimiter();

        RateLimiterManager manager = new RateLimiterManager(brokenRedisLimiter, localLimiter);

        String testKey = "ratelimit:fallback:test";
        int window = 60;
        int limit = 2;

        // 尽管 Redis 报错，Manager 必须捕获并降级到本地限流器，允许前 2 次访问
        assertTrue(manager.tryAcquire(testKey, window, limit), "第 1 次访问应由降级本地限流器放行");
        assertTrue(manager.tryAcquire(testKey, window, limit), "第 2 次访问应由降级本地限流器放行");

        // 本地限流器在第 3 次成功拦截超限流量
        assertFalse(manager.tryAcquire(testKey, window, limit), "第 3 次访问应被降级本地限流器拦截");
    }

    @Test
    @DisplayName("2. GlobalExceptionHandler 捕获 RateLimitException 时精确映射 HTTP 429 与 Retry-After 响应头")
    void testRateLimitExceptionProducesHttp429WithRetryAfterHeader() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RateLimitException exception = new RateLimitException("429", "接口访问频次超限，请稍后重试", 45);

        ResponseEntity<Result<String>> response = handler.handle(exception);

        assertNotNull(response);
        assertEquals(429, response.getStatusCodeValue(), "HTTP 响应状态码必须为 429 Too Many Requests");

        HttpHeaders headers = response.getHeaders();
        assertEquals("45", headers.getFirst(HttpHeaders.RETRY_AFTER), "响应头中必须回显 Retry-After 等待秒数");

        Result<String> body = response.getBody();
        assertNotNull(body);
        assertEquals("429", body.getCode());
        assertEquals("接口访问频次超限，请稍后重试", body.getMessage());
    }
}
