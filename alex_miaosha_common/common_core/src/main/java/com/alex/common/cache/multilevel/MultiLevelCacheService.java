package com.alex.common.cache.multilevel;

import com.alex.common.utils.bean.BeanUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * description:  分布式多级缓存核心门面服务 (L1 Caffeine + L2 Redis)
 * 提供纳秒级本地缓存查询、热 Key 自动探测提升、防击穿互斥锁回源、防穿透空值拦截与防雪崩 TTL 随机扰动。
 * author:       alex
 * createDate:   2026/10/21
 * version:      1.0.0
 */
@Slf4j
public class MultiLevelCacheService {

    /**
     * 空值防御哨兵常量，防止缓存穿透
     */
    public static final String NULL_SENTINEL = "@@NULL_SENTINEL@@";

    private final MultiLevelCacheProperties properties;
    private final StringRedisTemplate stringRedisTemplate;
    private final HotKeyDetector hotKeyDetector;
    private final Cache<String, Object> l1Cache;

    /**
     * 细粒度行锁池，防止同一 Key 缓存击穿时并发轰炸数据库
     */
    private final ConcurrentHashMap<String, Object> mutexLocks = new ConcurrentHashMap<>();

    public MultiLevelCacheService(MultiLevelCacheProperties properties,
                                  StringRedisTemplate stringRedisTemplate,
                                  HotKeyDetector hotKeyDetector) {
        this(properties, stringRedisTemplate, hotKeyDetector, null);
    }

