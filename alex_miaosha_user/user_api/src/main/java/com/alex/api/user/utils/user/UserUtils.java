package com.alex.api.user.utils.user;

import com.alex.api.user.security.SecurityUser;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.LoginException;
import com.alex.common.redis.key.UserKey;
import com.alex.common.utils.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 *@description:  用户工具类
 *@author:       alex
 *@createDate:   2022/8/18 21:46
 *@version:      1.0.0
 */
@Component
@RequiredArgsConstructor
public class UserUtils {

    private final RedisUtils redisUtils;

    /**
     * @param request
     * @description: 根据request中的token获取当前用户的id
     * @author:      alex
     * @return:      java.lang.Long
    */
    public Long getUserId(HttpServletRequest request) throws LoginException {
        String authInfo = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authInfo)) {
            throw new LoginException(ResultEnum.NO_LOGIN);
        }
        String loginToken = authInfo.split("Bearer_")[1];
        // TODO: 2022/8/18 使用断言
        if (StringUtils.isEmpty(loginToken)) {
            throw new LoginException(ResultEnum.NO_LOGIN);
        }
        Object result = redisUtils.get(UserKey.getById, loginToken);
        if (result == null) {
            throw new LoginException(ResultEnum.NO_LOGIN);
        }
        return Long.parseLong(result.toString());
    }

    // TODO: 2023/1/10 设置获取当前登录人 
    public static SecurityUser getLoginUser() {

        /**
         SecurityContextHolder.getContext()获取安全上下文对象，就是那个保存在 ThreadLocal 里面的安全上下文对象
         总是不为null(如果不存在，则创建一个authentication属性为null的empty安全上下文对象)
         获取当前认证了的 principal(当事人),或者 request token (令牌)
         如果没有认证，会是 null,该例子是认证之后的情况
         */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //有登陆用户就返回登录用户，没有就返回null
        if (authentication != null) {
            //目前账户设置通过所有请求
            if (authentication instanceof AnonymousAuthenticationToken) {
                return null;
            }
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                return (SecurityUser) authentication.getPrincipal();
            }
        }
        return null;
    }
}
