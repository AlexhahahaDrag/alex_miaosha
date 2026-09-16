package com.alex.ai.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SenseNova（商汤日日新）调用配置。
 * API 与 OpenAI Chat Completions 兼容：POST {baseUrl}/v1/chat/completions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SenseNovaProperties extends OpenAiCompatibleProperties {

    public SenseNovaProperties() {
        setBaseUrl("https://token.sensenova.cn");
        setChatCompletionsPath("/v1/chat/completions");
        setModel("sensenova-6.8-flash-lite");
        setTemperature(0.2);
        setMaxTokens(1024);
        setTimeoutMs(15000);
    }
}
