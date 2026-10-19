package com.alex.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 路由限流配置
 * 提供 IP（首个代理穿透）、用户凭据与 API 路径三维度 KeyResolver
 *
 * @author scott / alex
 * @date 2020/5/26
 */
@Configuration
@Slf4j
public class RateLimiterConfiguration {

    public static final String UNKNOWN_IP = "unknown";
    public static final String ANONYMOUS_USER = "anonymous";

    /**
     * IP 限流（默认 Primary KeyResolver）
     * 支持 X-Forwarded-For 多级反向代理首个真实 IP 穿透，并优雅兜底
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = resolveClientIp(exchange.getRequest());
            log.debug("RateLimiter IP Key: {}", clientIp);
            return Mono.just(clientIp);
        };
    }

    /**
     * 用户维度限流 (从配置的 tokenHeader 或默认 Authorization 提取用户凭证)
     */
    @Bean
    public KeyResolver userKeyResolver(GatewayAudience audience) {
        String tokenHeader = (audience != null && StringUtils.hasText(audience.getTokenHeader()))
                ? audience.getTokenHeader()
                : HttpHeaders.AUTHORIZATION;
        return exchange -> {
            String token = exchange.getRequest().getHeaders().getFirst(tokenHeader);
            if (!StringUtils.hasText(token)) {
                token = ANONYMOUS_USER;
            }
            log.debug("RateLimiter User Key: {}", token);
            return Mono.just(token);
        };
    }

    /**
     * 接口路径限流 (获取请求地址的规范 uri 作为限流 key)
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            log.debug("RateLimiter API Key: {}", path);
            return Mono.just(path);
        };
    }

    /**
     * 解析真实客户端 IP
     */
    public static String resolveClientIp(ServerHttpRequest request) {
        if (request == null) {
            return UNKNOWN_IP;
        }
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).trim();
            } else {
                return ip.trim();
            }
        }
        ip = headers.getFirst("X-Real-IP");
        if (StringUtils.hasText(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        ip = headers.getFirst("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        ip = headers.getFirst("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return UNKNOWN_IP;
    }
}
