package com.alex.gateway.filter;

import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Set;

/**
 * SSE 流式路径匹配：供网关跳过响应加密缓冲。
 */
public final class GatewaySsePathMatcher {

    public static final Set<String> DEFAULT_SSE_STREAM_PATHS = Set.of(
            "/**/ai/chat/stream"
    );

    private final AntPathMatcher antPathMatcher;
    private final Collection<String> patterns;

    public GatewaySsePathMatcher() {
        this(new AntPathMatcher(), DEFAULT_SSE_STREAM_PATHS);
    }

    public GatewaySsePathMatcher(AntPathMatcher antPathMatcher, Collection<String> patterns) {
        this.antPathMatcher = antPathMatcher == null ? new AntPathMatcher() : antPathMatcher;
        this.patterns = patterns == null || patterns.isEmpty()
                ? DEFAULT_SSE_STREAM_PATHS
                : patterns;
    }

    public boolean matches(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (pattern != null && antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
