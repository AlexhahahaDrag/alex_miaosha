package com.alex.gateway.filter;

import com.alex.gateway.config.GatewayAudience;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局灰度发布与金丝雀（Canary）流量染色过滤器
 * 职责：
 * 1. 识别请求中的灰度标签（Header: X-Gray-Tag / X-Version 等）；
 * 2. 对命中灰度的流量注入下游元数据（X-Route-Tag: gray, X-Service-Version: {version}）；
 * 3. 未命中灰度时默认标记为 default，确保请求平滑兜底。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GrayReleaseGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayAudience audience;

    public static final String DEFAULT_GRAY_HEADER = "X-Gray-Tag";
    public static final String ROUTE_TAG_HEADER = "X-Route-Tag";
    public static final String SERVICE_VERSION_HEADER = "X-Service-Version";

    public static final String TAG_GRAY = "gray";
    public static final String TAG_DEFAULT = "default";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String headerName = StringUtils.hasText(audience.getGrayHeaderName())
                ? audience.getGrayHeaderName()
                : DEFAULT_GRAY_HEADER;

        String grayTag = request.getHeaders().getFirst(headerName);

        String effectiveRouteTag;
        String effectiveServiceVersion;

        if (StringUtils.hasText(grayTag) && !TAG_DEFAULT.equalsIgnoreCase(grayTag.trim())) {
            effectiveRouteTag = TAG_GRAY;
            effectiveServiceVersion = grayTag.trim();
            log.debug("流量命中灰度染色规则: path={}, header={}, value={}", request.getPath(), headerName, grayTag);
        } else {
            effectiveRouteTag = TAG_DEFAULT;
            effectiveServiceVersion = TAG_DEFAULT;
        }

        // 注入下游请求头
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(ROUTE_TAG_HEADER, effectiveRouteTag)
                .header(SERVICE_VERSION_HEADER, effectiveServiceVersion)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // 先于常规鉴权与解密执行（早于 GatewayFilter 的 -2）
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
