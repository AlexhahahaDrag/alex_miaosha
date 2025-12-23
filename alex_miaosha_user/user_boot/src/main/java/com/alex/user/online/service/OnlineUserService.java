package com.alex.user.online.service;

import com.alex.api.user.vo.user.OnlineAdmin;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.tUserLogin.entity.TUserLogin;
import com.alex.utils.IpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * 在线用户管理服务
 *
 * @author alex
 * createDate 2024/12/19
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineUserService {

    private final RedisUtils redisUtils;

    /**
     * 异步添加在线用户
     *
     * @param userLogin 用户登录信息
     * @param expiration 过期时间
     */
    @Async("onlineUserExecutor")
    public void addOnlineUserAsync(TUserLogin userLogin, long expiration) {
        try {
            log.debug("开始异步添加在线用户，用户: {}", userLogin.getUsername());
            // 构建在线用户信息
            OnlineAdmin onlineAdmin = buildOnlineAdmin(userLogin, expiration);
            // 获取 IP地址信息
            enrichIpLocation(onlineAdmin, userLogin.getLoginIp(), expiration);
            // 存储到 Redis
            redisUtils.setEx(LoginKey.loginOnlineUser, userLogin.getTokenId(), String.valueOf(onlineAdmin), (int) expiration, TimeUnit.SECONDS);
            log.debug("异步添加在线用户成功，用户: {}, tokenId: {}", userLogin.getUsername(), userLogin.getTokenId());
        } catch (Exception e) {
            log.error("异步添加在线用户失败，用户: {}, 错误: {}", userLogin.getUsername(), e.getMessage(), e);
        }
    }

    /**
     * 构建在线用户信息
     *
     * @param userLogin 用户登录信息
     * @param expiration 过期时间
     * @return OnlineAdmin
     */
    private OnlineAdmin buildOnlineAdmin(TUserLogin userLogin, long expiration) {
        return OnlineAdmin.builder()
                .userId(userLogin.getUserId())
                .tokenId(userLogin.getTokenId())
                .token(userLogin.getToken())
                .os(userLogin.getOs())
                .browser(userLogin.getBroswer())
                .ipaddr(userLogin.getLoginIp())
                .loginLocation(userLogin.getLoginLocation())
                .loginTime(DateUtils.getTimeStr(userLogin.getLastLoginTime()))
                .roleName(null)
                .username(userLogin.getUsername())
                .expireTime(DateUtils.getTimeStr(DateUtils.addTime(LocalDateTime.now(), expiration, ChronoUnit.MICROS)))
                .build();
    }

    /**
     * 丰富 IP地址信息
     *
     * @param onlineAdmin 在线用户信息
     * @param loginIp 登录 IP
     * @param expiration 过期时间
     */
    private void enrichIpLocation(OnlineAdmin onlineAdmin, String loginIp, long expiration) {
        try {
            // 从 Redis中获取IP来源
            String jsonResult = redisUtils.get(LoginKey.loginIpSource, loginIp);
            if (StringUtils.isEmpty(jsonResult)) {
                // 如果Redis中没有，则调用IP地址查询服务
                String addresses = IpUtils.getAddresses("ip=" + loginIp);
                if (StringUtils.isNotEmpty(addresses)) {
                    onlineAdmin.setLoginLocation(addresses);
                    // 缓存IP地址信息，过期时间设置为24小时
                    redisUtils.setEx(LoginKey.loginIpSource, loginIp, addresses, expiration * 24, TimeUnit.SECONDS);
                }
            } else {
                onlineAdmin.setLoginLocation(jsonResult);
            }
        } catch (Exception e) {
            log.warn("获取IP地址信息失败，IP: {}, 错误: {}", loginIp, e.getMessage());
        }
    }
}