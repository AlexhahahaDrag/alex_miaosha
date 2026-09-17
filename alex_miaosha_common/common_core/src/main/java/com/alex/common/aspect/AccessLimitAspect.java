package com.alex.common.aspect;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.common.handler.RequestHolder;
import com.alex.common.redis.key.AccessKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.ip.IpUtils;
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
 * description: 访问次数限制aop切面
 * author:       majf, alex
 * createDate:   2022/8/8 17:49
 * version:      2.0.0
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
        String uri = request != null ? request.getRequestURI() : method.getDeclaringClass().getSimpleName() + "#" + method.getName();
        // 绑定 URI 与 IP，彻底解决不同接口之间全局配额撞车问题
        String key = uri + ":" + ip;

        // 原子递增并在首次生成 Key 时设置 TTL，消除并发竞态漂移
        Long count = redisUtils.incrementWithExpire(accessKey, key, timeout, java.util.concurrent.TimeUnit.SECONDS);
        if (count != null && count > limit) {
            log.warn("用户IP: {} 访问接口 {} 过于频繁，当前次数: {}，上限: {}", ip, uri, count, limit);
            return Result.error(ResultEnum.ACCESS_LIMIT_REACHED.getCode(), ResultEnum.ACCESS_LIMIT_REACHED.getValue());
        }
        //执行方法
        return point.proceed();
    }
}
