package com.alex.ai.engine;

import com.alex.ai.config.AiProperties;
import com.alex.ai.config.DeepSeekProperties;
import com.alex.ai.config.SenseNovaProperties;
import com.alex.ai.engine.impl.DeepSeekAiEngine;
import com.alex.ai.engine.impl.RuleBasedAiEngine;
import com.alex.ai.engine.impl.SenseNovaAiEngine;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.AiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 阶段三十：AI 多引擎动态路由与参数覆盖集成测试
 */
@ExtendWith(MockitoExtension.class)
public class AiEngineDynamicRoutingIT {

    @Mock
    private DeepSeekAiEngine deepSeekAiEngine;

    @Mock
    private SenseNovaAiEngine senseNovaAiEngine;

    private RuleBasedAiEngine ruleBasedAiEngine;
    private AiProperties aiProperties;
    private AiEngineRouter router;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.setEngine("deepseek");
        AiProperties.Fallback fallback = new AiProperties.Fallback();
        fallback.setEnabled(true);
        aiProperties.setFallback(fallback);

        DeepSeekProperties deepseek = new DeepSeekProperties();
        deepseek.setApiKey("sk-mock-key");
        deepseek.setModel("deepseek-chat");
        aiProperties.setDeepseek(deepseek);

        SenseNovaProperties sensenova = new SenseNovaProperties();
        sensenova.setApiKey("mock-sense-key");
        sensenova.setModel("Nova-1.0");
        aiProperties.setSensenova(sensenova);

        ruleBasedAiEngine = new RuleBasedAiEngine();

        lenient().when(deepSeekAiEngine.key()).thenReturn("deepseek");
        lenient().when(deepSeekAiEngine.isEnabled(any())).thenReturn(true);

        lenient().when(senseNovaAiEngine.key()).thenReturn("sensenova");
        lenient().when(senseNovaAiEngine.isEnabled(any())).thenReturn(true);

        router = new AiEngineRouter(aiProperties, ruleBasedAiEngine, List.of(deepSeekAiEngine, senseNovaAiEngine, ruleBasedAiEngine));
    }

    @Test
    @DisplayName("默认路由：请求未显式指定 engine 时，默认路由至配置的 deepseek 引擎")
    void testDefaultRouteToDeepSeek() {
        AiAnalyzeResp mockResp = new AiAnalyzeResp();
        mockResp.setRequestId("rid-101");
        mockResp.setEngine("deepseek:deepseek-chat");
        mockResp.setSummary("DeepSeek 分析成功");
        when(deepSeekAiEngine.analyze(any(), anyString(), anyLong())).thenReturn(mockResp);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("请分析本季度支出");

        AiAnalyzeResp resp = router.analyze(req, "rid-101", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("deepseek:deepseek-chat", resp.getEngine());
        assertEquals("DeepSeek 分析成功", resp.getSummary());
        verify(deepSeekAiEngine, times(1)).analyze(eq(req), eq("rid-101"), anyLong());
        verify(senseNovaAiEngine, never()).analyze(any(), any(), anyLong());
    }

    @Test
    @DisplayName("动态覆盖：请求显式指定 engine=sensenova 时，优先路由至 sensenova 引擎")
    void testExplicitRouteOverrideToSenseNova() {
        AiAnalyzeResp mockResp = new AiAnalyzeResp();
        mockResp.setRequestId("rid-102");
        mockResp.setEngine("sensenova:Nova-1.0");
        mockResp.setSummary("商汤分析成功");
        when(senseNovaAiEngine.analyze(any(), anyString(), anyLong())).thenReturn(mockResp);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("请分析");
        req.setEngine("sensenova");

        AiAnalyzeResp resp = router.analyze(req, "rid-102", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("sensenova:Nova-1.0", resp.getEngine());
        verify(senseNovaAiEngine, times(1)).analyze(eq(req), eq("rid-102"), anyLong());
        verify(deepSeekAiEngine, never()).analyze(any(), any(), anyLong());
    }

    @Test
    @DisplayName("规则引擎直通：请求指定 engine=rule-based 时，直通本地轻量规则引擎")
    void testDirectRouteToRuleBased() {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("测试规则引擎");
        req.setEngine("rule-based");

        AiAnalyzeResp resp = router.analyze(req, "rid-103", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("rule-based", resp.getEngine());
        assertTrue(resp.getSummary().contains("已完成基础分析"));
        verify(deepSeekAiEngine, never()).analyze(any(), any(), anyLong());
        verify(senseNovaAiEngine, never()).analyze(any(), any(), anyLong());
    }

    @Test
    @DisplayName("异常自动降级：大模型调用抛出异常且 fallback=true 时，平滑降级至规则引擎并打上 fallback 标签")
    void testLlmFailureFallbacksToRuleBasedGracefully() {
        when(deepSeekAiEngine.analyze(any(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("DeepSeek upstream network timeout"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("网络超时降级测试");

        AiAnalyzeResp resp = router.analyze(req, "rid-104", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("rule-based(fallback)", resp.getEngine());
        assertTrue(resp.getSummary().contains("已回退规则引擎"));
    }

    @Test
    @DisplayName("熔断阻断验证：fallback=false 时，未知或不可用引擎直接抛出 AiException 业务异常")
    void testEngineUnavailableThrowsExceptionWhenFallbackDisabled() {
        aiProperties.getFallback().setEnabled(false);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("测试未知引擎");
        req.setEngine("non-existent-engine");

        AiException ex = assertThrows(AiException.class, () ->
                router.analyze(req, "rid-105", System.currentTimeMillis()));

        assertEquals(ResultEnum.AI_ENGINE_UNAVAILABLE.getCode(), ex.getCode());
        assertTrue(ex.getMsg().contains("non-existent-engine"));
    }
}
