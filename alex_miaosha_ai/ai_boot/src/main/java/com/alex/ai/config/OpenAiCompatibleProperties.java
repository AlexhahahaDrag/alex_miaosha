package com.alex.ai.config;

import lombok.Data;

/**
 * OpenAI Chat Completions 兼容 API 的公共配置字段。
 * <p>DeepSeek、SenseNova 等引擎子类继承并设置各自默认值。
 */
@Data
public class OpenAiCompatibleProperties {

    private String baseUrl;

    private String chatCompletionsPath;

    private String apiKey;

    private String model;

    private Double temperature;

    private Integer maxTokens;

    private Integer timeoutMs;
}
