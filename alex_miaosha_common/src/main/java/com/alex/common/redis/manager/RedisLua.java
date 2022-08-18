package com.alex.common.redis.manager;

import com.alex.common.redis.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 *description:  redis lua 脚本
 *author:       majf
 *createDate:   2022/7/14 11:26
 *version:      1.0.0
 */
// TODO: 2022/8/13 学习lua脚本
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisLua {

    // TODO: 2022/7/14 学习lua脚本
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * @param goodsId
     * @description: 判断库存和预减库存
     * @author:      majf
     * @createDate:  2022/7/14 11:30
     * @return:      java.lang.Long
    */
    public Long judgeStockAndDecrStock(Long goodsId) {
        String stockScript = "local stock = tonumber(redis.call('get', KEYS[1])); " +
                " if (stock <= 0) then " +
                " return -1;" +
                " else " +
                " return redis.call('DECR', KEYS[1]); " +
                " end; ";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(stockScript, Long.class);
        log.info("===============================key:{}========================", RedisConstants.SECKILL_KEY + ":" + goodsId);
        return redisTemplate.execute(redisScript, Collections.singletonList(RedisConstants.SECKILL_KEY + ":" + goodsId));
    }

    /**
     * @param lockKey
     * @description: 获取访问次数
     * @author:      majf
     * @createDate:  2022/7/14 11:48
     * @return:      java.lang.Long
    */
    public Long getVisitorCount(String lockKey) {
        try {
            String countScript = "local num = tonumber(redis.call('get', KEYS[1]));" +
                    "return num;";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(countScript, Long.class);
            return redisTemplate.execute(redisScript, Collections.singletonList(lockKey));
        } catch (Exception e) {
            log.error("统计访问次数失败！错误信息: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param lockKey
     * @description: 添加访问次数
     * @author:      majf
     * @createDate:  2022/7/14 11:48
     * @return:      java.lang.Long
    */
    public Long addVisitorCount(String lockKey) {
        String addNumScript = "local num = redis.call('incr', KEYS[1]) return num;";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(addNumScript, Long.class);
        return redisTemplate.execute(redisScript, Collections.singletonList(lockKey));
    }
}
