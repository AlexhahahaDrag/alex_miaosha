package com.alex.common.ratelimit;

import com.alex.common.annotations.rate.LimitType;
import com.alex.common.annotations.rate.RateLimiter;
import com.alex.common.aspect.RateLimiterAspect;
import com.alex.common.exception.RateLimitException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("分布式限流 RateLimiter 多维度（IP/USER/PARAM）隔离集成测试")
class RateLimiterMultiDimensionIT {

    static class MultiDimensionTargetService {

        @RateLimiter(time = 60, count = 2, limitType = LimitType.IP, message = "IP 限流拦截")
        public String executeByIp() {
            return "IP_SUCCESS";
        }

        @RateLimiter(time = 60, count = 2, limitType = LimitType.USER, message = "用户限流拦截")
        public String executeByUser() {
            return "USER_SUCCESS";
        }

        @RateLimiter(time = 60, count = 2, limitType = LimitType.PARAM, key = "#phone", message = "手机号限流拦截")
        public String sendSms(String phone) {
            return "SMS_SENT_" + phone;
        }
    }

    private MultiDimensionTargetService proxiedService;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        CaffeineFallbackRateLimiter fallbackLimiter = new CaffeineFallbackRateLimiter();
        RateLimiterManager rateLimiterManager = new RateLimiterManager(null, fallbackLimiter);
        RateLimitKeyResolver keyResolver = new RateLimitKeyResolver(null);

        MultiDimensionTargetService target = new MultiDimensionTargetService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        RateLimiterAspect aspect = new RateLimiterAspect(rateLimiterManager, keyResolver);
        factory.addAspect(aspect);
        proxiedService = factory.getProxy();

        mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/v1/test/operation");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("1. IP 维度限流：IP-A 超限被拦截，IP-B 配额独立互不干扰")
    void testIpDimensionIsolation() {
        // IP-A 发起 2 次请求成功
        mockRequest.setRemoteAddr("192.168.1.101");
        assertEquals("IP_SUCCESS", proxiedService.executeByIp());
        assertEquals("IP_SUCCESS", proxiedService.executeByIp());

        // IP-A 第 3 次应被拦截
        assertThrows(RateLimitException.class, () -> proxiedService.executeByIp());

        // 切换为 IP-B，配额应完全独立并正常放行
        mockRequest.setRemoteAddr("192.168.1.102");
        assertEquals("IP_SUCCESS", proxiedService.executeByIp());
        assertEquals("IP_SUCCESS", proxiedService.executeByIp());
        assertThrows(RateLimitException.class, () -> proxiedService.executeByIp());
    }

    @Test
    @DisplayName("2. 用户维度限流：UID-1001 超限被拦截，UID-1002 独立放行")
    void testUserDimensionIsolation() {
        // 用户 1001 发起 2 次请求
        mockRequest.addHeader("userId", "1001");
        assertEquals("USER_SUCCESS", proxiedService.executeByUser());
        assertEquals("USER_SUCCESS", proxiedService.executeByUser());
        assertThrows(RateLimitException.class, () -> proxiedService.executeByUser());

        // 切换为用户 1002
        MockHttpServletRequest requestUser2 = new MockHttpServletRequest();
        requestUser2.setRequestURI("/api/v1/test/operation");
        requestUser2.addHeader("userId", "1002");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestUser2));

        assertEquals("USER_SUCCESS", proxiedService.executeByUser());
        assertEquals("USER_SUCCESS", proxiedService.executeByUser());
        assertThrows(RateLimitException.class, () -> proxiedService.executeByUser());
    }

    @Test
    @DisplayName("3. 业务参数 SpEL 维度限流：手机号 A 超限，手机号 B 正常放行")
    void testParamSpelDimensionIsolation() {
        // 手机号 13800000000 发起 2 次请求
        assertEquals("SMS_SENT_13800000000", proxiedService.sendSms("13800000000"));
        assertEquals("SMS_SENT_13800000000", proxiedService.sendSms("13800000000"));
        assertThrows(RateLimitException.class, () -> proxiedService.sendSms("13800000000"));

        // 手机号 13999999999 配额独立
        assertEquals("SMS_SENT_13999999999", proxiedService.sendSms("13999999999"));
        assertEquals("SMS_SENT_13999999999", proxiedService.sendSms("13999999999"));
        assertThrows(RateLimitException.class, () -> proxiedService.sendSms("13999999999"));
    }
}
