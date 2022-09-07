package com.alex.utils.interceptor;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.annotations.SeckillLimit;
import com.alex.common.handler.RequestHolder;
import com.alex.common.redis.key.AccessKey;
import com.alex.utils.redis.RedisUtils;
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
 *description:  避免接口重复提交aop
 *author:       alex
 *createDate:   2021/10/10 13:32
 *version:      1.0.0
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
// TODO: 2022/7/15 判断效果与 SeckillInterceptor是否一致
public class SeckillLimitAspect {

    private final RedisUtils redisUtils;

    /**
     * @param point
     * @description:  过滤AvoidRepeatableCommit，避免表单重复提交
     * @author:       alex
     * @return:       java.lang.Object
    */
    @Around("@annotation(com.alex.common.annotations.SeckillLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = RequestHolder.getRequest();
        String requestURI = request.getRequestURI();
        //获取注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        //获取注解
        SeckillLimit seckillLimit = method.getAnnotation(SeckillLimit.class);
        int seconds = seckillLimit.seconds();
        int maxCount = seckillLimit.maxCount();

        //目标类方法
        String className = method.getDeclaringClass().getName();
        String name = method.getName();
        //得到类名的方法
        String ipKey = String.format("%s#%s", className, name);
        //转换成hashCode
        int hashCode = Math.abs(ipKey.hashCode());
        AccessKey accessKey = AccessKey.withExpire;
        log.info("ipKey={}, requestURI={},key={}", ipKey, requestURI, accessKey);
        // TODO: 2022/8/25 添加ip信息
        //当前获取指定url的访问次数
        Integer count = (Integer) redisUtils.get(accessKey, requestURI);
        if (count == null) {
            redisUtils.set(accessKey, requestURI, 1, seconds);
        } else if (count < maxCount) {
            redisUtils.increase(accessKey, requestURI);
        } else {
            log.info("访问太频繁");
            return Result.error(ResultEnum.ACCESS_LIMIT_REACHED.getCode(), ResultEnum.ACCESS_LIMIT_REACHED.getValue());
        }
        //执行方法
        return point.proceed();
    }
}
