package com.alex.ai.service;

import com.alex.ai.engine.AiEngineRouter;
import com.alex.ai.service.impl.AiAnalyzeServiceImpl;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.common.utils.redis.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 阶段三十：AI Redis 语义指纹防穿透与多级限流配额降级集成测试
 */
@ExtendWith(MockitoExtension.class)
public class AiServiceFingerprintAndQuotaIT {

    @Mock
    private AiEngineRouter aiEngineRouter;

    @Mock
    private RedisUtils redisUtils;

    private ObjectMapper objectMapper;
    private AiAnalyzeServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AiAnalyzeServiceImpl(aiEngineRouter, redisUtils, objectMapper);
    }

    @Test
    @DisplayName("语义指纹确定性：无论 context Map 的插入顺序如何，生成的 SHA-256 缓存指纹 100% 一致")
    void testCacheFingerprintDeterministicRegardlessOfMapOrder() {
        Map<String, Object> ctx1 = new LinkedHashMap<>();
        ctx1.put("userId", "1001");
        ctx1.put("role", "finance_manager");

        Map<String, Object> ctx2 = new LinkedHashMap<>();
        ctx2.put("role", "finance_manager");
        ctx2.put("userId", "1001");

        AiAnalyzeReq req1 = new AiAnalyzeReq();
        req1.setBizType("finance");
        req1.setContent("分析本月支出");
        req1.setContext(ctx1);

        AiAnalyzeReq req2 = new AiAnalyzeReq();
        req2.setBizType("finance");
        req2.setContent("分析本月支出");
        req2.setContext(ctx2);

        String fp1 = service.buildCacheFingerprint(req1);
        String fp2 = service.buildCacheFingerprint(req2);

        assertNotNull(fp1);
        assertEquals(fp1, fp2, "不同 Map 插入顺序的上下文必须产出完全一致的语义指纹");
        assertTrue(fp1.startsWith("finance:"));
    }

    @Test
    @DisplayName("缓存命中免扣配额：Redis 存在命中结果时直接返回，engine 标注为 redis-cache，0 路由调用")
    void testCacheHitReturnsDirectlyWithoutRouting() throws Exception {
        AiAnalyzeResp cached = new AiAnalyzeResp();
        cached.setSummary("缓存命中的分析结果");
        cached.setKeyPoints(List.of("指标A", "指标B"));
        cached.setEngine("deepseek:deepseek-chat");

        when(redisUtils.get(startsWith(AiAnalyzeServiceImpl.CACHE_PREFIX)))
                .thenReturn(objectMapper.writeValueAsString(cached));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("coupon");
        req.setContent("满100减30营销分析");

        AiAnalyzeResp resp = service.analyze(req);

        assertNotNull(resp);
        assertEquals("缓存命中的分析结果", resp.getSummary());
        assertEquals("redis-cache", resp.getEngine(), "缓存命中时必须标注 engine 为 redis-cache");
        verify(aiEngineRouter, never()).analyze(any(), anyString(), anyLong());
    }

    @Test
    @DisplayName("配额超限平滑降级：分钟调用超过 15 次时，自动降级至轻量规则引擎并注入友情横幅提示")
    void testMinuteLimitExceededDegradesToRuleBasedWithBanner() {
        // 未命中缓存
        when(redisUtils.get(anyString())).thenReturn(null);
        // 模拟一分钟调用达 16 次
        when(redisUtils.incrementWithExpire(isNull(), contains("AiLimitKey:minute:"), eq(60L), any()))
                .thenReturn(16L);

        AiAnalyzeResp ruleResp = new AiAnalyzeResp();
        ruleResp.setSummary("AI Agent：已完成基础分析（规则引擎示例）。");
        ruleResp.setEngine("rule-based");
        when(aiEngineRouter.analyze(any(), anyString(), anyLong())).thenReturn(ruleResp);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("userId", "20088");
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("高频调用请求");
        req.setContext(ctx);

        AiAnalyzeResp resp = service.analyze(req);

        assertNotNull(resp);
        assertEquals("rule-based(quota-degraded)", resp.getEngine());
        assertTrue(resp.getSummary().contains("已完成基础分析"));
        assertTrue(resp.getSummary().contains("当前 AI 使用频率较高或今日额度已达上限，已自动切换为轻量规则引擎"));
    }

    @Test
    @DisplayName("单日配额超限平滑降级：单日调用超过 150 次时，同样触发轻量降级保护")
    void testDailyQuotaExceededDegradesToRuleBasedWithBanner() {
        when(redisUtils.get(anyString())).thenReturn(null);
        // 分钟未超（5次）
        when(redisUtils.incrementWithExpire(isNull(), contains("AiLimitKey:minute:"), eq(60L), any()))
                .thenReturn(5L);
        // 单日超限（151次）
        when(redisUtils.incrementWithExpire(isNull(), contains("AiLimitKey:daily:"), eq(86400L), any()))
                .thenReturn(151L);

        AiAnalyzeResp ruleResp = new AiAnalyzeResp();
        ruleResp.setSummary("基础规则分析结果");
        ruleResp.setEngine("rule-based");
        when(aiEngineRouter.analyze(any(), anyString(), anyLong())).thenReturn(ruleResp);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("userId", "30099");
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("日配额测试");
        req.setContext(ctx);

        AiAnalyzeResp resp = service.analyze(req);

        assertNotNull(resp);
        assertEquals("rule-based(quota-degraded)", resp.getEngine());
        assertTrue(resp.getSummary().contains("当前 AI 使用频率较高或今日额度已达上限"));
    }

    @Test
    @DisplayName("高可用容错：Redis 发生网络断连抛异常时，只记录告警日志，业务主链路平稳透传")
    void testRedisExceptionDoesNotBlockMainService() {
        doThrow(new RuntimeException("Redis connection refused"))
                .when(redisUtils).get(anyString());
        doThrow(new RuntimeException("Redis timeout"))
                .when(redisUtils).incrementWithExpire(any(), any(), anyLong(), any());

        AiAnalyzeResp routerResp = new AiAnalyzeResp();
        routerResp.setSummary("透传正常执行");
        routerResp.setEngine("deepseek:chat");
        when(aiEngineRouter.analyze(any(), anyString(), anyLong())).thenReturn(routerResp);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("Redis 故障容错");

        assertDoesNotThrow(() -> {
            AiAnalyzeResp resp = service.analyze(req);
            assertNotNull(resp);
            assertEquals("透传正常执行", resp.getSummary());
        });
    }
}
