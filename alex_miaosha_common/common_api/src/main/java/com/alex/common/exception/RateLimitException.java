package com.alex.common.exception;

import lombok.Getter;

/**
 * 分布式限流与接口防刷拦截异常
 */
@Getter
public class RateLimitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP 业务状态码，默认 429
     */
    private final String code;

    /**
     * 建议客户端重试的等待时间（秒）
     */
    private final int retryAfterSeconds;

    public RateLimitException(String message) {
        this(message, 60);
    }

    public RateLimitException(String message, int retryAfterSeconds) {
        super(message);
        this.code = "429";
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public RateLimitException(String code, String message, int retryAfterSeconds) {
        super(message);
        this.code = code != null ? code : "429";
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }
}
