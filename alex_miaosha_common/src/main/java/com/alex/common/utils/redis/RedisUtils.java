package com.alex.common.utils.redis;

import com.alex.common.redis.key.KeyPrefix;
import com.alex.common.utils.bean.BeanUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    public String get(KeyPrefix prefix, String key) {
        try {
            String realKey = prefix.getPrefix() + SEGEMENT + key;
            return redisTemplate.opsForValue().get(realKey);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String result = redisTemplate.opsForValue().get(key);
            log.info("获取单个对象成功，key为{}，value为{}", key, result);
            T t = BeanUtils.stringToBean(result, clazz);
            log.info("获取单个对象成功，key为{}，value为{}", key, t);
            return BeanUtils.stringToBean(result, clazz);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            String result = redisTemplate.opsForValue().get(key);
            List<T> arr  = JSONArray.parseArray(result, clazz);
            log.info("获取单个对象成功，key为{}，value为{}", key, arr);
            return arr;
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e.getMessage());
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
            String realKey = prefix.getPrefix() + SEGEMENT + key;
            String result = redisTemplate.opsForValue().get(realKey);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            return BeanUtils.stringToBean(result, clazz);
        } catch (Exception e) {
            log.error("获取单个对象失败，key为{}，异常为{}", key, e.getMessage());
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
            String realKey = prefix.getPrefix() + SEGEMENT + key;
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(realKey, JSONObject.toJSONString(value));
            } else {
                redisTemplate.opsForValue().set(realKey, JSONObject.toJSONString(value), exTime, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("设置对象失败，key为{}，异常为{}", key, e.getMessage());
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
            log.error("设置对象失败，key为{}，异常为{}", key, e.getMessage());
            return false;
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
    public boolean setEx(KeyPrefix keyPrefix, String key, String value, long exTime, TimeUnit timeUnit) {
        try {
            if (exTime == 0) {
                //不设置过期时间
                redisTemplate.opsForValue().set(keyPrefix.getPrefix() + SEGEMENT + key, value);
            } else {
                redisTemplate.opsForValue().set(keyPrefix.getPrefix() + SEGEMENT + key, value, exTime, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
            }
            return true;
        } catch (Exception e) {
            log.error("设置对象失败，key为{}，异常为{}", key, e.getMessage());
            return false;
        }
    }

    public boolean set(String key, Object value) {
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(value));
        return true;
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
            redisTemplate.delete(prefix.getPrefix() + SEGEMENT + key);
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
            return Boolean.TRUE.equals(redisTemplate.hasKey(prefix.getPrefix() + SEGEMENT + key));
        } catch (Exception e) {
            log.error("判断key是否存在失败，key为{}，异常为{}", key, e.getMessage());
            return false;
        }
    }

    /**
     * @param prefix
     * @param key    description: key值增加1
     *               author: majf
     *               createDate: 2022/7/12 9:57
     *               return: java.lang.Long
     */
    public Long increase(KeyPrefix prefix, String key) {
        try {
            return redisTemplate.opsForValue().increment(prefix.getPrefix() + SEGEMENT + key, 1);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @param num    description: key值增加num
     *               author: majf
     *               createDate: 2022/7/12 9:58
     *               return: java.lang.Long
     */
    public Long increase(KeyPrefix prefix, String key, Long num) {
        try {
            return redisTemplate.opsForValue().increment(prefix.getPrefix() + SEGEMENT + key, num);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e.getMessage());
            return null;
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
            return redisTemplate.opsForValue().decrement(prefix.getPrefix() + SEGEMENT + key, 1);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * @param prefix
     * @param key
     * @param num    description: key值减少num
     *               author: majf
     *               createDate: 2022/7/12 9:58
     *               return: java.lang.Long
     */
    public Long decrease(KeyPrefix prefix, String key, Long num) {
        try {
            return redisTemplate.opsForValue().decrement(prefix.getPrefix() + SEGEMENT + key, num);
        } catch (Exception e) {
            log.error("key增加值1失败，key为{}，异常为{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * param: prefix
     * description: 获取所有模糊的key
     * author:      majf
     * return:      java.util.Set<java.lang.String>
    */
    public Set<String> keys(KeyPrefix prefix) {
        return redisTemplate.keys(prefix.getPrefix() + "*");
    }

    /**
     * @param prefix
     * @param clazz  description: 根据前缀模糊查询数据
     *               author: majf
     *               createDate: 2022/7/12 11:30
     *               return: java.util.List<T>
     */
    public <T> List<T> keys(KeyPrefix prefix, Class<T> clazz) {
        try {
            Set<String> keys = keys(prefix);
            if (keys == null || keys.isEmpty()) {
                return null;
            }
            return keys.parallelStream().map(item -> get(item, clazz)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据前缀模糊查询key失败，key为{},异常为{}", prefix.getPrefix(), e.getMessage());
            return null;
        }
    }

    public <T> List<T> keysList(KeyPrefix prefix, Class<T> clazz) {
        try {
            Set<String> keys = keys(prefix);
            if (keys == null || keys.isEmpty()) {
                return null;
            }
            return keys.parallelStream().flatMap(item -> {
                List<T> list = getList(item, clazz);
                list = (list == null) ? Collections.emptyList() : list;
                return list.stream();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据前缀模糊查询key失败，key为{},异常为{}", prefix.getPrefix(), e.getMessage());
            return null;
        }
    }
}
