package com.alex.common.config;

import feign.Logger;
import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("微服务间 Feign 超时配置与非幂等重试防呆集成测试")
class FeignTimeoutAndRetrySecurityIT {

    private FeignConfig feignConfig;

    @BeforeEach
    void setUp() {
        feignConfig = new FeignConfig();
    }

    @Test
    @DisplayName("1. 验证全局默认连接超时 5s 与读取超时 10s")
    void options_configuresSafeDefaultTimeouts() {
        Request.Options options = feignConfig.options();
        assertNotNull(options);

        assertEquals(5000, options.connectTimeoutMillis());
        assertEquals(10000, options.readTimeoutMillis());
        assertTrue(options.isFollowRedirects());
    }

    @Test
    @DisplayName("2. 验证全局禁用自动重试（NEVER_RETRY），防止非幂等接口重复调用")
    void retryer_isConfiguredToNeverRetry() {
        Retryer retryer = feignConfig.retryer();
        assertNotNull(retryer);
        assertSame(Retryer.NEVER_RETRY, retryer);

        feign.Request dummyRequest = feign.Request.create(
                feign.Request.HttpMethod.POST,
                "http://alex-test",
                java.util.Collections.emptyMap(),
                feign.Request.Body.empty(),
                null
        );
        RetryableException ex = new RetryableException(
                503,
                "Service Unavailable",
                Request.HttpMethod.POST,
                new Date(),
                dummyRequest
        );

        assertThrows(RetryableException.class, () -> retryer.continueOrPropagate(ex));
    }

    @Test
    @DisplayName("3. 验证日志等级为 BASIC 兼顾性能与可排查性")
    void level_isBasic() {
        Logger.Level level = feignConfig.level();
        assertEquals(Logger.Level.BASIC, level);
    }

    @Test
    @DisplayName("4. 验证 RequestContextListener 正确加载")
    void requestContextListener_instantiatesSuccessfully() {
        assertNotNull(feignConfig.requestContextListener());
    }
}
