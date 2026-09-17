package com.alex.common.utils.redis;

import com.alex.common.redis.key.KeyPrefix;
import com.alex.common.utils.bean.BeanUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisUtils {

    private static final String SEGMENT = ":";

    final StringRedisTemplate redisTemplate;

    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    public String get(KeyPrefix prefix, String key) {
        try {
            String realKey = prefix.getPrefix() + SEGMENT + key;
            return redisTemplate.opsForValue().get(realKey);
        } catch (Exception e) {
            log.error("获取单个对象失败，prefix为{}，key为{}，异常为{}", prefix.getPrefix(), key, e.getMessage());
            return null;
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String result = redisTemplate.opsForValue().get(key);
            T t = BeanUtils.stringToBean(result, clazz);
            log.debug("获取单个对象成功，class为{}，key为{}，value为{}", clazz.getName(), key, t);
            return t;
        } catch (Exception e) {
            log.error("获取单个对象失败，class为{}，key为{}，异常为{}", clazz.getName(), key, e.getMessage());
            return null;
        }
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            String result = redisTemplate.opsForValue().get(key);
            List<T> arr = JSONArray.parseArray(result, clazz);
            log.debug("获取列表对象成功，key为{}，value为{}", key, arr);
            return arr;
        } catch (Exception e) {
            log.error("获取列表对象失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @param clazz  description: 根据key获取类信息
     *               author: majf
     *               createDate: 2022/7/12 9:50
     *               return: T
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        try {
            String realKey = prefix.getPrefix() + SEGMENT + key;
            String result = redisTemplate.opsForValue().get(realKey);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            return BeanUtils.stringToBean(result, clazz);
        } catch (Exception e) {
            log.error("获取单个前缀对象失败，class为{}，key为{}，异常为{}", clazz.getName(), key, e.getMessage());
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @param value
     * @param exTime description: 设置key-value
     *               author: majf
     *               createDate: 2022/7/12 9:51
     *               return: boolean
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value, int exTime) {
        try {
            String realKey = prefix.getPrefix() + SEGMENT + key;
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(realKey, JSONObject.toJSONString(value, SerializerFeature.DisableCircularReferenceDetect));
            } else {
                redisTemplate.opsForValue().set(realKey, JSONObject.toJSONString(value, SerializerFeature.DisableCircularReferenceDetect), exTime, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("设置对象失败，prefix:{}, key为{}，异常为{}", prefix.getPrefix(), key, e.getMessage());
            return false;
        }
    }

    /**
     * @param key
     * @param value
     * @param exTime
     * @param timeUnit description:
     *                 author:      majf
     *                 return:      boolean
     */
    public void setEx(String key, String value, int exTime, TimeUnit timeUnit) {
        try {
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(key, value);
            } else {
                redisTemplate.opsForValue().set(key, value, exTime, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
            }
        } catch (Exception e) {
            log.error("设置对象失败，key为{}，value:{}, 异常为{}", key, value, e.getMessage());
        }
    }

    /**
     * @param keyPrefix
     * @param key
     * @param value
     * @param exTime
     * @param timeUnit  description:
     *                  author:      majf
     *                  return:      boolean
     */
    public void setEx(KeyPrefix keyPrefix, String key, String value, long exTime, TimeUnit timeUnit) {
        try {
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(keyPrefix.getPrefix() + SEGMENT + key, value);
            } else {
                redisTemplate.opsForValue().set(keyPrefix.getPrefix() + SEGMENT + key, value, exTime, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
            }
        } catch (Exception e) {
            log.error("设置对象失败，key为{}，异常为{}", key, e.getMessage());
        }
    }

    public boolean set(String key, Object value) {
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(value, SerializerFeature.DisableCircularReferenceDetect));
        return true;
    }

    /**
     * 原子设置分布式防重/排他锁（SET NX PX/EX）
     *
     * @param prefix   前缀
     * @param key      键
     * @param value    值
     * @param timeout  超时时长
     * @param timeUnit 时间单位
     * @return 是否成功抢占
     */
    public Boolean setIfAbsent(KeyPrefix prefix, String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            String realKey = (prefix != null ? prefix.getPrefix() + SEGMENT : "") + key;
            return redisTemplate.opsForValue().setIfAbsent(realKey, value, timeout, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
        } catch (Exception e) {
            log.error("setIfAbsent设置失败，key为{}，异常为{}", key, e.getMessage());
            return false;
        }
    }

    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit timeUnit) {
        return setIfAbsent(null, key, value, timeout, timeUnit);
    }

    /**
     * 原子递增并在首次创建时设定过期时间
     *
     * @param prefix   前缀
     * @param key      键
     * @param timeout  超时时长
     * @param timeUnit 时间单位
     * @return 递增后的计数值
     */
    public Long incrementWithExpire(KeyPrefix prefix, String key, long timeout, TimeUnit timeUnit) {
        try {
            String realKey = (prefix != null ? prefix.getPrefix() + SEGMENT : "") + key;
            Long count = redisTemplate.opsForValue().increment(realKey, 1);
            if (count != null && count == 1) {
                redisTemplate.expire(realKey, timeout, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
            }
            return count;
        } catch (Exception e) {
            log.error("incrementWithExpire失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * @param prefix
     * @param key    description: 删除key
     *               author: majf
     *               createDate: 2022/7/12 9:57
     *               return: void
     */
    public void delete(KeyPrefix prefix, String key) {
        try {
            redisTemplate.delete(prefix.getPrefix() + SEGMENT + key);
        } catch (Exception e) {
            log.error("删除key失败，key为{}，异常为{}", key, e.getMessage());
        }
    }

    /**
     * @param prefix
     * @param key    description: 判断key是否存在
     *               author: majf
     *               createDate: 2022/7/12 9:57
     *               return: boolean
     */
    public boolean exists(KeyPrefix prefix, String key) {
        try {
            return redisTemplate.hasKey(prefix.getPrefix() + SEGMENT + key);
        } catch (Exception e) {
            log.error("判断key是否存在失败，key为{}，异常为{}", key, e.getMessage());
            return false;
        }
    }

    /**
     * @param prefix
     * @param key
     * description: key值增加1
     * author: majf
     * createDate: 2022/7/12 9:57
     * return: java.lang.Long
     */
    public void increase(KeyPrefix prefix, String key) {
        try {
            redisTemplate.opsForValue().increment(prefix.getPrefix() + SEGMENT + key, 1);
        } catch (Exception e) {
            log.error("key增加值1失败，前缀：{}， key为{}，异常为{}", prefix.getPrefix(), key, e.getMessage());
        }
    }

    /**
     * @param prefix
     * @param key    description: key值减少1
     *               author: majf
     *               createDate: 2022/7/12 9:58
     *               return: java.lang.Long
     */
    public Long decrease(KeyPrefix prefix, String key) {
        try {
            return redisTemplate.opsForValue().decrement(prefix.getPrefix() + SEGMENT + key, 1);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 基于 Redis SCAN 游标的非阻塞迭代，避免生产环境使用 KEYS 命令阻塞单线程事件循环
     *
     * @param pattern 模糊匹配表达式（例如 "prefix:*"）
     * @return 匹配的键集合
     */
    public Set<String> scan(String pattern) {
        Set<String> keys = new java.util.HashSet<>();
        if (StringUtils.isEmpty(pattern)) {
            return keys;
        }
        try {
            org.springframework.data.redis.core.ScanOptions options = org.springframework.data.redis.core.ScanOptions.scanOptions()
                    .match(pattern)
                    .count(200)
                    .build();
            try (org.springframework.data.redis.core.Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
            }
        } catch (Exception e) {
            log.error("Redis 非阻塞 scan 扫描异常，pattern为{}，异常为{}", pattern, e.getMessage());
        }
        return keys;
    }

    /**
     * 基于前缀进行非阻塞 scan
     *
     * @param prefix 前缀
     * @return 匹配的键集合
     */
    public Set<String> scan(KeyPrefix prefix) {
        if (prefix == null) {
            return Collections.emptySet();
        }
        return scan(prefix.getPrefix() + "*");
    }

    /**
     * 获取指定前缀的所有键（内部已切换为基于游标的非阻塞 SCAN 机制，消除 KEYS * 阻塞）
     *
     * @param prefix 前缀
     * @return 匹配的键集合
     */
    public Set<String> keys(KeyPrefix prefix) {
        return scan(prefix);
    }

    /**
     * @param prefix
     * @param clazz  description: 根据前缀模糊查询数据（非阻塞扫描）
     *               author: majf
     *               createDate: 2022/7/12 11:30
     *               return: java.util.List<T>
     */
    public <T> List<T> keys(KeyPrefix prefix, Class<T> clazz) {
        try {
            Set<String> keys = scan(prefix);
            if (keys == null || keys.isEmpty()) {
                return null;
            }
            return keys.parallelStream().map(item -> get(item, clazz)).toList();
        } catch (Exception e) {
            log.error("根据前缀模糊查询key失败，key为{},异常为{}", prefix != null ? prefix.getPrefix() : null, e.getMessage());
            return null;
        }
    }

    public <T> List<T> keysList(KeyPrefix prefix, Class<T> clazz) {
        try {
            Set<String> keys = scan(prefix);
            if (keys == null || keys.isEmpty()) {
                return null;
            }
            return keys.parallelStream().flatMap(item -> {
                List<T> list = getList(item, clazz);
                list = (list == null) ? Collections.emptyList() : list;
                return list.stream();
            }).toList();
        } catch (Exception e) {
            log.error("根据前缀模糊查询keysList失败，key为{},异常为{}", prefix != null ? prefix.getPrefix() : null, e.getMessage());
            return null;
        }
    }
}
