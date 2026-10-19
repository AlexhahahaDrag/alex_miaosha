package com.alex.finance.cpnCouponInfo;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.api.fallback.AiAnalyzeFallbackFactory;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenFeign 降级工厂 AiAnalyzeFallbackFactory 标准化与契约测试
 */
class AiAnalyzeFallbackFactoryIT {

    private final AiAnalyzeFallbackFactory fallbackFactory = new AiAnalyzeFallbackFactory();

    @Test
    @DisplayName("降级工厂成功为网络异常创建非空 AiAnalyzeApi 降级客户端实例")
    void fallback_factory_should_create_safe_client_instance() {
        Throwable networkError = new RuntimeException("Connect to alex-ai-dev refused: timeout");

        AiAnalyzeApi fallbackClient = fallbackFactory.create(networkError);

        assertNotNull(fallbackClient, "降级客户端实例必须非空！");
    }

    @Test
    @DisplayName("调用降级客户端的 chat 方法时必须抛出规范的 SystemException(SYSTEM_NO_AVAILABLE, 'ai')")
    void fallback_client_should_throw_standard_system_exception() {
        Throwable cause = new IllegalStateException("Service unavailable");
        AiAnalyzeApi fallbackClient = fallbackFactory.create(cause);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("coupon-ai-plan");
        req.setContent("帮我策划一个促销券方案");

        SystemException ex = assertThrows(SystemException.class, () -> fallbackClient.chat(req));

        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), ex.getCode());
        assertNotNull(ex.getMsg());
        assertTrue(ex.getMsg().contains("ai"));
    }

    @Test
    @DisplayName("AiAnalyzeReq 与 AiAnalyzeResp 契约实体属性读写与上下文传递校验")
    void feign_request_response_bean_contract() {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("test-biz");
        req.setContent("test content");
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("budget", 1000);
        req.setContext(ctx);

        assertEquals("test-biz", req.getBizType());
        assertEquals("test content", req.getContent());
        assertEquals(1000, req.getContext().get("budget"));

        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary("{\"status\":\"ok\"}");
        resp.setEngine("deepseek");
        resp.setRequestId("req-123456");

        assertEquals("{\"status\":\"ok\"}", resp.getSummary());
        assertEquals("deepseek", resp.getEngine());
        assertEquals("req-123456", resp.getRequestId());
    }
}
