package com.alex.utils.aspact;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.constants.redis.RedisConstants;
import com.alex.common.handler.RequestHolder;
import com.alex.common.redis.key.CommonKey;
import com.alex.utils.IpUtils;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * description: 处理避免重复提交按钮
 * author: alex
 * createDate: 2022/12/9 21:35
 * version: 1.0.0
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AvoidRepeatableCommitAspect {

    private final RedisUtils redisUtils;

    /**
     * @param point
     * description:  过滤AvoidRepeatableCommit，避免表单重复提交
     * author:       alex
     * return:       java.lang.Object
     */
    @Around("@annotation(com.alex.common.annotations.AvoidRepeatableCommit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = RequestHolder.getRequest();
        String ip = IpUtils.getIpAddr(request);
        //获取注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        //目标类方法
        String className = method.getDeclaringClass().getName();
        String name = method.getName();
        Object[] args = point.getArgs();
        String ipKey = String.format("%s#%s", className, name);
        //转换成hashCode
        if (args != null && args.length > 0) {
            for(Object arg :args) {
                ipKey += arg.hashCode();
                log.info(arg.hashCode() + "");
            }
        }
        int hashCode = Math.abs(ipKey.hashCode());

        //得到类名的方法
        String key = String.format("%s:%s_%d", RedisConstants.AVOID_REPEAT_COMMIT, ip, hashCode);

        log.info("ipKey={}, hashCode={},key={}", ipKey, hashCode, key);

        //判断是否redis中存在，如果存在
        String value = redisUtils.get(CommonKey.commonKey.getPrefix() + RedisConstants.SEGMENTATION + key);
        if (StringUtils.isNotBlank(value)) {
            log.info("请勿重复提交表单！");
            return Result.error(ResultEnum.REPEAT_COMMIT);
        }
        //设置表单提交时间
        AvoidRepeatableCommit avoidRepeatableCommit = method.getAnnotation(AvoidRepeatableCommit.class);
        long timeout = avoidRepeatableCommit.timeout();
        redisUtils.setEx(CommonKey.commonKey, key, "1", timeout, TimeUnit.MILLISECONDS);
        //执行方法
        return point.proceed();
    }
}
