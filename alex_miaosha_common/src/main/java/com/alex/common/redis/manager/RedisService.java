package com.alex.common.redis.manager;

import com.alex.common.redis.key.KeyPrefix;
import com.alex.common.utils.BeanUtils;
import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description: ?:匹配某个字符
 * *:匹配多个字符
 * [ae]:匹配某些字符
 * @author: alex
 * @createDate: 2022/8/12 21:41
 * @version: 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

    private static final String SEGEMENT = ":";

    final RedisTemplate<String, Object> redisTemplate;

    public String get(String key) {
        try {
            String result = (String) redisTemplate.opsForValue().get(key);
            return result;
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
            String realKey = prefix.getPrefix() + SEGEMENT + key ;
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
     * @param key
     * @param exTime
     * @description: 设置key的过期时间
     * @author: majf
     * @createDate: 2022/7/12 9:50
     * @return: void
     */
    public void expire(KeyPrefix prefix, String key, int exTime) {
        try {
            redisTemplate.expire(prefix.getPrefix() + SEGEMENT + key, exTime, TimeUnit.SECONDS);
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
            String result = BeanUtils.beanToString(value);
            if (result == null || result.length() == 0) {
                return false;
            }
            String realKey = prefix.getPrefix() + SEGEMENT + key;
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(realKey, result);
            } else {
                redisTemplate.opsForValue().set(realKey, result, exTime, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("设置对象失败，key为{}，异常为{}", key, e);
            return false;
        }
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
            return redisTemplate.opsForValue().decrement(prefix.getPrefix() +  SEGEMENT + key, num);
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
