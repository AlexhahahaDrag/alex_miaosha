package com.alex.utils.interceptor;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.common.handler.RequestHolder;
import com.alex.common.redis.key.AccessKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.utils.IpUtils;
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
 * description: 访问次数限制aop
 * author:       majf
 * createDate:   2022/8/8 17:49
 * version:      1.0.0
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AccessLimitAspect {

    private final RedisUtils redisUtils;

    /**
     * @param point
     * description:  过滤AccessLimit，访问次数限制
     * author:       alex
     * return:       java.lang.Object
    */
    // TODO (majf) 2024/1/16 19:57 测试功能是否生效
    // todo 这里需要优化，使用redis的布隆过滤器，减少redis的访问
    // 这里如何添加布隆过滤器
    @Around("@annotation(com.alex.common.annotations.user.AccessLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = RequestHolder.getRequest();
        //获取注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        //获取注解
        AccessLimit accessLimit = method.getAnnotation(AccessLimit.class);
        int limit = accessLimit.limit();
        int timeout = accessLimit.timeout();

        AccessKey accessKey = AccessKey.withExpire;
        String ip = IpUtils.getIpAddr(request);
        //当前获取指定url的访问次数
        Integer count = redisUtils.get(accessKey, ip, Integer.class);
        if (count == null) {
            redisUtils.set(accessKey, ip, 1, timeout);
        } else if (count < limit) {
            redisUtils.increase(accessKey, ip);
        } else {
            log.info("用户ip：{}，访问太频繁", ip);
            return Result.error(ResultEnum.ACCESS_LIMIT_REACHED.getCode(), ResultEnum.ACCESS_LIMIT_REACHED.getValue());
        }
        //执行方法
        return point.proceed();
    }
}
