package com.alex.ai.engine;

import com.alex.ai.config.AiProperties;
import com.alex.ai.engine.impl.RuleBasedAiEngine;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.common.exception.AiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiEngineRouterTest {

    private AiProperties aiProperties;
    private RuleBasedAiEngine ruleBasedAiEngine;

    @Mock
    private AiEngine deepSeekEngine;

    private AiEngineRouter router;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.setEngine("rule-based");
        ruleBasedAiEngine = new RuleBasedAiEngine();

        lenient().when(deepSeekEngine.key()).thenReturn("deepseek");
        lenient().when(deepSeekEngine.isEnabled(any())).thenReturn(true);

        router = new AiEngineRouter(aiProperties, ruleBasedAiEngine, List.of(deepSeekEngine, ruleBasedAiEngine));
    }

    @Test
    void unknownEngine_fallbackEnabled_usesRuleBased() {
        aiProperties.getFallback().setEnabled(true);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("unknown-engine");
        req.setContent("hello");

        AiAnalyzeResp resp = router.analyze(req, "req-1", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("rule-based", resp.getEngine());
        assertNotNull(resp.getSummary());
    }

    @Test
    void unknownEngine_fallbackDisabled_throwsUnavailable() {
        aiProperties.getFallback().setEnabled(false);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("unknown-engine");
        req.setContent("hello");

        AiException ex = assertThrows(AiException.class,
                () -> router.analyze(req, "req-2", System.currentTimeMillis()));
        assertEquals("500701", ex.getCode());
    }

    @Test
    void enabledEngineThrows_fallbackEnabled_summaryWithDisplayName() {
        aiProperties.getFallback().setEnabled(true);
        when(deepSeekEngine.analyze(any(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("boom"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        AiAnalyzeResp resp = router.analyze(req, "req-3", System.currentTimeMillis());

        assertEquals("rule-based(fallback)", resp.getEngine());
        assertNotNull(resp.getSummary());
        assertTrue(resp.getSummary().contains("DeepSeek"));
        assertTrue(resp.getSummary().contains("回退规则引擎"));
    }

    @Test
    void enabledEngineThrows_fallbackDisabled_throwsCallFailed() {
        aiProperties.getFallback().setEnabled(false);
        when(deepSeekEngine.analyze(any(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("boom"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        AiException ex = assertThrows(AiException.class,
                () -> router.analyze(req, "req-4", System.currentTimeMillis()));
        assertEquals("500702", ex.getCode());
    }

    @Test
    void disabledEngine_fallbackEnabled_usesRuleBased() {
        aiProperties.getFallback().setEnabled(true);
        when(deepSeekEngine.isEnabled(any())).thenReturn(false);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        AiAnalyzeResp resp = router.analyze(req, "req-5", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("rule-based", resp.getEngine());
        assertNotNull(resp.getSummary());
    }

    @Test
    void disabledEngine_fallbackDisabled_throwsUnavailable() {
        aiProperties.getFallback().setEnabled(false);
        when(deepSeekEngine.isEnabled(any())).thenReturn(false);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        AiException ex = assertThrows(AiException.class,
                () -> router.analyze(req, "req-6", System.currentTimeMillis()));
        assertEquals("500701", ex.getCode());
    }

    @Test
    void fallbackNull_treatedAsEnabled_usesRuleBased() {
        aiProperties.setFallback(null);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("unknown-engine");
        req.setContent("hello");

        AiAnalyzeResp resp = router.analyze(req, "req-7", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("rule-based", resp.getEngine());
    }
}
