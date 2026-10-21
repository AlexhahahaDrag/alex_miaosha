package com.alex.common.cache.multilevel;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:  轻量级热 Key 滑动窗口自动探测器
 * author:       alex
 * createDate:   2026/10/21
 * version:      1.0.0
 */
@Slf4j
public class HotKeyDetector {

    private final MultiLevelCacheProperties properties;
    private final ConcurrentHashMap<String, CounterEntry> counterMap = new ConcurrentHashMap<>();

    public HotKeyDetector(MultiLevelCacheProperties properties) {
        this.properties = properties != null ? properties : new MultiLevelCacheProperties();
    }

    /**
     * 记录一次 key 访问并检测是否达到热 Key 阈值
     *
     * @param key 缓存键
     * @return true 表示当前已达到热 Key 判定标准
     */
    public boolean recordAndCheck(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        long now = System.currentTimeMillis();
        long windowMillis = properties.getHotKeyWindowSeconds() * 1000L;

        CounterEntry entry = counterMap.compute(key, (k, old) -> {
            if (old == null || (now - old.windowStart) > windowMillis) {
                return new CounterEntry(now, 1);
            }
            old.count.incrementAndGet();
            return old;
        });

        boolean isHot = entry.count.get() >= properties.getHotKeyThreshold();
        if (isHot && entry.count.get() == properties.getHotKeyThreshold()) {
            log.info("探测到热 Key: key={}, 窗口内频次={}", key, entry.count.get());
        }

        // 偶发触发清理过期 counter，防止 Map 无限膨胀
        if (counterMap.size() > 5000) {
            cleanExpired(now, windowMillis);
        }

        return isHot;
    }

    /**
     * 手动判断是否为热 Key（不增加计数值）
     */
    public boolean isHot(String key) {
        if (key == null) {
            return false;
        }
        CounterEntry entry = counterMap.get(key);
        if (entry == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long windowMillis = properties.getHotKeyWindowSeconds() * 1000L;
        if ((now - entry.windowStart) > windowMillis) {
            return false;
        }
        return entry.count.get() >= properties.getHotKeyThreshold();
    }

    public void reset() {
        counterMap.clear();
    }

    private void cleanExpired(long now, long windowMillis) {
        Iterator<Map.Entry<String, CounterEntry>> it = counterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CounterEntry> entry = it.next();
            if ((now - entry.getValue().windowStart) > windowMillis) {
                it.remove();
            }
        }
    }

    private static class CounterEntry {
        final long windowStart;
        final AtomicInteger count;

        CounterEntry(long windowStart, int initialCount) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(initialCount);
        }
    }
}
