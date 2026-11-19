package com.alex.user.token.service;

import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 刷新服务
 *
 * @author alex
 * createDate 2024/12/19
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshService {

    private final RedisUtils redisUtils;
    private final JwtTokenUtils jwtTokenUtils;
    private final Audience audience;

    /**
     * 异步刷新 token
     *
     * @param token JWT token
     * @param base64Secret 密钥
     * @param uuidToken UUID token
     * @param barToken 完整 token
     */
    @Async("tokenRefreshExecutor")
    public void refreshTokenAsync(String token, String base64Secret, String uuidToken, String barToken) {
        try {
            log.debug("开始异步刷新token，uuidToken: {}", uuidToken);

            // 获取 token过期时间
            Date expirationDate = jwtTokenUtils.getExpiration(token, base64Secret);

            // 计算剩余存活时间（秒）
            long survivalSecond = calculateSurvivalTime(expirationDate);

            log.debug("Token剩余存活时间: {} 秒，刷新阈值: {} 秒", survivalSecond, audience.getRefreshSecond());

            // 当存活时间小于刷新阈值时，生成新token
            if (survivalSecond < audience.getRefreshSecond()) {
                Boolean locked = redisUtils.setIfAbsent(LoginKey.loginRefreshLock, uuidToken, "1", 15, TimeUnit.SECONDS);
                if (Boolean.FALSE.equals(locked)) {
                    log.debug("Token刷新任务已存在或在防并发窗口内，跳过重复刷新，uuidToken: {}", uuidToken);
                    return;
                }
                refreshTokenInternal(token, base64Secret, uuidToken, barToken);
            } else {
                log.debug("Token无需刷新，剩余时间充足");
            }

        } catch (Exception e) {
            log.error("异步刷新token失败，uuidToken: {}", uuidToken, e);
        }
    }

    /**
     * 计算 token剩余存活时间
     *
     * @param expirationDate 过期时间
     * @return 剩余秒数
     */
    private long calculateSurvivalTime(Date expirationDate) {
        if (expirationDate == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long expirationTime = expirationDate.getTime();

        return Math.max(0, (expirationTime - currentTime) / 1000);
    }

    /**
     * 解析续期时应使用的有效期（秒）：
     * 若旧 JWT 签发总时长超过普通 expiresSecond（说明为「记住我」长效会话），
     * 续期时保留 isRememberMeExpiresSecond，防止长效登录态被降级为短效。
     */
    private long resolveRefreshExpiresSecond(String token, String base64Secret) {
        long baseExpiresSecond = audience.getExpiresSecond();
        Long rememberExpiresSecond = audience.getIsRememberMeExpiresSecond();
        if (rememberExpiresSecond == null || rememberExpiresSecond <= baseExpiresSecond) {
            return baseExpiresSecond;
        }
        Claims claims = jwtTokenUtils.parseJwt(token, base64Secret);
        if (claims == null || claims.getExpiration() == null) {
            return baseExpiresSecond;
        }
        Object createTimeObj = claims.get(SysConf.CREATE_TIME);
        Long createdAtMillis = null;
        if (createTimeObj instanceof Number) {
            createdAtMillis = ((Number) createTimeObj).longValue();
        } else if (createTimeObj instanceof Date) {
            createdAtMillis = ((Date) createTimeObj).getTime();
        }
        if (createdAtMillis == null) {
            return baseExpiresSecond;
        }
        long issuedTtlSeconds = (claims.getExpiration().getTime() - createdAtMillis) / 1000L;
        return issuedTtlSeconds > baseExpiresSecond ? rememberExpiresSecond : baseExpiresSecond;
    }

    /**
     * @description: 内部刷新 token方法
     *
     * @param token JWT token
     * @param base64Secret 密钥
     * @param uuidToken UUID token
     * @param barToken 完整 token
     */
    private void refreshTokenInternal(String token, String base64Secret, String uuidToken, String barToken) {
        try {
            log.info("开始刷新token，uuidToken: {}", uuidToken);

            long refreshExpiresSecond = resolveRefreshExpiresSecond(token, base64Secret);

            // 生成新 token
            String newToken = audience.getTokenHead() + jwtTokenUtils.refreshToken(token, base64Secret, refreshExpiresSecond * 1000L);

            // 更新 Redis中的token映射
            redisUtils.setEx(LoginKey.loginUuid, uuidToken, newToken, refreshExpiresSecond, TimeUnit.SECONDS);

            // 获取并更新用户信息
            String onlineAdminStr = redisUtils.get(LoginKey.loginToken, barToken);
            if (StringUtils.isNotBlank(onlineAdminStr)) {
                TUserVo onlineAdmin = JSONObject.parseObject(onlineAdminStr, TUserVo.class);

                // 更新 Redis中的用户信息
                redisUtils.setEx(LoginKey.loginToken, newToken, JSONObject.toJSONString(onlineAdmin, SerializerFeature.DisableCircularReferenceDetect), refreshExpiresSecond, TimeUnit.SECONDS);

                // 旧 token 保留 120 秒平滑过渡期，避免并发请求查空被误杀
                redisUtils.setEx(LoginKey.loginToken, barToken, onlineAdminStr, 120, TimeUnit.SECONDS);

                log.info("Token刷新成功，uuidToken: {}, 新token: {}, 续期时长: {} 秒", uuidToken, newToken, refreshExpiresSecond);
            } else {
                log.warn("未找到用户信息，无法更新token，uuidToken: {}", uuidToken);
            }

        } catch (Exception e) {
            log.error("刷新token失败，uuidToken: {}", uuidToken, e);
        }
    }
}