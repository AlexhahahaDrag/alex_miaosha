package com.alex.ai.engine.impl;

import com.alex.ai.client.openai.OpenAiCompatibleClient;
import com.alex.ai.config.AiProperties;
import com.alex.ai.config.OpenAiCompatibleProperties;
import com.alex.ai.engine.AiEngineType;
import com.alex.ai.util.AiApiKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 引擎（Strategy key = deepseek）
 */
@Component
public class DeepSeekAiEngine extends AbstractOpenAiCompatibleAiEngine {

    public DeepSeekAiEngine(OpenAiCompatibleClient openAiCompatibleClient,
                            ObjectMapper objectMapper,
                            AiProperties aiProperties) {
        super(openAiCompatibleClient, objectMapper, aiProperties);
    }

    @Override
    protected AiEngineType engineType() {
        return AiEngineType.DEEPSEEK;
    }

    @Override
    protected OpenAiCompatibleProperties resolveProps(AiProperties aiProperties) {
        return aiProperties == null ? null : aiProperties.getDeepseek();
    }

    @Override
    public boolean isEnabled(AiProperties aiProperties) {
        if (aiProperties == null || aiProperties.getDeepseek() == null) {
            return false;
        }
        return AiApiKeys.isConfigured(aiProperties.getDeepseek().getApiKey());
    }
}
