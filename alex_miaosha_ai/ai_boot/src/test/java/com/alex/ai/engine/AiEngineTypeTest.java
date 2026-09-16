package com.alex.ai.engine;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class AiEngineTypeTest {
    @Test
    void fromKey_ignoreCase() {
        assertEquals(AiEngineType.DEEPSEEK, AiEngineType.fromKey("DeepSeek").orElseThrow());
        assertEquals(AiEngineType.SENSENOVA, AiEngineType.fromKey("sensenova").orElseThrow());
        assertEquals(AiEngineType.RULE_BASED, AiEngineType.fromKey("rule-based").orElseThrow());
    }

    @Test
    void fromKey_unknownOrBlank_empty() {
        assertTrue(AiEngineType.fromKey(null).isEmpty());
        assertTrue(AiEngineType.fromKey("  ").isEmpty());
        assertTrue(AiEngineType.fromKey("openai").isEmpty());
    }
}
