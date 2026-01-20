package com.alex.ai.config;

import lombok.Data;

/**
 * AI Agent：
 * DeepSeek 调用配置（按 OpenAI Chat Completions 兼容方式实现）
 */
@Data
public class DeepSeekProperties {

    /**
     * AI Agent：DeepSeek API BaseUrl
     * - 默认：<a href="https://api.deepseek.com">...</a>
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * AI Agent：Chat Completions Path
     * - 默认：/v1/chat/completions
     * - 若 DeepSeek 后续调整或你使用代理网关，可在配置中覆盖
     */
    private String chatCompletionsPath = "/v1/chat/completions";

    /**
     * AI Agent：API Key（建议通过环境变量注入，如：AI_DEEPSEEK_API_KEY）
     */
    private String apiKey;

    /**
     * AI Agent：默认模型
     * - 常见：deepseek-chat / deepseek-reasoner
     */
    private String model = "deepseek-chat";

    /**
     * AI Agent：采样温度
     */
    private Double temperature = 0.2;

    /**
     * AI Agent：最大 tokens
     */
    private Integer maxTokens = 1024;

    /**
     * AI Agent：超时时间（毫秒）
     */
    private Integer timeoutMs = 15000;
}


