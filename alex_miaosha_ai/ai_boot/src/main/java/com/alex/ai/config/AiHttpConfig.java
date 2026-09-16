package com.alex.ai.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * AI 服务 HTTP 客户端配置（WebClient 供 OpenAI 兼容客户端）。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiHttpConfig {

    /**
     * 按超时毫秒构建 WebClient（仅作 HTTP 客户端，不切换为 WebFlux 服务端）。
     * timeoutMs 为 null 或 &lt;=0 时默认 15000。
     */
    public static WebClient buildAiWebClient(int timeoutMs) {
        int safeTimeout = timeoutMs <= 0 ? 15000 : timeoutMs;
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, safeTimeout)
                .responseTimeout(Duration.ofMillis(safeTimeout));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
