package com.alex.common.annotations.rate;

import java.lang.annotation.*;

/**
 * 分布式限流与防刷注解
 * <p>基于滑动时间窗口（Sliding Window）实现，支持集群多实例全局限流与本地高可用优雅降级。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流时间窗口周期（秒），默认为 60 秒
     */
    int time() default 60;

    /**
     * 时间窗口内允许的最大访问次数，默认为 100 次
     */
    int count() default 100;

    /**
     * 限流维度，默认为按客户端真实 IP
     */
    LimitType limitType() default LimitType.IP;

    /**
     * 自定义 Key 前缀或 SpEL 表达式（当 limitType 为 PARAM 时作为入参表达式提取器，如 "#phone"）
     */
    String key() default "";

    /**
     * 触发限流时的提示文案
     */
    String message() default "访问过于频繁，请稍后再试！";
}
