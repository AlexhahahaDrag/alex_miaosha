package com.alex.user.rbac.service.impl;

import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.rbac.service.PermissionContextCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * RBAC-BE-RELATION-002 / RBAC-BE-SCOPE-004: 见 {@link PermissionContextCacheService} 类注释。
 * 缓存键格式（锁定）：{@code LoginKey.loginKey} + "permission_context:" + userId，
 * 与 {@code UserPermissionContextServiceImpl} 写入时使用的键保持一致。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionContextCacheServiceImpl implements PermissionContextCacheService {

    private final RedisUtils redisUtils;

    @Override
    public void invalidate(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisUtils.delete(LoginKey.loginKey, "permission_context:" + userId);
        } catch (Exception e) {
            // 缓存清理失败不阻断主流程，仅记录日志兜底排查
            log.error("清理用户权限上下文缓存异常，userId: {}", userId, e);
        }
    }

    @Override
    public void invalidateAll(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            invalidate(userId);
        }
    }
}
