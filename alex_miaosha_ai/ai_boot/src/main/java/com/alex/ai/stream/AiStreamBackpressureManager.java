package com.alex.ai.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 流式会话背压与并发管理器
 * 职责：
 * 1. 限制单个用户并发活跃流连接数（默认最多 2 个）；
 * 2. 避免高并发刷流导致的线程池耗尽与 OOM；
 * 3. 活跃流计数归零时自动释放 map 键，防止内存泄漏。
 */
@Component
@Slf4j
public class AiStreamBackpressureManager {

    public static final int DEFAULT_MAX_CONCURRENT_STREAMS = 2;

    private final int maxConcurrentStreams;
    private final ConcurrentHashMap<String, AtomicInteger> activeStreams = new ConcurrentHashMap<>();

    public AiStreamBackpressureManager() {
        this(DEFAULT_MAX_CONCURRENT_STREAMS);
    }

    public AiStreamBackpressureManager(int maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams > 0 ? maxConcurrentStreams : DEFAULT_MAX_CONCURRENT_STREAMS;
    }

    /**
     * 尝试获取流式会话许可
     *
     * @param userId 用户 ID
     * @return true 获取成功，false 已超出并发上限触发背压
     */
    public boolean tryAcquire(String userId) {
        String key = (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
        AtomicInteger counter = activeStreams.computeIfAbsent(key, k -> new AtomicInteger(0));

        while (true) {
            int current = counter.get();
            if (current >= maxConcurrentStreams) {
                log.warn("用户 {} 活跃流式会话已达上限 {}，触发背压拦截", key, maxConcurrentStreams);
                return false;
            }
            if (counter.compareAndSet(current, current + 1)) {
                log.debug("用户 {} 成功获取流会话许可，当前活跃数: {}", key, current + 1);
                return true;
            }
        }
    }

    /**
     * 释放流式会话许可
     *
     * @param userId 用户 ID
     */
    public void release(String userId) {
        String key = (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
        AtomicInteger counter = activeStreams.get(key);
        if (counter != null) {
            int remaining = counter.decrementAndGet();
            log.debug("用户 {} 释放流会话许可，剩余活跃数: {}", key, remaining);
            if (remaining <= 0) {
                activeStreams.remove(key, counter);
            }
        }
    }

    /**
     * 获取当前用户活跃会话数
     */
    public int getActiveCount(String userId) {
        String key = (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
        AtomicInteger counter = activeStreams.get(key);
        return counter == null ? 0 : Math.max(0, counter.get());
    }

    public int getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }
}
