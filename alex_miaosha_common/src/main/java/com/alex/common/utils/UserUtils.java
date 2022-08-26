package com.alex.common.utils;

import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.LoginException;
import com.alex.common.redis.key.UserKey;
import com.alex.common.redis.manager.RedisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
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

    // TODO: 2022/8/18 更优雅使用userutils 
    private final RedisService redisService;

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
        Object result = redisService.get(UserKey.getById, loginToken);
        if (result == null) {
            throw new LoginException(ResultEnum.NO_LOGIN);
        }
        return Long.parseLong(result.toString());
    }
}
