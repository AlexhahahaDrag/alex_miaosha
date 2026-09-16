package com.alex.ai.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI Agent：
 * DeepSeek 调用配置（按 OpenAI Chat Completions 兼容方式实现）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeepSeekProperties extends OpenAiCompatibleProperties {

    public DeepSeekProperties() {
        setBaseUrl("https://api.deepseek.com");
        setChatCompletionsPath("/v1/chat/completions");
        setModel("deepseek-chat");
        setTemperature(0.2);
        setMaxTokens(1024);
        setTimeoutMs(15000);
    }
}
