package com.alex.ai.engine.impl;

import com.alex.ai.client.openai.OpenAiCompatibleClient;
import com.alex.ai.config.AiProperties;
import com.alex.ai.config.DeepSeekProperties;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Covers AbstractOpenAiCompatibleAiEngine JSON parse / degrade path.
 */
@ExtendWith(MockitoExtension.class)
class AbstractOpenAiCompatibleAiEngineJsonTest {

    @Mock
    private OpenAiCompatibleClient openAiCompatibleClient;

    private AiProperties aiProperties;
    private DeepSeekAiEngine engine;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        DeepSeekProperties deepseek = new DeepSeekProperties();
        deepseek.setApiKey("sk-test-key");
        deepseek.setModel("deepseek-chat");
        aiProperties.setDeepseek(deepseek);

        engine = new DeepSeekAiEngine(openAiCompatibleClient, new ObjectMapper(), aiProperties);
    }

    @Test
    void analyze_validJson_setsSummaryAndKeyPoints() {
        when(openAiCompatibleClient.chat(any(), any(), anyString()))
                .thenReturn("{\"summary\":\"ok summary\",\"keyPoints\":[\"a\",\"b\"]}");

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("hello");

        AiAnalyzeResp resp = engine.analyze(req, "req-json", System.currentTimeMillis());

        assertEquals("ok summary", resp.getSummary());
        assertEquals(List.of("a", "b"), resp.getKeyPoints());
        assertTrue(resp.getEngine().startsWith("deepseek:"));
    }

    @Test
    void analyze_nonJson_degradesToRawSummary() {
        when(openAiCompatibleClient.chat(any(), any(), anyString()))
                .thenReturn("plain text reply, not json");

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("hello");

        AiAnalyzeResp resp = engine.analyze(req, "req-plain", System.currentTimeMillis());

        assertEquals("plain text reply, not json", resp.getSummary());
        assertNotNull(resp.getKeyPoints());
        assertTrue(resp.getKeyPoints().isEmpty());
    }

    @Test
    void analyze_blankContent_setsEmptyDisplayMessage() {
        when(openAiCompatibleClient.chat(any(), any(), anyString())).thenReturn("");

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("hello");

        AiAnalyzeResp resp = engine.analyze(req, "req-blank", System.currentTimeMillis());

        assertEquals("DeepSeek 返回为空。", resp.getSummary());
        assertNotNull(resp.getKeyPoints());
        assertTrue(resp.getKeyPoints().isEmpty());
    }
}
