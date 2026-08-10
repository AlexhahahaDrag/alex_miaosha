package com.alex.user.rbac;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.user.online.service.OnlineUserService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.rbac.service.UserDeleteCleanupService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alex.user.token.service.TokenRefreshService;
import com.alex.user.user.entity.TUser;
import com.alex.user.user.mapper.TUserMapper;
import com.alex.user.user.service.impl.TUserServiceImpl;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-USER-002: write-path ownership guard must go through scoped queryTUser.
 */
@ExtendWith(MockitoExtension.class)
public class UserOwnershipGuardTest {

    @Mock
    private TUserMapper tUserMapper;
    @Mock
    private RedisUtils redisUtils;
    @Mock
    private JwtTokenUtils jwtTokenUtils;
    @Mock
    private Audience audience;
    @Mock
    private OssApi ossApi;
    @Mock
    private MenuInfoService menuInfoService;
    @Mock
    private UserUtils userUtils;
    @Mock
    private OrgUserInfoService orgUserInfoService;
    @Mock
    private RoleUserInfoService roleUserInfoService;
    @Mock
    private RoleInfoMapper roleInfoMapper;
    @Mock
    private TokenRefreshService tokenRefreshService;
    @Mock
    private OnlineUserService onlineUserService;
    @Mock
    private Executor asyncTaskExecutor;
    @Mock
    private UserPermissionContextService userPermissionContextService;
    @Mock
    private UserDeleteCleanupService userDeleteCleanupService;
    @Mock
    private PermissionContextCacheService permissionContextCacheService;

    private TUserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TUserServiceImpl(
                tUserMapper,
                redisUtils,
                jwtTokenUtils,
                audience,
                ossApi,
                menuInfoService,
                userUtils,
                orgUserInfoService,
                roleUserInfoService,
                roleInfoMapper,
                tokenRefreshService,
                onlineUserService,
                asyncTaskExecutor,
                userPermissionContextService,
                userDeleteCleanupService,
                permissionContextCacheService
        );
    }

    @Test
    void updateTUser_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(tUserMapper.queryTUser("200")).thenReturn(null);

        TUserVo vo = targetUser(200L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updateTUser(vo),
                "RBAC-BE-USER-002: update must reject users outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(tUserMapper, never()).updateById(any(TUser.class));
    }

    @Test
    void updateTUser_allowed_whenScopedQueryReturnsUser() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        TUserVo visible = targetUser(200L);
        when(tUserMapper.queryTUser("200")).thenReturn(visible);
        when(tUserMapper.updateById(any(TUser.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updateTUser(targetUser(200L)));
        verify(tUserMapper).updateById(any(TUser.class));
    }

    @Test
    void deleteTUser_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(tUserMapper.queryTUser("200")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class, () -> service.deleteTUser("200"),
                "RBAC-BE-USER-002: delete must reject users outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(tUserMapper, never()).deleteBatchIds(any());
    }

    @Test
    void updateTUser_deniesRoleSync_whenRoleIdOutOfScope() {
        // C2: syncUserRbacAssignments 必须校验 roleIds 归属，越权角色 id 直接拒绝
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        TUserVo visibleUser = targetUser(200L);
        when(tUserMapper.queryTUser("200")).thenReturn(visibleUser);
        when(tUserMapper.updateById(any(TUser.class))).thenReturn(1);
        when(roleInfoMapper.queryRoleInfo("999")).thenReturn(null);

        TUserVo vo = targetUser(200L);
        vo.setRoleIds(Collections.singletonList(999L));

        SystemException ex = assertThrows(SystemException.class, () -> service.updateTUser(vo),
                "C2: syncing roleIds must reject roles outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleUserInfoService, never()).assignRoles(any(), any());
    }

    @Test
    void updateTUser_deniesRoleSync_whenGrantingSuperRole() {
        // C2: 非超管一律不得授予 super_super，即使该角色恰好在其可见范围内
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        TUserVo visibleUser = targetUser(200L);
        when(tUserMapper.queryTUser("200")).thenReturn(visibleUser);
        when(tUserMapper.updateById(any(TUser.class))).thenReturn(1);
        RoleInfoVo superRole = new RoleInfoVo();
        superRole.setId(5L);
        superRole.setRoleCode(RbacRoleCodes.SUPER);
        when(roleInfoMapper.queryRoleInfo("5")).thenReturn(superRole);

        TUserVo vo = targetUser(200L);
        vo.setRoleIds(Collections.singletonList(5L));

        SystemException ex = assertThrows(SystemException.class, () -> service.updateTUser(vo),
                "C2: non-super must never be able to grant super_super role");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleUserInfoService, never()).assignRoles(any(), any());
    }

    @Test
    void updateTUser_deniedWhenLoginUserNull() {
        // I1: 登录上下文不可用时必须 fail-closed，不能默认放行
        when(userUtils.getLoginUser()).thenReturn(null);

        TUserVo vo = targetUser(200L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updateTUser(vo),
                "I1: null login context must fail-closed");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(tUserMapper, never()).updateById(any(TUser.class));
    }

    @Test
    void updateTUser_superAdmin_bypassesScopedQuery() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(tUserMapper.updateById(any(TUser.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updateTUser(targetUser(200L)));
        verify(tUserMapper, never()).queryTUser(any());
        verify(tUserMapper).updateById(any(TUser.class));
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static TUserVo targetUser(Long id) {
        TUserVo vo = new TUserVo();
        vo.setId(id);
        return vo;
    }
}
