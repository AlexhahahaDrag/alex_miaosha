package com.alex.finance.gift;

import com.alex.base.common.Result;
import com.alex.common.annotations.rate.LimitType;
import com.alex.common.annotations.rate.RateLimiter;
import com.alex.common.aspect.RateLimiterAspect;
import com.alex.common.exception.RateLimitException;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import com.alex.common.ratelimit.RateLimitKeyResolver;
import com.alex.common.ratelimit.RateLimiterManager;
import com.alex.finance.cpnCouponInfo.controller.CpnCouponInfoController;
import com.alex.finance.finance.controller.FinanceBudgetInfoController;
import com.alex.finance.finance.controller.FinanceInfoController;
import com.alex.finance.gift.analysis.controller.GiftAnalysisController;
import com.alex.finance.gift.analysis.service.GiftAnalysisService;
import com.alex.finance.gift.record.controller.GiftRecordInfoController;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Controller 接口 @RateLimiter 分布式限流切面拦截与异常防护集成测试
 */
@ExtendWith(MockitoExtension.class)
class ControllerRateLimiterIT {

    @Mock
    private GiftAnalysisService giftAnalysisService;

    @Mock
    private GiftRecordInfoService giftRecordInfoService;

    @Mock
    private RateLimiterManager rateLimiterManager;

    @Mock
    private RateLimitKeyResolver keyResolver;

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("测试礼金大盘矩阵导出接口在超频时被切面拦截并抛出 RateLimitException (HTTP 429)")
    void testExportMatrixRateLimiterBlocked() {
        GiftAnalysisController target = new GiftAnalysisController(giftAnalysisService);
        RateLimiterAspect aspect = new RateLimiterAspect(rateLimiterManager, keyResolver);

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        GiftAnalysisController proxy = factory.getProxy();

        when(keyResolver.resolveKey(any(Method.class), any(), any(RateLimiter.class), any()))
                .thenReturn("rate_limit:user:1001:export-matrix");
        when(rateLimiterManager.tryAcquire(eq("rate_limit:user:1001:export-matrix"), eq(60), eq(3)))
                .thenReturn(true)
                .thenReturn(false);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // 第一次调用：放行
        assertDoesNotThrow(() -> proxy.exportMatrix("1", "张", response));
        verify(giftAnalysisService, times(1)).exportBalanceMatrix(eq("1"), eq("张"), any());

        // 第二次调用：超频拦截抛出异常
        RateLimitException ex = assertThrows(RateLimitException.class, () -> proxy.exportMatrix("1", "张", response));
        assertTrue(ex.getMessage().contains("礼金平衡矩阵导出过于频繁"));
        assertEquals(60, ex.getRetryAfterSeconds());

        // 验证全局异常拦截器处理为 429
        ResponseEntity<Result<String>> responseEntity = exceptionHandler.handle(ex);
        assertEquals(429, responseEntity.getStatusCodeValue());
        assertEquals("60", responseEntity.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
        assertNotNull(responseEntity.getBody());
        assertEquals(ex.getMessage(), responseEntity.getBody().getMessage());
    }

    @Test
    @DisplayName("测试礼金流水导出接口在未放行时被切面精准拦截")
    void testExportRecordRateLimiterBlocked() {
        GiftRecordInfoController target = new GiftRecordInfoController(giftRecordInfoService);
        RateLimiterAspect aspect = new RateLimiterAspect(rateLimiterManager, keyResolver);

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        GiftRecordInfoController proxy = factory.getProxy();

        when(keyResolver.resolveKey(any(Method.class), any(), any(RateLimiter.class), any()))
                .thenReturn("rate_limit:user:1001:export-record");
        when(rateLimiterManager.tryAcquire(eq("rate_limit:user:1001:export-record"), eq(60), eq(3)))
                .thenReturn(false);

        MockHttpServletResponse response = new MockHttpServletResponse();
        RateLimitException ex = assertThrows(RateLimitException.class, () -> proxy.export(null, response));
        assertTrue(ex.getMessage().contains("礼尚往来流水导出过于频繁"));
        assertEquals(60, ex.getRetryAfterSeconds());

        // 验证全局异常处理器
        ResponseEntity<Result<String>> responseEntity = exceptionHandler.handle(ex);
        assertEquals(429, responseEntity.getStatusCodeValue());
        verify(giftRecordInfoService, never()).exportGiftRecordInfo(any(), any());
    }

    @Test
    @DisplayName("断言核心 Controller 接口上 @RateLimiter 属性配置契约")
    void testRateLimiterAnnotationContracts() throws Exception {
        // 1. FinanceInfoController.aiParse
        Method aiParseMethod = FinanceInfoController.class.getMethod("aiParse", com.alex.api.finance.vo.finance.UniversalFinanceAiParseReq.class);
        RateLimiter aiParseRate = aiParseMethod.getAnnotation(RateLimiter.class);
        assertNotNull(aiParseRate, "aiParse 必须标注 @RateLimiter");
        assertEquals(LimitType.USER, aiParseRate.limitType());
        assertEquals(10, aiParseRate.count());
        assertEquals(60, aiParseRate.time());

        // 2. FinanceInfoController.aiParseStream
        Method aiStreamMethod = FinanceInfoController.class.getMethod("aiParseStream", com.alex.api.finance.vo.finance.UniversalFinanceAiParseReq.class);
        RateLimiter aiStreamRate = aiStreamMethod.getAnnotation(RateLimiter.class);
        assertNotNull(aiStreamRate, "aiParseStream 必须标注 @RateLimiter");
        assertEquals(LimitType.USER, aiStreamRate.limitType());
        assertEquals(6, aiStreamRate.count());
        assertEquals(60, aiStreamRate.time());

        // 3. FinanceBudgetInfoController.recommendBudget
        Method recommendMethod = FinanceBudgetInfoController.class.getMethod("recommendBudget", com.alex.api.finance.vo.finance.FinanceBudgetAiRecommendReq.class);
        RateLimiter recommendRate = recommendMethod.getAnnotation(RateLimiter.class);
        assertNotNull(recommendRate, "recommendBudget 必须标注 @RateLimiter");
        assertEquals(LimitType.USER, recommendRate.limitType());
        assertEquals(5, recommendRate.count());
        assertEquals(60, recommendRate.time());

        // 4. CpnCouponInfoController.aiPlan
        Method aiPlanMethod = CpnCouponInfoController.class.getMethod("aiPlan", com.alex.api.finance.cpnCouponInfo.vo.CpnCouponAiPlanReq.class);
        RateLimiter aiPlanRate = aiPlanMethod.getAnnotation(RateLimiter.class);
        assertNotNull(aiPlanRate, "aiPlan 必须标注 @RateLimiter");
        assertEquals(LimitType.USER, aiPlanRate.limitType());
        assertEquals(5, aiPlanRate.count());
        assertEquals(60, aiPlanRate.time());
    }
}