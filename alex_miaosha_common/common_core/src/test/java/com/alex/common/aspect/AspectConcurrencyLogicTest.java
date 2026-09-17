package com.alex.common.aspect;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.SeckillLimit;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.common.redis.key.AccessKey;
import com.alex.common.redis.key.CommonKey;
import com.alex.common.utils.redis.RedisUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AspectConcurrencyLogicTest {

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    public static class SampleController {
        @AvoidRepeatableCommit(timeout = 5000)
        public String submitForm(String data) {
            return "ok";
        }

        @AccessLimit(limit = 3, timeout = 60)
        public String accessResource() {
            return "ok";
        }

        @SeckillLimit(seconds = 10, maxCount = 5)
        public String seckillOrder() {
            return "ok";
        }
    }

    @Test
    @DisplayName("AvoidRepeatableCommitAspect: 当首次提交抢占锁成功时正常放行")
    void testAvoidRepeatableCommit_Success() throws Throwable {
        AvoidRepeatableCommitAspect aspect = new AvoidRepeatableCommitAspect(redisUtils);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Method method = SampleController.class.getMethod("submitForm", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"formData"});
        when(redisUtils.setIfAbsent(any(), anyString(), eq("1"), eq(5000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true);
        when(joinPoint.proceed()).thenReturn(Result.success("ok"));

        Object result = aspect.around(joinPoint);

        verify(joinPoint, times(1)).proceed();
        assertEquals(Result.success("ok"), result);
    }

    @Test
    @DisplayName("AvoidRepeatableCommitAspect: 并发或快速重复提交时原子拦截返回 REPEAT_COMMIT")
    void testAvoidRepeatableCommit_Blocked() throws Throwable {
        AvoidRepeatableCommitAspect aspect = new AvoidRepeatableCommitAspect(redisUtils);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Method method = SampleController.class.getMethod("submitForm", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"formData"});
        // 模拟 Redis setIfAbsent 抢占失败（已被前一个并发请求占据）
        when(redisUtils.setIfAbsent(any(), anyString(), eq("1"), eq(5000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(false);

        Object result = aspect.around(joinPoint);

        verify(joinPoint, never()).proceed();
        assertEquals(Result.error(ResultEnum.REPEAT_COMMIT).getCode(), ((Result<?>) result).getCode());
    }

    @Test
    @DisplayName("AccessLimitAspect: 计数超限时精准拦截并隔离 URI")
    void testAccessLimit_BlockedWhenExceeded() throws Throwable {
        AccessLimitAspect aspect = new AccessLimitAspect(redisUtils);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/order/create");
        request.setRemoteAddr("192.168.1.50");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Method method = SampleController.class.getMethod("accessResource");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        // 当前计数 4，上限 3
        when(redisUtils.incrementWithExpire(any(), eq("/api/order/create:192.168.1.50"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(4L);

        Object result = aspect.around(joinPoint);

        verify(joinPoint, never()).proceed();
        assertEquals(ResultEnum.ACCESS_LIMIT_REACHED.getCode(), ((Result<?>) result).getCode());
    }

    @Test
    @DisplayName("SeckillLimitAspect: 根除首击 null 导致的 500，初次访问返回正常计数值并放行")
    void testSeckillLimit_FirstAccessSuccess() throws Throwable {
        SeckillLimitAspect aspect = new SeckillLimitAspect(redisUtils);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/seckill/do");
        request.setRemoteAddr("192.168.1.20");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Method method = SampleController.class.getMethod("seckillOrder");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        // 首次访问返回 1，上限 5，有效期 10s
        when(redisUtils.incrementWithExpire(any(), eq("/api/seckill/do:192.168.1.20"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(1L);
        when(joinPoint.proceed()).thenReturn("seckill_success");

        Object result = aspect.around(joinPoint);

        verify(joinPoint, times(1)).proceed();
        assertEquals("seckill_success", result);
    }
}