    public MultiLevelCacheService(MultiLevelCacheProperties properties,
                                  StringRedisTemplate stringRedisTemplate,
                                  HotKeyDetector hotKeyDetector,
                                  Cache<String, Object> customL1Cache) {
        this.properties = properties != null ? properties : new MultiLevelCacheProperties();
        this.stringRedisTemplate = stringRedisTemplate;
        this.hotKeyDetector = hotKeyDetector != null ? hotKeyDetector : new HotKeyDetector(this.properties);
        this.l1Cache = customL1Cache != null ? customL1Cache : Caffeine.newBuilder()
                .initialCapacity(this.properties.getL1InitialCapacity())
                .maximumSize(this.properties.getL1MaximumSize())
                .expireAfterWrite(this.properties.getL1DefaultTtlSeconds(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * 查询多级缓存（L1 Caffeine -> L2 Redis）
     *
     * @param key   缓存键
     * @param clazz 目标对象类型
     * @param <T>   泛型
     * @return 目标对象（若为空值哨兵则返回 null）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (key == null) {
            return null;
        }

        // 1. 热 Key 访问统计记录
        hotKeyDetector.recordAndCheck(key);

        // 2. 先查 L1 Caffeine 本地高速内存缓存
        Object l1Val = l1Cache.getIfPresent(key);
        if (l1Val != null) {
            if (NULL_SENTINEL.equals(l1Val)) {
                log.debug("L1 命中防穿透空值哨兵: key={}", key);
                return null;
            }
            if (clazz.isInstance(l1Val)) {
                return (T) l1Val;
            }
            return BeanUtils.stringToBean(l1Val.toString(), clazz);
        }

        // 3. L1 未命中，回源查询 L2 Redis 分布式缓存
        if (stringRedisTemplate != null) {
            try {
                String l2Val = stringRedisTemplate.opsForValue().get(key);
                if (l2Val != null) {
                    if (NULL_SENTINEL.equals(l2Val)) {
                        l1Cache.put(key, NULL_SENTINEL);
                        return null;
                    }
                    T bean = BeanUtils.stringToBean(l2Val, clazz);
                    if (bean != null) {
                        l1Cache.put(key, bean);
                        return bean;
                    }
                }
            } catch (Exception e) {
                log.warn("读取 L2 Redis 失败，降级执行: key={}, err={}", key, e.getMessage());
            }
        }

        return null;
    }

    /**
     * 带防击穿互斥锁、防穿透空值防御与防雪崩 TTL 随机扰动的原子加载查询
     *
     * @param key            缓存键
     * @param clazz          目标类型
     * @param dbLoader       数据库/远程业务加载函数
     * @param baseTtlSeconds 基准过期时间（秒）
     * @param <T>            泛型
     * @return 结果对象
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> dbLoader, long baseTtlSeconds) {
        return getWithLoader(key, clazz, dbLoader, baseTtlSeconds);
    }

    public <T> T getWithLoader(String key, Class<T> clazz, Supplier<T> dbLoader, long baseTtlSeconds) {
        if (key == null) {
            return null;
        }

        // 1. 尝试直接从多级缓存中获取
        T cached = get(key, clazz);
        if (cached != null) {
            return cached;
        }

        // 检查 L1 是否为空值哨兵（如果是空值哨兵，说明近期已查过库不存在，直接返回 null 防穿透）
        Object l1Val = l1Cache.getIfPresent(key);
        if (NULL_SENTINEL.equals(l1Val)) {
            return null;
        }

        // 2. 防击穿互斥锁保护：同一 JVM 内同一个 Key 仅允许 1 个线程执行 dbLoader 回源
        Object lock = mutexLocks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            try {
                // 双重检查锁定 (Double-Checked Locking)
                T recheck = get(key, clazz);
                if (recheck != null) {
                    return recheck;
                }
                if (NULL_SENTINEL.equals(l1Cache.getIfPresent(key))) {
                    return null;
                }

                // 3. 执行业务回源查询
                T dbResult = dbLoader.get();

                if (dbResult == null) {
                    // 4. 防穿透：写入空值哨兵，设置较短的过期时间
                    log.debug("回源查空，写入防穿透空值哨兵: key={}", key);
                    l1Cache.put(key, NULL_SENTINEL);
                    if (stringRedisTemplate != null) {
                        try {
                            stringRedisTemplate.opsForValue().set(key, NULL_SENTINEL, properties.getNullValueTtlSeconds(), TimeUnit.SECONDS);
                        } catch (Exception e) {
                            log.warn("写入 L2 空值哨兵失败: key={}, err={}", key, e.getMessage());
                        }
                    }
                    return null;
                }

                // 5. 防雪崩：在基准 TTL 上叠加随机扰动
                long finalTtl = calculateJitterTtl(baseTtlSeconds);

                // 6. 写入多级缓存
                l1Cache.put(key, dbResult);
                if (stringRedisTemplate != null) {
                    try {
                        String json = BeanUtils.beanToString(dbResult);
                        stringRedisTemplate.opsForValue().set(key, json, finalTtl, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.warn("写入 L2 Redis 失败: key={}, err={}", key, e.getMessage());
                    }
                }

                return dbResult;
            } finally {
                mutexLocks.remove(key);
            }
        }
    }

    /**
     * 写入多级缓存（同时更新 L2 Redis 并回填 L1）
     *
     * @param key            缓存键
     * @param value          缓存对象
     * @param baseTtlSeconds 基准过期时间（秒）
     */
    public void put(String key, Object value, long baseTtlSeconds) {
        if (key == null || value == null) {
            return;
        }

        l1Cache.put(key, value);
        if (stringRedisTemplate != null) {
            try {
                long finalTtl = calculateJitterTtl(baseTtlSeconds);
                String json = BeanUtils.beanToString(value);
                stringRedisTemplate.opsForValue().set(key, json, finalTtl, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("写入 L2 Redis 失败: key={}, err={}", key, e.getMessage());
            }
        }
    }

    /**
     * 双层原子淘汰（L1 与 L2 同步失效）
     *
     * @param key 缓存键
     */
    public void evict(String key) {
        if (key == null) {
            return;
        }

        l1Cache.invalidate(key);
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("删除 L2 Redis key 失败: key={}, err={}", key, e.getMessage());
            }
        }
    }

    /**
     * 按前缀批量失效双层缓存
     *
     * @param patternKey 前缀匹配键，例如 "finance:summary:100:*"
     */
    public void evictPattern(String patternKey) {
        if (patternKey == null || patternKey.trim().isEmpty()) {
            return;
        }
        String prefix = patternKey.endsWith("*") ? patternKey.substring(0, patternKey.length() - 1) : patternKey;
        try {
            l1Cache.asMap().keySet().removeIf(k -> k != null && k.startsWith(prefix));
        } catch (Exception e) {
            log.warn("失效 L1 Caffeine keys 失败: prefix={}, err={}", prefix, e.getMessage());
        }

        if (stringRedisTemplate != null) {
            try {
                java.util.Set<String> keys = stringRedisTemplate.keys(patternKey);
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                }
            } catch (Exception e) {
                log.warn("按前缀删除 L2 Redis keys 失败: pattern={}, err={}", patternKey, e.getMessage());
            }
        }
    }

    /**
     * 清空本地 L1 缓存（如接收到广播失效消息时）
     */
    public void clearLocal() {
        l1Cache.invalidateAll();
        log.info("L1 Caffeine 本地缓存已全量清空");
    }

    /**
     * 获取当前 L1 缓存实例
     */
    public Cache<String, Object> getL1Cache() {
        return l1Cache;
    }

    /**
     * 获取热 Key 探测器
     */
    public HotKeyDetector getHotKeyDetector() {
        return hotKeyDetector;
    }

    /**
     * 计算防雪崩随机抖动过期时间
     */
    long calculateJitterTtl(long baseTtlSeconds) {
        int maxJitter = properties.getMaxJitterSeconds();
        if (maxJitter <= 0 || baseTtlSeconds <= 0) {
            return baseTtlSeconds;
        }
        int jitter = ThreadLocalRandom.current().nextInt(maxJitter + 1);
        return baseTtlSeconds + jitter;
    }
}
