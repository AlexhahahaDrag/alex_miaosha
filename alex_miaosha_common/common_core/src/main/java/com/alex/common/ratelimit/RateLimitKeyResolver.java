package com.alex.common.ratelimit;

import com.alex.common.annotations.rate.LimitType;
import com.alex.common.annotations.rate.RateLimiter;
import com.alex.common.utils.ip.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 多维度分布式限流 Key 解析器
 * <p>支持解析全局接口、客户端真实 IP、登录用户 ID 以及基于 SpEL 表达式的入参提取。
 */
@Slf4j
public class RateLimitKeyResolver {

    private final ApplicationContext applicationContext;
    private final SpelExpressionParser spelParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

    public RateLimitKeyResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 根据请求与注解解析出唯一的限流 Key
     *
     * @param method      目标方法
     * @param args        方法入参
     * @param rateLimiter 限流注解
     * @param request     HTTP 请求上下文
     * @return 格式化后的限流 Key
     */
    public String resolveKey(Method method, Object[] args, RateLimiter rateLimiter, HttpServletRequest request) {
        String uri = resolveUri(method, request);
        LimitType limitType = rateLimiter.limitType();
        String customKey = rateLimiter.key();

        StringBuilder sb = new StringBuilder("ratelimit:");
        if (StringUtils.isNotBlank(customKey) && limitType != LimitType.PARAM) {
            sb.append(customKey).append(":");
        }

        switch (limitType) {
            case GLOBAL:
                sb.append("global:").append(uri);
                break;
            case IP:
                String clientIp = resolveClientIp(request);
                sb.append("ip:").append(clientIp).append(":").append(uri);
                break;
            case USER:
                String userId = resolveUserId(request);
                if (StringUtils.isNotBlank(userId)) {
                    sb.append("user:").append(userId).append(":").append(uri);
                } else {
                    // 未登录用户优雅降级为 IP 维度
                    String fallbackIp = resolveClientIp(request);
                    sb.append("user:anon:").append(fallbackIp).append(":").append(uri);
                }
                break;
            case PARAM:
                String paramValue = resolveSpelParam(method, args, customKey);
                if (StringUtils.isNotBlank(paramValue)) {
                    sb.append("param:").append(paramValue).append(":").append(uri);
                } else {
                    // SpEL 解析不到参数时降级为 IP 维度
                    String fallbackIp = resolveClientIp(request);
                    sb.append("param:fallback:").append(fallbackIp).append(":").append(uri);
                }
                break;
            default:
                sb.append("default:").append(uri);
                break;
        }

        return sb.toString();
    }

    private String resolveUri(Method method, HttpServletRequest request) {
        if (request != null && StringUtils.isNotBlank(request.getRequestURI())) {
            return request.getRequestURI();
        }
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    public String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        try {
            String ip = IpUtils.getIpAddr(request);
            return StringUtils.isNotBlank(ip) ? ip : "127.0.0.1";
        } catch (Throwable t) {
            log.warn("解析客户端 IP 异常，回退 127.0.0.1: {}", t.getMessage());
            return "127.0.0.1";
        }
    }

    public String resolveUserId(HttpServletRequest request) {
        if (request != null) {
            String headerUserId = request.getHeader("userId");
            if (StringUtils.isNotBlank(headerUserId)) {
                return headerUserId.trim();
            }
            String token = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(token)) {
                // 若仅有 token，提取哈希或作为标识
                return "token:" + Math.abs(token.hashCode());
            }
        }
        // 尝试通过 Spring 容器中可能存在的 UserUtils 获取
        if (applicationContext != null) {
            try {
                Object userUtilsBean = applicationContext.getBean("userUtils");
                Method getLoginUserMethod = userUtilsBean.getClass().getMethod("getLoginUser");
                Object loginUser = getLoginUserMethod.invoke(userUtilsBean);
                if (loginUser != null) {
                    Method getIdMethod = loginUser.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(loginUser);
                    if (id != null) {
                        return String.valueOf(id);
                    }
                }
            } catch (Throwable ignored) {
                // 安全忽略反射提取错误
            }
        }
        return null;
    }

    public String resolveSpelParam(Method method, Object[] args, String spelExpression) {
        if (StringUtils.isBlank(spelExpression) || args == null || args.length == 0) {
            return null;
        }
        try {
            String[] paramNames = paramDiscoverer.getParameterNames(method);
            if (paramNames == null || paramNames.length == 0) {
                return null;
            }
            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            Expression expression = spelParser.parseExpression(spelExpression);
            Object value = expression.getValue(context);
            return value != null ? String.valueOf(value) : null;
        } catch (Throwable t) {
            log.warn("解析限流 SpEL 表达式 [{}] 失败: {}", spelExpression, t.getMessage());
            return null;
        }
    }
}
