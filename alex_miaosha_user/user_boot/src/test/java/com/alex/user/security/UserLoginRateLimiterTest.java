package com.alex.user.security;

import com.alex.base.common.Result;
import com.alex.common.annotations.rate.LimitType;
import com.alex.common.annotations.rate.RateLimiter;
import com.alex.common.aspect.RateLimiterAspect;
import com.alex.common.exception.RateLimitException;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import com.alex.common.ratelimit.RateLimitKeyResolver;
import com.alex.common.ratelimit.RateLimiterManager;
import com.alex.user.user.controller.TUserController;
import com.alex.user.user.service.TUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户微服务 TUserController @RateLimiter 登录防刷与暴力撞库防御测试
 */
@ExtendWith(MockitoExtension.class)
class UserLoginRateLimiterTest {

    @Mock
    private TUserService tUserService;

    @Mock
    private RateLimiterManager rateLimiterManager;

    @Mock
    private RateLimitKeyResolver keyResolver;

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("验证 TUserController.doLogin 上的 @RateLimiter 属性契约: IP 维度 + 60s/5次限制")
    void testLoginRateLimiterAnnotationContract() throws Exception {
        Method doLoginMethod = TUserController.class.getMethod("doLogin",
                javax.servlet.http.HttpServletRequest.class, String.class, String.class, Boolean.class);
        RateLimiter rateLimiter = doLoginMethod.getAnnotation(RateLimiter.class);

        assertNotNull(rateLimiter, "doLogin 必须标注 @RateLimiter");
        assertEquals(LimitType.IP, rateLimiter.limitType());
        assertEquals(5, rateLimiter.count());
        assertEquals(60, rateLimiter.time());
        assertEquals("登录过于频繁，请稍后再试", rateLimiter.message());
    }

    @Test
    @DisplayName("测试用户登录接口在同一 IP 连续请求超频时触发 RateLimitException (HTTP 429)")
    void testLoginRateLimiterBlockedOnExceed() throws Exception {
        TUserController target = new TUserController(tUserService);
        RateLimiterAspect aspect = new RateLimiterAspect(rateLimiterManager, keyResolver);

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        TUserController proxy = factory.getProxy();

        when(keyResolver.resolveKey(any(Method.class), any(), any(RateLimiter.class), any()))
                .thenReturn("rate_limit:ip:192.168.1.100:login");
        when(rateLimiterManager.tryAcquire(eq("rate_limit:ip:192.168.1.100:login"), eq(60), eq(5)))
                .thenReturn(true)
                .thenReturn(false);

        when(tUserService.login(any(), eq("alex"), eq("123456"), eq(false)))
                .thenReturn(Collections.singletonMap("token", "mock_token"));

        MockHttpServletRequest request = new MockHttpServletRequest();

        // 第一次调用：成功放行
        Result<Map<String, Object>> successResult = proxy.doLogin(request, "alex", "123456", false);
        assertNotNull(successResult);
        verify(tUserService, times(1)).login(any(), eq("alex"), eq("123456"), eq(false));

        // 第二次调用：超频被切面拦截
        RateLimitException ex = assertThrows(RateLimitException.class,
                () -> proxy.doLogin(request, "alex", "123456", false));
        assertEquals("登录过于频繁，请稍后再试", ex.getMessage());
        assertEquals(60, ex.getRetryAfterSeconds());

        // 验证全局异常拦截器处理为 429
        ResponseEntity<Result<String>> responseEntity = exceptionHandler.handle(ex);
        assertEquals(429, responseEntity.getStatusCodeValue());
        assertEquals("60", responseEntity.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
        assertEquals("登录过于频繁，请稍后再试", responseEntity.getBody().getMessage());
    }
}