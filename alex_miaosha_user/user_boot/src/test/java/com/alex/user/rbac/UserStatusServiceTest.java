package com.alex.user.rbac;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.constants.SysConf;
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
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-USER-003: dedicated user status endpoint must only touch status,
 * go through assertUserAccessible, and invalidate permission_context on success.
 */
@ExtendWith(MockitoExtension.class)
public class UserStatusServiceTest {

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
    void updateUserStatus_updatesOnlyStatusField() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(tUserMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updateUserStatus(200L, SysConf.INVALID_STATUS));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<TUser>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(tUserMapper).update(isNull(), wrapperCaptor.capture());
        verify(tUserMapper, never()).updateById(any(TUser.class));

        String sqlSet = wrapperCaptor.getValue().getSqlSet();
        assertTrue(sqlSet != null && sqlSet.toLowerCase().contains("status"),
                "sqlSet must contain status, actual=" + sqlSet);
        // 仅 status 变更：其它业务列不得出现在 SET 子句
        assertFalse(sqlSet.toLowerCase().contains("username"), "must not set username");
        assertFalse(sqlSet.toLowerCase().contains("password"), "must not set password");
        assertFalse(sqlSet.toLowerCase().contains("email"), "must not set email");
        assertFalse(sqlSet.toLowerCase().contains("mobile"), "must not set mobile");
        assertFalse(sqlSet.toLowerCase().contains("nick_name"), "must not set nick_name");

        verify(permissionContextCacheService).invalidate(200L);
    }

    @Test
    void updateUserStatus_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(tUserMapper.queryTUser("200")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class,
                () -> service.updateUserStatus(200L, SysConf.VALID_STATUS),
                "RBAC-BE-USER-003: status update must reject users outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(tUserMapper, never()).update(isNull(), any(Wrapper.class));
        verify(permissionContextCacheService, never()).invalidate(any());
    }

    @Test
    void updateUserStatus_rejectsInvalidStatus() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));

        SystemException ex = assertThrows(SystemException.class,
                () -> service.updateUserStatus(200L, "2"),
                "status must be 1 or 0");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("状态"),
                "exception message must mention 状态, actual=" + ex.getMsg());
        verify(tUserMapper, never()).update(isNull(), any(Wrapper.class));
        verify(permissionContextCacheService, never()).invalidate(any());
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }
}
