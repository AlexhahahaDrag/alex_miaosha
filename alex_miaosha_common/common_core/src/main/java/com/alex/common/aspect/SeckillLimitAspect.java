package com.alex.common.aspect;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.annotations.SeckillLimit;
import com.alex.common.handler.RequestHolder;
import com.alex.common.redis.key.AccessKey;
import com.alex.common.utils.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * description:  秒杀限流aop切面
 * author:       alex
 * createDate:   2021/10/10 13:32
 * version:      2.0.0
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SeckillLimitAspect {

    private final RedisUtils redisUtils;

    /**
     * @param point
     * description:  过滤SeckillLimit，限制接口并发与频次
     * author:       alex
     * return:       java.lang.Object
     */
    @Around("@annotation(com.alex.common.annotations.SeckillLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = RequestHolder.getRequest();
        if (request == null) {
            return null;
        }
        String requestURI = request.getRequestURI();
        //获取注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        //获取注解
        SeckillLimit seckillLimit = method.getAnnotation(SeckillLimit.class);
        int maxCount = seckillLimit.maxCount();
        int seconds = seckillLimit.seconds();

        AccessKey accessKey = AccessKey.withExpire;
        String ip = com.alex.common.utils.ip.IpUtils.getIpAddr(request);
        String key = requestURI + ":" + ip;

        // 原子累加并在初次访问时绑定 seconds() 有效期，彻底根治首击 Integer.parseInt(null) 崩溃及无过期死锁
        Long count = redisUtils.incrementWithExpire(accessKey, key, seconds, java.util.concurrent.TimeUnit.SECONDS);
        if (count != null && count > maxCount) {
            log.warn("秒杀接口访问过于频繁: URI={}, IP={}, count={}, limit={}/{}s", requestURI, ip, count, maxCount, seconds);
            return Result.error(ResultEnum.ACCESS_LIMIT_REACHED.getCode(), ResultEnum.ACCESS_LIMIT_REACHED.getValue());
        }
        //执行方法
        return point.proceed();
    }
}
