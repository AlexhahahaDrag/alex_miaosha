package com.alex.utils.redis;

import cn.hutool.json.JSONUtil;
import com.alex.common.redis.key.KeyPrefix;
import com.alex.utils.bean.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisUtils {

    private static final String SEGEMENT = ":";

    final StringRedisTemplate redisTemplate;

    public String get(String key) {
        try {
            String result = redisTemplate.opsForValue().get(key);
            return result;
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    public String get(KeyPrefix prefix, String key) {
        try {
            String realKey = prefix.getPrefix() + SEGEMENT + key;
            return redisTemplate.opsForValue().get(realKey);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String result = (String) redisTemplate.opsForValue().get(key);
            return BeanUtils.stringToBean(result, clazz);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @param clazz
     * @description: 根据key获取类信息
     * @author: majf
     * @createDate: 2022/7/12 9:50
     * @return: T
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        try {
            String realKey = prefix.getPrefix() + SEGEMENT + key;
            String result = (String) redisTemplate.opsForValue().get(realKey);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            return BeanUtils.stringToBean(result, clazz);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    /**
     * @param prefix
     * @param value
     * @param exTime
     * @description: 设置key的过期时间
     * @author: majf
     * @createDate: 2022/7/12 9:50
     * @return: void
     */
    public void expire(KeyPrefix prefix, String key, String value, Long exTime) {
        try {
            redisTemplate.opsForValue().set(prefix.getPrefix() + SEGEMENT + key, value, exTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置过期时间失败，key为{}，异常为{}", prefix.getPrefix() + SEGEMENT + key, e);
        }
    }

    public void expire(KeyPrefix prefix, String key, Long exTime, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(prefix.getPrefix() + SEGEMENT + key, "1", exTime, unit);
        } catch (Exception e) {
            log.error("设置过期时间失败，key为{}，异常为{}", key, e);
        }
    }

    /**
     * @param prefix
     * @param key
     * @param value
     * @param exTime
     * @description: 设置key-value
     * @author: majf
     * @createDate: 2022/7/12 9:51
     * @return: boolean
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value, int exTime) {
        try {
            String realKey = prefix.getPrefix() + SEGEMENT + key;
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(realKey, JSONUtil.toJsonStr(value));
            } else {
                redisTemplate.opsForValue().set(realKey, JSONUtil.toJsonStr(value), exTime, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("设置对象失败，key为{}，异常为{}", key, e);
            return false;
        }
    }

    /**
     * @param key
     * @param value
     * @param exTime
     * @param timeUnit
     * @description: 
     * @author:      majf
     * @return:      boolean
    */
    public boolean setEx(String key, String value, int exTime, TimeUnit timeUnit) {
        try {
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(key, value);
            } else {
                redisTemplate.opsForValue().set(key, value, exTime, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
            }
            return true;
        } catch (Exception e) {
            log.error("设置对象失败，key为{}，异常为{}", key, e);
            return false;
        }
    }

    public boolean set(String key, Object value) {
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value));
        return true;
    }

    /**
     * @param prefix
     * @param key
     * @description: 删除key
     * @author: majf
     * @createDate: 2022/7/12 9:57
     * @return: void
     */
    public void delete(KeyPrefix prefix, String key) {
        try {
            redisTemplate.delete(prefix.getPrefix() + SEGEMENT + key);
        } catch (Exception e) {
            log.error("删除key失败，key为{}，异常为{}", key, e);
        }
    }

    /**
     * @param prefix
     * @param key
     * @description: 判断key是否存在
     * @author: majf
     * @createDate: 2022/7/12 9:57
     * @return: boolean
     */
    public boolean exists(KeyPrefix prefix, String key) {
        try {
            return redisTemplate.hasKey(prefix.getPrefix() + SEGEMENT + key);
        } catch (Exception e) {
            log.error("判断key是否存在失败，key为{}，异常为{}", key, e);
            return false;
        }
    }

    /**
     * @param prefix
     * @param key
     * @description: key值增加1
     * @author: majf
     * @createDate: 2022/7/12 9:57
     * @return: java.lang.Long
     */
    public Long increase(KeyPrefix prefix, String key) {
        try {
            return redisTemplate.opsForValue().increment(prefix.getPrefix() + SEGEMENT + key, 1);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @param num
     * @description: key值增加num
     * @author: majf
     * @createDate: 2022/7/12 9:58
     * @return: java.lang.Long
     */
    public Long increase(KeyPrefix prefix, String key, Long num) {
        try {
            return redisTemplate.opsForValue().increment(prefix.getPrefix() + SEGEMENT + key, num);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @description: key值减少1
     * @author: majf
     * @createDate: 2022/7/12 9:58
     * @return: java.lang.Long
     */
    public Long decrease(KeyPrefix prefix, String key) {
        try {
            return redisTemplate.opsForValue().decrement(prefix.getPrefix() + SEGEMENT + key, 1);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @param num
     * @description: key值减少num
     * @author: majf
     * @createDate: 2022/7/12 9:58
     * @return: java.lang.Long
     */
    public Long decrease(KeyPrefix prefix, String key, Long num) {
        try {
            return redisTemplate.opsForValue().decrement(prefix.getPrefix() + SEGEMENT + key, num);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e);
            return null;
        }
    }

    /**
     * @param prefix
     * @param clazz
     * @description: 根据前缀模糊查询数据
     * @author: majf
     * @createDate: 2022/7/12 11:30
     * @return: java.util.List<T>
     */
    public <T> List<T> keys(KeyPrefix prefix, Class<T> clazz) {
        try {
            Set<String> keys = redisTemplate.keys(prefix.getPrefix() + "*");
            if (keys == null || keys.isEmpty()) {
                return null;
            }
            return keys.parallelStream().map(item -> get(item, clazz)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据前缀模糊查询key失败，key为{}，异常为{}", prefix.getPrefix(), e);
            return null;
        }
    }
}
