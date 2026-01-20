package com.alex.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI Agent：
 * AI 服务 HTTP 客户端配置
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiHttpConfig {

    /**
     * AI Agent：
     * 给 AI 外呼（DeepSeek 等）准备的 RestTemplate，避免影响全局 RestTemplate 配置。
     */
    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate(AiProperties aiProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        int timeoutMs = aiProperties != null && aiProperties.getDeepseek() != null
                ? aiProperties.getDeepseek().getTimeoutMs()
                : 15000;

        int safeTimeout = timeoutMs <= 0 ? 15000 : timeoutMs;
        factory.setConnectTimeout(safeTimeout);
        factory.setReadTimeout(safeTimeout);

        return new RestTemplate(factory);
    }
}


