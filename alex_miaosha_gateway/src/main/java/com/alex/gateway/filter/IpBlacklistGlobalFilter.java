package com.alex.gateway.filter;

import com.alex.gateway.config.GatewayAudience;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关全局 IP 黑名单快速短路拦截过滤器
 * 职责：
 * 1. 提取客户端真实外网 IP（兼容 X-Forwarded-For 多级代理与 X-Real-IP）；
 * 2. 匹配本地与配置黑名单；
 * 3. 命中黑名单直接返回 HTTP 403 阻断，不向下游微服务透传，杜绝资源浪费。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IpBlacklistGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayAudience audience;

    public static final String UNKNOWN = "unknown";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = resolveClientIp(exchange.getRequest());

        if (isBlacklisted(clientIp)) {
            log.warn("检测到黑名单 IP 请求被安全拦截: ip={}, path={}", clientIp, exchange.getRequest().getPath());
            return renderForbiddenResponse(exchange, clientIp);
        }

        return chain.filter(exchange);
    }

    /**
     * 判断是否在黑名单中
     */
    public boolean isBlacklisted(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return false;
        }
        List<String> blackList = audience.getIpBlackList();
        if (blackList == null || blackList.isEmpty()) {
            return false;
        }

        for (String pattern : blackList) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            String trimmed = pattern.trim();
            // 精确 IP 匹配
            if (trimmed.equalsIgnoreCase(clientIp)) {
                return true;
            }
            // 通配符前缀匹配（如 10.0.0.*）
            if (trimmed.endsWith("*") && clientIp.startsWith(trimmed.substring(0, trimmed.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析客户端真实 IP
     */
    public static String resolveClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个IP才是真实IP
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index).trim();
            } else {
                return ip.trim();
            }
        }

        ip = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        ip = request.getHeaders().getFirst("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "127.0.0.1";
    }

    /**
     * 写出 403 阻断响应
     */
    private Mono<Void> renderForbiddenResponse(ServerWebExchange exchange, String clientIp) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        JsonObject message = new JsonObject();
        message.addProperty("code", 403);
        message.addProperty("message", "IP已被安全封禁，禁止访问: " + clientIp);
        message.addProperty("success", false);

        byte[] bits = message.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 优先级仅次于 TraceIdGlobalFilter，先于鉴权与加解密
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
