package com.alex.user.utils.user;

import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.LoginException;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.redis.key.UserKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 *@description:  用户工具类
 *@author:       alex
 *@createDate:   2022/8/18 21:46
 *@version:      1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
            throw new LoginException(ResultEnum.USER_NO_LOGIN);
        }
        String loginToken = authInfo.split("Bearer_")[1];
        // TODO: 2022/8/18 使用断言
        if (StringUtils.isEmpty(loginToken)) {
            throw new LoginException(ResultEnum.USER_NO_LOGIN);
        }
        Object result = redisUtils.get(UserKey.getById, loginToken);
        if (result == null) {
            throw new LoginException(ResultEnum.USER_NO_LOGIN);
        }
        return Long.parseLong(result.toString());
    }

    // TODO: 2023/1/10 设置获取当前登录人 
    public TUserVo getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request =((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String uuidToken = request.getHeader("Authorization");
        if (StringUtils.isEmpty(uuidToken)) {
            return null;
        }
        String barToken = redisUtils.get(LoginKey.loginUuid, uuidToken);
        if (barToken == null) {
            return  null;
        }
        String onlineAdminStr = redisUtils.get(LoginKey.loginToken, barToken);
        if (StringUtils.isEmpty(onlineAdminStr)) {
            return null;
        }
        return JSONObject.parseObject(onlineAdminStr, TUserVo.class);
    }

    public static TUserVo getCurUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TUserVo tUserVo = JSONObject.parseObject(authentication.getCredentials().toString(), TUserVo.class);
        log.info("当前登录人：{}", JSONObject.toJSONString(tUserVo));
        return tUserVo;
    }
}
