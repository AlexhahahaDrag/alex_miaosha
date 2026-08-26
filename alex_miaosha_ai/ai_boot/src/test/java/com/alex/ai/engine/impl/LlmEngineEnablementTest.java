package com.alex.ai.engine.impl;

import com.alex.ai.client.openai.OpenAiCompatibleClient;
import com.alex.ai.config.AiProperties;
import com.alex.ai.engine.AiEngineType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LlmEngineEnablementTest {

    @Mock
    private OpenAiCompatibleClient openAiCompatibleClient;

    private ObjectMapper objectMapper;
    private AiProperties aiProperties;
    private DeepSeekAiEngine deepSeekAiEngine;
    private SenseNovaAiEngine senseNovaAiEngine;
    private RuleBasedAiEngine ruleBasedAiEngine;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aiProperties = new AiProperties();
        deepSeekAiEngine = new DeepSeekAiEngine(openAiCompatibleClient, objectMapper, aiProperties);
        senseNovaAiEngine = new SenseNovaAiEngine(openAiCompatibleClient, objectMapper, aiProperties);
        ruleBasedAiEngine = new RuleBasedAiEngine();
    }

    @Test
    void deepSeek_key_returnsEnumKey() {
        assertEquals(AiEngineType.DEEPSEEK.getKey(), deepSeekAiEngine.key());
    }

    @Test
    void senseNova_key_returnsEnumKey() {
        assertEquals(AiEngineType.SENSENOVA.getKey(), senseNovaAiEngine.key());
    }

    @Test
    void ruleBased_key_returnsEnumKey() {
        assertEquals(AiEngineType.RULE_BASED.getKey(), ruleBasedAiEngine.key());
    }

    @Test
    void deepSeek_isEnabled_placeholderApiKey_false() {
        aiProperties.getDeepseek().setApiKey("sk-xxx");
        assertFalse(deepSeekAiEngine.isEnabled(aiProperties));
    }

    @Test
    void deepSeek_isEnabled_realApiKey_true() {
        aiProperties.getDeepseek().setApiKey("sk-real-key-value");
        assertTrue(deepSeekAiEngine.isEnabled(aiProperties));
    }

    @Test
    void deepSeek_isEnabled_nullProps_false() {
        assertFalse(deepSeekAiEngine.isEnabled(null));
    }

    @Test
    void senseNova_isEnabled_placeholderApiKey_false() {
        aiProperties.getSensenova().setApiKey("sk-xxx");
        assertFalse(senseNovaAiEngine.isEnabled(aiProperties));
    }

    @Test
    void senseNova_isEnabled_realApiKey_true() {
        aiProperties.getSensenova().setApiKey("sk-real-key-value");
        assertTrue(senseNovaAiEngine.isEnabled(aiProperties));
    }

    @Test
    void senseNova_isEnabled_nullProps_false() {
        assertFalse(senseNovaAiEngine.isEnabled(null));
    }

    @Test
    void ruleBased_isEnabled_alwaysTrue() {
        assertTrue(ruleBasedAiEngine.isEnabled(null));
        assertTrue(ruleBasedAiEngine.isEnabled(aiProperties));
    }
}
