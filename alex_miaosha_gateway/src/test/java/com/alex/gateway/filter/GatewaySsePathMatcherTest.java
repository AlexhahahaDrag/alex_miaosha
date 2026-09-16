package com.alex.gateway.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GatewaySsePathMatcherTest {

    private final GatewaySsePathMatcher matcher = new GatewaySsePathMatcher();

    @Test
    void matches_apiV1AiChatStream() {
        assertTrue(matcher.matches("/api/v1/ai/chat/stream"));
    }

    @Test
    void matches_gatewayPrefixedPath() {
        assertTrue(matcher.matches("/am-ai/api/v1/ai/chat/stream"));
    }

    @Test
    void matches_nestedServicePath() {
        assertTrue(matcher.matches("/ai/chat/stream"));
    }

    @Test
    void rejects_batchChat() {
        assertFalse(matcher.matches("/api/v1/ai/chat"));
        assertFalse(matcher.matches("/api/v1/ai/chat/"));
    }

    @Test
    void rejects_legacyAnalyzeStream() {
        assertFalse(matcher.matches("/api/v1/ai/analyze/stream"));
    }

    @Test
    void rejects_nullOrBlank() {
        assertFalse(matcher.matches(null));
        assertFalse(matcher.matches(""));
    }
}
