package com.alex.common.cache.multilevel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description:  分布式多级缓存 (Caffeine L1 + Redis L2) 配置属性
 * author:       alex
 * createDate:   2026/10/21
 * version:      1.0.0
 */
@Data
@ConfigurationProperties(prefix = "spring.cache.multilevel")
public class MultiLevelCacheProperties {

    /**
     * 是否启用多级缓存
     */
    private boolean enabled = true;

    /**
     * L1 本地缓存初始容量
     */
    private int l1InitialCapacity = 256;

    /**
     * L1 本地缓存最大条数
     */
    private long l1MaximumSize = 10000L;

    /**
     * L1 本地缓存默认写入后过期时间（秒），默认 60 秒
     */
    private long l1DefaultTtlSeconds = 60L;

    /**
     * 防穿透空值缓存过期时间（秒），默认 30 秒
     */
    private long nullValueTtlSeconds = 30L;

    /**
     * 防雪崩 TTL 随机抖动最大秒数（0 ~ maxJitterSeconds 随机波动）
     */
    private int maxJitterSeconds = 15;

    /**
     * 热 Key 统计滑动窗口时间（秒），默认 5 秒
     */
    private int hotKeyWindowSeconds = 5;

    /**
     * 热 Key 触发阈值（窗口时间内访问超过此频次自动判定为热 Key 并延长本地驻留）
     */
    private int hotKeyThreshold = 20;

    /**
     * 热 Key 延长留存时间（秒），默认 300 秒（5 分钟）
     */
    private long hotKeyExtendedTtlSeconds = 300L;
}
