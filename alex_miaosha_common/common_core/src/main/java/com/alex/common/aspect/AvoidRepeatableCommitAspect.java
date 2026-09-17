package com.alex.common.aspect;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.constants.redis.RedisConstants;
import com.alex.common.handler.RequestHolder;
import com.alex.common.redis.key.CommonKey;
import com.alex.common.utils.ip.IpUtils;
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
 * description: 处理避免重复提交注解切面
 * author: alex
 * createDate: 2022/12/9 21:35
 * version: 2.0.0
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

        // 优先使用请求 Token 作为用户唯一凭证，无 Token 时以客户端真实 IP 兜底
        String token = request != null ? request.getHeader("token") : null;
        String identity = StringUtils.isNotBlank(token) ? token : ip;

        //目标类方法
        String className = method.getDeclaringClass().getName();
        String name = method.getName();
        Object[] args = point.getArgs();
        StringBuilder ipKey = new StringBuilder(String.format("%s#%s", className, name));
        // 转换成参数指纹（对 MultipartFile 提取文件名与长度，避免默认 identityHashCode 导致防重失效）
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    ipKey.append("_null");
                } else if (arg instanceof org.springframework.web.multipart.MultipartFile) {
                    org.springframework.web.multipart.MultipartFile f = (org.springframework.web.multipart.MultipartFile) arg;
                    ipKey.append(String.format("_file:%s:%d", f.getOriginalFilename(), f.getSize()));
                } else {
                    ipKey.append("_").append(arg.hashCode());
                }
            }
        }
        int hashCode = Math.abs(ipKey.toString().hashCode());

        String key = String.format("%s:%s_%d", RedisConstants.AVOID_REPEAT_COMMIT, identity, hashCode);
        log.debug("AvoidRepeatableCommit check: ipKey={}, hashCode={}, key={}", ipKey, hashCode, key);

        AvoidRepeatableCommit avoidRepeatableCommit = method.getAnnotation(AvoidRepeatableCommit.class);
        long timeout = avoidRepeatableCommit.timeout();

        // 基于 Redis SET NX PX 强原子抢占，彻底消除 Check-Then-Act 竞态击穿
        Boolean acquired = redisUtils.setIfAbsent(CommonKey.commonKey, key, "1", timeout, TimeUnit.MILLISECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("防重复提交拦截触发，请勿频繁提交！key={}", key);
            return Result.error(ResultEnum.REPEAT_COMMIT);
        }
        //执行方法
        return point.proceed();
    }
}
