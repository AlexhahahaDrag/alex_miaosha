package com.alex.user.rbac.service.impl;

import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.UserDeleteCleanupService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alex.user.tUserLogin.entity.TUserLogin;
import com.alex.user.tUserLogin.service.TUserLoginService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Invalidates org/role bindings and clears login Redis keys after user soft-delete.
 * Redis failures are logged and swallowed so DB cleanup is not rolled back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeleteCleanupServiceImpl implements UserDeleteCleanupService {

    private final OrgUserInfoService orgUserInfoService;
    private final RoleUserInfoService roleUserInfoService;
    private final TUserLoginService tUserLoginService;
    private final RedisUtils redisUtils;

    @Override
    public void cleanupAfterUserDeleted(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return;
        }

        invalidateOrgAssignments(userId);
        invalidateRoleAssignments(userId);
        clearPermissionContext(userId);
        kickSessions(userId);
    }

    private void invalidateOrgAssignments(String userId) {
        List<OrgUserInfo> orgs = orgUserInfoService.list(Wrappers.<OrgUserInfo>lambdaQuery()
                .eq(OrgUserInfo::getUserId, userId)
                .eq(OrgUserInfo::getStatus, SysConf.VALID_STATUS));
        for (OrgUserInfo row : orgs) {
            row.setStatus(SysConf.INVALID_STATUS);
            orgUserInfoService.updateById(row);
        }
    }

    private void invalidateRoleAssignments(String userId) {
        List<RoleUserInfo> roles = roleUserInfoService.list(Wrappers.<RoleUserInfo>lambdaQuery()
                .eq(RoleUserInfo::getUserId, userId)
                .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
        for (RoleUserInfo row : roles) {
            row.setStatus(SysConf.INVALID_STATUS);
            roleUserInfoService.updateById(row);
        }
    }

    private void clearPermissionContext(String userId) {
        try {
            redisUtils.delete(LoginKey.loginKey, "permission_context:" + userId);
        } catch (Exception e) {
            log.error("清理用户权限上下文缓存异常，userId: {}", userId, e);
        }
    }

    /**
     * Kick all known login sessions for the user.
     * Org/role tables use String userId; TUserLogin.userId is Long — convert carefully.
     */
    private void kickSessions(String userId) {
        Long userIdLong;
        try {
            userIdLong = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            log.error("踢出会话跳过：userId 无法解析为 Long，userId: {}", userId, e);
            return;
        }

        List<TUserLogin> logins = tUserLoginService.list(Wrappers.<TUserLogin>lambdaQuery()
                .eq(TUserLogin::getUserId, userIdLong));
        for (TUserLogin login : logins) {
            try {
                if (StringUtils.isNotEmpty(login.getTokenId())) {
                    String barToken = redisUtils.get(LoginKey.loginUuid, login.getTokenId());
                    redisUtils.delete(LoginKey.loginUuid, login.getTokenId());
                    if (StringUtils.isNotEmpty(barToken)) {
                        redisUtils.delete(LoginKey.loginToken, barToken);
                    } else if (StringUtils.isNotEmpty(login.getToken())) {
                        redisUtils.delete(LoginKey.loginToken, login.getToken());
                    }
                    redisUtils.delete(LoginKey.loginOnlineUser, login.getTokenId());
                }
            } catch (Exception e) {
                log.error("踢出用户会话异常，userId: {}, tokenId: {}", userId, login.getTokenId(), e);
            }
        }
    }
}
