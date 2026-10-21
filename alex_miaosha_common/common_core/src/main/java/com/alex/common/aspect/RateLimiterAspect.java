package com.alex.common.aspect;

import com.alex.common.annotations.rate.RateLimiter;
import com.alex.common.exception.RateLimitException;
import com.alex.common.handler.RequestHolder;
import com.alex.common.ratelimit.RateLimitKeyResolver;
import com.alex.common.ratelimit.RateLimiterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 分布式限流与防刷切面
 */
@Aspect
@Order(10) // 优先级保证在业务 Controller 之前拦截
@Slf4j
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final RateLimiterManager rateLimiterManager;
    private final RateLimitKeyResolver keyResolver;

    @Around("@annotation(rateLimiter)")
    public Object aroundRateLimiter(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        HttpServletRequest request = resolveRequest();
        String limitKey = keyResolver.resolveKey(method, args, rateLimiter, request);

        int time = rateLimiter.time();
        int count = rateLimiter.count();

        boolean allowed = rateLimiterManager.tryAcquire(limitKey, time, count);
        if (!allowed) {
            log.warn(">>>>>> [RATE-LIMIT-BLOCKED] 接口触发分布式限流拦截: key=[{}], 限制=[{}/{}s], msg=[{}]",
                    limitKey, count, time, rateLimiter.message());
            throw new RateLimitException(rateLimiter.message(), time);
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest resolveRequest() {
        HttpServletRequest request = RequestHolder.getRequest();
        if (request != null) {
            return request;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
