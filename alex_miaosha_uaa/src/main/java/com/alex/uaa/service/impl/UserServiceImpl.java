package com.alex.uaa.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.alex.base.common.Result;
import com.alex.base.enums.RedisCacheTimeEnum;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.LoginException;
import com.alex.common.exception.RegisterException;
import com.alex.common.exception.UpdatePasswordException;
import com.alex.common.redis.key.UserKey;
import com.alex.common.redis.manager.RedisService;
import com.alex.uaa.manager.UserManager;
import com.alex.uaa.pojo.entity.User;
import com.alex.uaa.pojo.vo.LoginParam;
import com.alex.uaa.pojo.vo.RegisterParam;
import com.alex.uaa.pojo.vo.UpdatePasswordParam;
import com.alex.uaa.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @description: 用户服务实现类
 * @author: majf
 * @createDate: 2022/8/8 15:44
 * @version: 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RedisService redisService;

    private final UserManager userManager;

    @Override
    public Result<String> doLogin(LoginParam loginParam) {
        Result<User> userResult = login(loginParam);
        if (ResultEnum.SUCCESS.getCode() != userResult.getCode()) {
            throw new LoginException(userResult, loginParam.getMobile());
        }
        User user = userResult.getData();
        // TODO: 2022/8/8 修改为jwt
        String loginToken = getLoginToken(user);
        updateLastUpdateTime(user);
        redisService.set(UserKey.getById, loginToken, user.getId(), RedisCacheTimeEnum.LOGIN_EXTIME.getValue());
        log.info("用户{}登录成功", user.getId());
        return Result.success("登录成功！", loginToken);
    }

    @Override
    public Result<String> doLogout(HttpServletRequest request) {
        String loginToken = getUserIdByRequest(request);
        Long userId = redisService.get(UserKey.getById, loginToken, Long.class);
        redisService.delete(UserKey.getById, loginToken);
        log.info("用户{}已注销", userId);
        return Result.success();
    }

    @Override
    public Result doRegister(RegisterParam registerParam) {
        isRegistered(registerParam);
        try {
            userManager.save(getUserByRegisterParam(registerParam));
            log.info(registerParam.getRegisterMobile() + "用户注册成功");
        } catch (Exception e) {
            log.info(registerParam.getRegisterMobile() + "用户注册失败");
            throw new RegisterException(ResultEnum.SYSTEM_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result updatePassword(UpdatePasswordParam updatePasswordParam, HttpServletRequest request) {
        String loginToken = getUserIdByRequest(request);
        Long userId = redisService.get(UserKey.getById, loginToken, Long.class);
        User user = userManager.getById(userId);
        if (!user.getPassword().equals(updatePasswordParam.getOldPassword())) {
            throw new UpdatePasswordException(ResultEnum.PASSWORD_ERROR);
        }
        try {
            user.setPassword(updatePasswordParam.getNewPassword());
            userManager.updateById(user);
            log.info(user.getId() + "用户修改密码成功");
        } catch (Exception e) {
            log.info(user.getId() + "用户修改密码失败");
            throw new RegisterException(ResultEnum.SYSTEM_ERROR);
        }
        return Result.success();
    }

    /**
     * @param loginParam
     * @description: 登录
     * @author: majf
     * @return: com.alex.base.common.Result<com.alex.uaa.pojo.entity.User>
     */
    private Result<User> login(LoginParam loginParam) {
        User user = userManager.getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, loginParam.getMobile()));
        if (user == null) {
            return Result.error(ResultEnum.MOBILE_NOT_EXIST);
        }
        // TODO: 2022/8/8 传入的密码是明文，数据库的密码是加密后的信息，如何判断相等
        if (!user.getPassword().equals(loginParam.getPassword())) {
            return Result.error(ResultEnum.PASSWORD_ERROR);
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * @param user
     * @description: 设置token
     * @author: majf
     * @return: java.lang.String
     */
    private String getLoginToken(User user) {
        return SecureUtil.md5(user.getPhone() + RandomUtil.randomInt(100000));
    }

    /**
     * @param user
     * @description: 异步更新登录时间
     * @author: majf
     * @return: void
     */
    @Async(value = "myExecutor")
    public void updateLastUpdateTime(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        userManager.updateById(user);
    }

    /**
     * @param request
     * @description: 通过请求获取token
     * @author: majf
     * @return: java.lang.String
     */
    private String getUserIdByRequest(HttpServletRequest request) {
        String authInfo = request.getHeader("Authorization");
        return authInfo.split("Bearer")[1];
    }

    /**
     * @param registerParam
     * @description: 判断电话号、身份证是否被注册过
     * @author:      majf
     * @return:      boolean
    */
    private boolean isRegistered(RegisterParam registerParam) {
        if (userManager.getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, registerParam.getRegisterMobile())) != null) {
            throw new RegisterException(ResultEnum.REPEATED_REGISTER_MOBILE);
        }
        if (userManager.getOne(Wrappers.<User>lambdaQuery().eq(User::getIdentityCardId, registerParam.getRegisterIdentity())) != null) {
            throw new RegisterException(ResultEnum.REPEATED_REGISTER_IDENTITY);
        }
        if (userManager.getOne(Wrappers.<User>lambdaQuery().eq(User::getUserName, registerParam.getRegisterUsername())) != null) {
            throw new RegisterException(ResultEnum.REPEATED_REGISTER_USERNAME);
        }
        return false;
    }

    /**
     * @param registerParam
     * @description: 通过注册信息转化成用户信息
     * @author:      majf
     * @return:      com.alex.uaa.pojo.entity.User
    */
    private User getUserByRegisterParam(RegisterParam registerParam) {
        return User.builder()
                .userName(registerParam.getRegisterUsername())
                .identityCardId(registerParam.getRegisterIdentity())
                .phone(registerParam.getRegisterMobile())
                .password(registerParam.getRegisterPassword())
                .build();
    }
}
