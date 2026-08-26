package com.alex.ai.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AiApiKeysTest {
    @Test
    void isConfigured_rejectsNullBlankAndPlaceholders() {
        assertFalse(AiApiKeys.isConfigured(null));
        assertFalse(AiApiKeys.isConfigured(""));
        assertFalse(AiApiKeys.isConfigured("  "));
        assertFalse(AiApiKeys.isConfigured("sk-xxx"));
        assertFalse(AiApiKeys.isConfigured("SK-XXX"));
        assertFalse(AiApiKeys.isConfigured("changeme"));
        assertFalse(AiApiKeys.isConfigured("your-api-key"));
        assertFalse(AiApiKeys.isConfigured("todo"));
        assertTrue(AiApiKeys.isConfigured("sk-real-key-value"));
    }
}
