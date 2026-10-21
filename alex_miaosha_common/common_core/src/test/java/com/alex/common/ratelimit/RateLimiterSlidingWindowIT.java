package com.alex.common.ratelimit;

import com.alex.common.annotations.rate.LimitType;
import com.alex.common.annotations.rate.RateLimiter;
import com.alex.common.aspect.RateLimiterAspect;
import com.alex.common.exception.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("分布式限流 RateLimiter 滑动时间窗口精准性集成测试")
class RateLimiterSlidingWindowIT {

    static class MockBusinessService {

        @RateLimiter(time = 1, count = 3, limitType = LimitType.GLOBAL, message = "全局接口访问过于频繁")
        public String executeOperation() {
            return "SUCCESS";
        }
    }

    private MockBusinessService proxiedService;
    private RateLimiterManager rateLimiterManager;

    @BeforeEach
    void setUp() {
        CaffeineFallbackRateLimiter fallbackLimiter = new CaffeineFallbackRateLimiter();
        rateLimiterManager = new RateLimiterManager(null, fallbackLimiter);
        RateLimitKeyResolver keyResolver = new RateLimitKeyResolver(null);

        MockBusinessService target = new MockBusinessService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        RateLimiterAspect aspect = new RateLimiterAspect(rateLimiterManager, keyResolver);
        factory.addAspect(aspect);
        proxiedService = factory.getProxy();
    }

    @Test
    @DisplayName("1. 滑动时间窗口内精确允许指定次数并在超限时抛出 RateLimitException")
    void testSlidingWindowThresholdEnforcement() {
        // 允许 3 次访问
        assertEquals("SUCCESS", proxiedService.executeOperation());
        assertEquals("SUCCESS", proxiedService.executeOperation());
        assertEquals("SUCCESS", proxiedService.executeOperation());

        // 第 4 次访问必须被拦截
        RateLimitException ex = assertThrows(RateLimitException.class, () -> {
            proxiedService.executeOperation();
        });

        assertEquals("429", ex.getCode());
        assertEquals("全局接口访问过于频繁", ex.getMessage());
        assertEquals(1, ex.getRetryAfterSeconds());
    }

    @Test
    @DisplayName("2. 时间窗口滚动后旧请求自动滑出窗口并恢复正常放行")
    void testSlidingWindowExpiryAndRecovery() throws InterruptedException {
        // 消耗完 3 次配额
        for (int i = 0; i < 3; i++) {
            assertEquals("SUCCESS", proxiedService.executeOperation());
        }

        // 立即发第 4 次触发限流
        assertThrows(RateLimitException.class, () -> proxiedService.executeOperation());

        // 等待 1100 毫秒（窗口为 1 秒）
        Thread.sleep(1100);

        // 窗口滚动后，历史请求已滑出窗口，应重新获得放行许可
        assertEquals("SUCCESS", proxiedService.executeOperation());
    }
}
