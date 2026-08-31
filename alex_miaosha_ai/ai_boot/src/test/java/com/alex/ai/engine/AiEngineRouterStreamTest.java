package com.alex.ai.engine;

import com.alex.ai.config.AiProperties;
import com.alex.ai.engine.impl.RuleBasedAiEngine;
import com.alex.ai.stream.AiStreamSink;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AiEngineRouterStreamTest {

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
    void unknownEngine_fallbackEnabled_emitsRuleFallback() {
        aiProperties.getFallback().setEnabled(true);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("unknown-engine");
        req.setContent("hello");

        RecordingSink sink = new RecordingSink();
        router.analyzeStream(req, "req-s1", System.currentTimeMillis(), sink);

        assertEquals(List.of("meta", "delta", "done"), sink.events);
        assertEquals("rule-based(fallback)", sink.metaEngine);
        assertNotNull(sink.doneResp);
        assertEquals("rule-based(fallback)", sink.doneResp.getEngine());
        assertNull(sink.errorCode);
    }

    @Test
    void unknownEngine_fallbackDisabled_emitsUnavailableError() {
        aiProperties.getFallback().setEnabled(false);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("unknown-engine");
        req.setContent("hello");

        RecordingSink sink = new RecordingSink();
        router.analyzeStream(req, "req-s2", System.currentTimeMillis(), sink);

        assertEquals(List.of("error"), sink.events);
        assertEquals("500701", sink.errorCode);
        assertTrue(sink.errorMessage.contains("unknown-engine"));
        assertNull(sink.doneResp);
    }

    @Test
    void enabledEngineThrows_fallbackEnabled_emitsRuleFallback() {
        aiProperties.getFallback().setEnabled(true);
        doThrow(new RuntimeException("boom"))
                .when(deepSeekEngine)
                .analyzeStream(any(), anyString(), anyLong(), any());

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        RecordingSink sink = new RecordingSink();
        router.analyzeStream(req, "req-s3", System.currentTimeMillis(), sink);

        assertEquals(List.of("meta", "delta", "done"), sink.events);
        assertEquals("rule-based(fallback)", sink.metaEngine);
        assertEquals("rule-based(fallback)", sink.doneResp.getEngine());
        assertTrue(sink.doneResp.getSummary().contains("DeepSeek"));
        assertTrue(sink.doneResp.getSummary().contains("回退规则引擎"));
        assertNull(sink.errorCode);
    }

    @Test
    void enabledEngineThrows_fallbackDisabled_emitsCallFailedError() {
        aiProperties.getFallback().setEnabled(false);
        doThrow(new RuntimeException("boom"))
                .when(deepSeekEngine)
                .analyzeStream(any(), anyString(), anyLong(), any());

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        RecordingSink sink = new RecordingSink();
        router.analyzeStream(req, "req-s4", System.currentTimeMillis(), sink);

        assertEquals(List.of("error"), sink.events);
        assertEquals("500702", sink.errorCode);
        assertEquals("boom", sink.errorMessage);
        assertNull(sink.doneResp);
    }

    @Test
    void enabledEngineSinkError_fallbackEnabled_emitsRuleFallback() {
        aiProperties.getFallback().setEnabled(true);
        doAnswer(invocation -> {
            AiStreamSink sink = invocation.getArgument(3);
            sink.meta(invocation.getArgument(1), "deepseek:default");
            sink.error("500702", "upstream failed");
            return null;
        }).when(deepSeekEngine).analyzeStream(any(), anyString(), anyLong(), any());

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        RecordingSink sink = new RecordingSink();
        router.analyzeStream(req, "req-s5", System.currentTimeMillis(), sink);

        assertEquals(List.of("meta", "meta", "delta", "done"), sink.events);
        assertEquals("rule-based(fallback)", sink.metaEngine);
        assertEquals("rule-based(fallback)", sink.doneResp.getEngine());
        assertTrue(sink.doneResp.getSummary().contains("DeepSeek"));
        assertNull(sink.errorCode);
    }

    @Test
    void midStreamDeltaThenSinkError_fallback_doesNotForwardError_replacesSummary() {
        aiProperties.getFallback().setEnabled(true);
        doAnswer(invocation -> {
            AiStreamSink sink = invocation.getArgument(3);
            sink.meta(invocation.getArgument(1), "deepseek:default");
            sink.delta("partial-llm-");
            sink.delta("token");
            sink.error("500702", "upstream failed mid-stream");
            return null;
        }).when(deepSeekEngine).analyzeStream(any(), anyString(), anyLong(), any());

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setEngine("deepseek");
        req.setContent("hello");

        RecordingSink sink = new RecordingSink();
        router.analyzeStream(req, "req-s6", System.currentTimeMillis(), sink);

        assertFalse(sink.events.contains("error"));
        assertNull(sink.errorCode);
        assertEquals("rule-based(fallback)", sink.doneResp.getEngine());
        assertEquals("DeepSeek 调用失败，已回退规则引擎。", sink.doneResp.getSummary());
        // LLM deltas already on wire + fallback summary delta
        assertTrue(sink.deltaTexts.contains("partial-llm-"));
        assertTrue(sink.deltaTexts.contains("token"));
        assertTrue(sink.deltaTexts.stream().anyMatch(t -> t.contains("回退规则引擎")));
    }

    private static final class RecordingSink implements AiStreamSink {
        private final List<String> events = new ArrayList<>();
        private final List<String> deltaTexts = new ArrayList<>();
        private String metaEngine;
        private AiAnalyzeResp doneResp;
        private String errorCode;
        private String errorMessage;

        @Override
        public void meta(String requestId, String engine) {
            events.add("meta");
            metaEngine = engine;
        }

        @Override
        public void delta(String text) {
            events.add("delta");
            deltaTexts.add(text);
        }

        @Override
        public void done(AiAnalyzeResp resp) {
            events.add("done");
            doneResp = resp;
        }

        @Override
        public void error(String code, String message) {
            events.add("error");
            errorCode = code;
            errorMessage = message;
        }
    }
}
