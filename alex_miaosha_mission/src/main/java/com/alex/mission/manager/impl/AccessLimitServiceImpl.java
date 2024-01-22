package com.alex.mission.manager.impl;

import com.alex.mission.manager.AccessLimitService;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 *description:
 *    rateLimiter是基于令牌桶算法来做的　
 * 　　guava的RateLimiter使用的是令牌桶算法，也就是以固定的频率向桶中放入令牌，例如一秒钟10枚令牌，实际业务在每次响应请求之前都从桶中获取令牌，只有取到令牌的请求才会被成功响应，
 *    获取的方式有两种：阻塞等待令牌或者取不到立即返回失败
 *author:       majf
 *createDate:   2022/7/11 10:34
 *version:      1.0.0
 */
@Service
public class AccessLimitServiceImpl implements AccessLimitService {

    private RateLimiter seckillRateLimiter = RateLimiter.create(10);

    @Override
    public boolean tryAcquireToken() {
        return seckillRateLimiter.tryAcquire(3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        AccessLimitServiceImpl accessLimitService = new AccessLimitServiceImpl();
        Thread.sleep(1000);
        for (int i = 0; i < 100; i++) {
            boolean b = accessLimitService.tryAcquireToken();
            System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "   " + b);
        }
    }
}
