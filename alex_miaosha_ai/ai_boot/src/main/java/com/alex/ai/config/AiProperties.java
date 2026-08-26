package com.alex.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 服务配置。
 * <p>engine 可选：sensenova / deepseek / rule-based
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 默认引擎：sensenova / deepseek / rule-based
     */
    private String engine = "rule-based";

    private Fallback fallback = new Fallback();

    private DeepSeekProperties deepseek = new DeepSeekProperties();

    private SenseNovaProperties sensenova = new SenseNovaProperties();

    @Data
    public static class Fallback {
        /** 对应 ai.fallback.enabled，默认 true */
        private boolean enabled = true;
    }
}
