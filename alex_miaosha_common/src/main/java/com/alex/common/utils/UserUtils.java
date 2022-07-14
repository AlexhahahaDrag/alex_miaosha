package com.alex.common.utils;

import cn.hutool.http.server.HttpServerRequest;
import com.alex.common.redis.key.UserKey;
import com.alex.common.redis.manager.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtils {

    private final RedisService redisService;

    public Long getUserId(HttpServerRequest request) {
        String authInfo = request.getHeader("Authorization");
        String loginToken = authInfo.split("Bearer ")[1];
        return redisService.get(UserKey.getById, loginToken, Long.class);
    }
}
