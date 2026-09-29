package com.alex.ai.service;

import com.alex.ai.engine.AiEngineRouter;
import com.alex.ai.service.impl.AiAnalyzeServiceImpl;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.common.utils.redis.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiAnalyzeCacheTest {

    @Test
    @DisplayName("测试当 Redis 命中时直接返回缓存响应且引擎为 redis-cache")
    void testCacheHitReturnsCachedResp() throws Exception {
        AiEngineRouter router = mock(AiEngineRouter.class);
        RedisUtils redisUtils = mock(RedisUtils.class);
        ObjectMapper mapper = new ObjectMapper();

        AiAnalyzeResp cached = new AiAnalyzeResp();
        cached.setSummary("这是缓存中的 AI 营销文案");
        cached.setEngine("deepseek");

        when(redisUtils.get(anyString())).thenReturn(mapper.writeValueAsString(cached));

        AiAnalyzeServiceImpl service = new AiAnalyzeServiceImpl(router, redisUtils, mapper);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("product-ai-copy");
        req.setContent("爆款耳机文案");

        AiAnalyzeResp resp = service.analyze(req);

        Assertions.assertNotNull(resp);
        Assertions.assertEquals("这是缓存中的 AI 营销文案", resp.getSummary());
        Assertions.assertEquals("redis-cache", resp.getEngine());
        // router 不应被调用
        verify(router, never()).analyze(any(), any(), anyLong());
    }

    @Test
    @DisplayName("测试当缓存未命中时调用 Router 并将结果写入 Redis")
    void testCacheMissCallsRouterAndSavesToRedis() {
        AiEngineRouter router = mock(AiEngineRouter.class);
        RedisUtils redisUtils = mock(RedisUtils.class);
        ObjectMapper mapper = new ObjectMapper();

        when(redisUtils.get(anyString())).thenReturn(null);

        AiAnalyzeResp freshResp = new AiAnalyzeResp();
        freshResp.setSummary("实时生成的文案");
        freshResp.setEngine("deepseek-chat");

        when(router.analyze(any(), any(), anyLong())).thenReturn(freshResp);

        AiAnalyzeServiceImpl service = new AiAnalyzeServiceImpl(router, redisUtils, mapper);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("coupon-plan");
        req.setContent("新客立减券方案");

        AiAnalyzeResp resp = service.analyze(req);

        Assertions.assertNotNull(resp);
        Assertions.assertEquals("实时生成的文案", resp.getSummary());
        Assertions.assertEquals("deepseek-chat", resp.getEngine());
        // verify setEx was called on redisUtils
        verify(redisUtils, times(1)).setEx(anyString(), contains("实时生成的文案"), eq(AiAnalyzeServiceImpl.CACHE_EXPIRE_SECONDS), any());
    }

    @Test
    @DisplayName("测试 Context 属性排序后生成确定的语义指纹")
    void testFingerprintDeterministicWithDifferentKeyOrder() {
        AiEngineRouter router = mock(AiEngineRouter.class);
        AiAnalyzeServiceImpl service = new AiAnalyzeServiceImpl(router, null, null);

        Map<String, Object> ctx1 = new HashMap<>();
        ctx1.put("price", 99);
        ctx1.put("category", "数码");

        Map<String, Object> ctx2 = new HashMap<>();
        ctx2.put("category", "数码");
        ctx2.put("price", 99);

        AiAnalyzeReq req1 = new AiAnalyzeReq();
        req1.setBizType("test");
        req1.setContent("hello");
        req1.setContext(ctx1);

        AiAnalyzeReq req2 = new AiAnalyzeReq();
        req2.setBizType("test");
        req2.setContent("hello");
        req2.setContext(ctx2);

        String fp1 = service.buildCacheFingerprint(req1);
        String fp2 = service.buildCacheFingerprint(req2);

        Assertions.assertEquals(fp1, fp2);
    }

    @Test
    @DisplayName("测试当用户调用频次超出限流时优雅降级至本地规则引擎并附加友好提示")
    void testRateLimitDegradesToRuleBased() {
        AiEngineRouter router = mock(AiEngineRouter.class);
        RedisUtils redisUtils = mock(RedisUtils.class);
        ObjectMapper mapper = new ObjectMapper();

        // 缓存未命中
        when(redisUtils.get(anyString())).thenReturn(null);
        // 分钟调用次数超出阈值 (16 > 15)
        when(redisUtils.incrementWithExpire(isNull(), contains("AiLimitKey:minute:"), eq(60L), any()))
                .thenReturn(16L);

        AiAnalyzeResp ruleResp = new AiAnalyzeResp();
        ruleResp.setSummary("本地规则分析完成");
        ruleResp.setEngine("rule-based");

        when(router.analyze(any(), any(), anyLong())).thenReturn(ruleResp);

        AiAnalyzeServiceImpl service = new AiAnalyzeServiceImpl(router, redisUtils, mapper);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("coupon-plan");
        req.setContent("高频请求测试");
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("userId", "10086");
        req.setContext(ctx);

        AiAnalyzeResp resp = service.analyze(req);

        Assertions.assertNotNull(resp);
        Assertions.assertEquals("rule-based(quota-degraded)", resp.getEngine());
        Assertions.assertTrue(resp.getSummary().contains("当前 AI 使用频率较高或今日额度已达上限"));
    }
}
