package com.alex.ai.client.openai;

import com.alex.api.ai.vo.AiAnalyzeReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AiPromptBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void buildUserPrompt_includesBizTypeDepthContent() {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("finance");
        req.setDepth(2);
        req.setContent("hello-world");
        Map<String, Object> context = new HashMap<>();
        context.put("k", "v");
        req.setContext(context);

        String prompt = AiPromptBuilder.buildUserPrompt(req, objectMapper);

        assertTrue(prompt.contains("bizType=finance"));
        assertTrue(prompt.contains("depth=2"));
        assertTrue(prompt.contains("content=hello-world"));
        assertTrue(prompt.contains("context="));
        assertTrue(prompt.contains("\"k\""));
    }

    @Test
    void buildUserPrompt_defaultsWhenNullFields() {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("only-content");

        String prompt = AiPromptBuilder.buildUserPrompt(req, objectMapper);

        assertTrue(prompt.contains("bizType=default"));
        assertTrue(prompt.contains("depth=1"));
        assertTrue(prompt.contains("content=only-content"));
    }

    @Test
    void buildUserPrompt_badContextDoesNotThrow() {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("gift");
        req.setDepth(1);
        req.setContent("x");
        Map<String, Object> context = new HashMap<>();
        context.put("bad", new ThrowingBean());
        req.setContext(context);

        String prompt = assertDoesNotThrow(() -> AiPromptBuilder.buildUserPrompt(req, objectMapper));

        assertTrue(prompt.contains("bizType=gift"));
        assertTrue(prompt.contains("content=x"));
        assertFalse(prompt.contains("context="));
    }

    @Test
    void buildSystemPrompt_requiresJsonConstraint() {
        String system = AiPromptBuilder.buildSystemPrompt(new AiAnalyzeReq());
        assertTrue(system.contains("JSON"));
        assertTrue(system.contains("summary"));
        assertTrue(system.contains("keyPoints"));
    }

    /** Getter throws → Jackson fails serialization without StackOverflowError. */
    static class ThrowingBean {
        public String getValue() {
            throw new IllegalStateException("boom");
        }
    }
}
