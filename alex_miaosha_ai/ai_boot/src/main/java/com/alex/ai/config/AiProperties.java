package com.alex.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI Agent：
 * AI 服务配置
 *
 * 说明：
 * - engine 用于选择默认引擎（deepseek / rule-based）
 * - deepseek 用于配置 DeepSeek OpenAI 兼容接口
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * AI Agent：默认引擎
     * - deepseek：优先调用 DeepSeek（需要配置 apiKey）
     * - rule-based：仅使用本地规则引擎
     */
    private String engine = "rule-based";

    /**
     * AI Agent：DeepSeek 配置
     */
    private DeepSeekProperties deepseek = new DeepSeekProperties();
}


