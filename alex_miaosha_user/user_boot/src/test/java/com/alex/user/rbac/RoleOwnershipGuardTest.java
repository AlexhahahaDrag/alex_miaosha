package com.alex.user.rbac;

import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.user.permissionInfo.service.PermissionInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleInfo.entity.RoleInfo;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleInfo.service.impl.RoleInfoServiceImp;
import com.alex.user.rolePermissionInfo.service.RolePermissionInfoService;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-ROLE-001: write-path ownership guard must go through scoped queryRoleInfo.
 */
@ExtendWith(MockitoExtension.class)
public class RoleOwnershipGuardTest {

    @Mock
    private RoleInfoMapper roleInfoMapper;
    @Mock
    private PermissionInfoService permissionInfoService;
    @Mock
    private RolePermissionInfoService rolePermissionInfoService;
    @Mock
    private RoleUserInfoService roleUserInfoService;
    @Mock
    private PermissionContextCacheService permissionContextCacheService;
    @Mock
    private UserUtils userUtils;

    private RoleInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new RoleInfoServiceImp(
                roleInfoMapper,
                permissionInfoService,
                rolePermissionInfoService,
                roleUserInfoService,
                permissionContextCacheService,
                userUtils
        );
    }

    @Test
    void updateRoleInfo_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(roleInfoMapper.queryRoleInfo("200")).thenReturn(null);

        RoleInfoVo vo = targetRole(200L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updateRoleInfo(vo),
                "RBAC-BE-ROLE-001: update must reject roles outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).updateById(any(RoleInfo.class));
    }

    @Test
    void updateRoleInfo_allowed_whenScopedQueryReturnsRole() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        RoleInfoVo visible = targetRole(200L);
        when(roleInfoMapper.queryRoleInfo("200")).thenReturn(visible);
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(roleInfoMapper.updateById(any(RoleInfo.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updateRoleInfo(targetRole(200L)));
        verify(roleInfoMapper).updateById(any(RoleInfo.class));
    }

    @Test
    void deleteRoleInfo_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(roleInfoMapper.queryRoleInfo("200")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class, () -> service.deleteRoleInfo("200"),
                "RBAC-BE-ROLE-001: delete must reject roles outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).deleteBatchIds(any());
    }

    @Test
    void updateRoleInfo_superAdmin_bypassesScopedQuery() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(roleInfoMapper.updateById(any(RoleInfo.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updateRoleInfo(targetRole(200L)));
        verify(roleInfoMapper, never()).queryRoleInfo(any());
        verify(roleInfoMapper).updateById(any(RoleInfo.class));
    }

    @Test
    void queryRoleInfo_returnsNull_whenOutOfScope() {
        // C1: 越权/不存在的角色 id 必须安全返回 null，不能 NPE（本批次引入的 500 回归）
        when(roleInfoMapper.queryRoleInfo("200")).thenReturn(null);

        RoleInfoVo result = assertDoesNotThrow(() -> service.queryRoleInfo("200"),
                "RBAC-BE-ROLE-001/C1: null mapper result must not NPE");
        assertNull(result, "out-of-scope role query must return null, not throw");
    }

    @Test
    void assignUsers_denied_whenScopedQueryReturnsNull() {
        // C2: assignUsers 之前完全无归属校验，机构管理员可对任意 roleId 授予用户
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(roleInfoMapper.queryRoleInfo("200")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class,
                () -> service.assignUsers(200L, Collections.singletonList(1L)),
                "C2: assignUsers must reject roles outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleUserInfoService, never()).assignUsersToRole(any(), any());
    }

    @Test
    void assignUsers_denied_whenGrantingSuperRole() {
        // C2: 非超管一律不得授予 super_super，即使该角色行恰好在其可见范围内
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        RoleInfoVo visible = targetRole(5L);
        when(roleInfoMapper.queryRoleInfo("5")).thenReturn(visible);
        RoleInfo superRole = new RoleInfo();
        superRole.setRoleCode(RbacRoleCodes.SUPER);
        when(roleInfoMapper.selectById(5L)).thenReturn(superRole);

        SystemException ex = assertThrows(SystemException.class,
                () -> service.assignUsers(5L, Collections.singletonList(1L)),
                "C2: non-super must never be able to grant super_super role");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleUserInfoService, never()).assignUsersToRole(any(), any());
    }

    @Test
    void assignPermissions_denied_whenScopedQueryReturnsNull() {
        // C2: assignPermissions 用 BaseMapper#selectById（不受 @DataPermission 约束），
        // 必须先经 assertRoleAccessible 拦截越权角色
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(roleInfoMapper.queryRoleInfo("200")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class,
                () -> service.assignPermissions(200L, Collections.singletonList(1L)),
                "C2: assignPermissions must reject roles outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).selectById(any());
    }

    @Test
    void updateRoleInfo_deniedWhenLoginUserNull() {
        // I1: 登录上下文不可用时必须 fail-closed，不能默认放行
        when(userUtils.getLoginUser()).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class,
                () -> service.updateRoleInfo(targetRole(200L)),
                "I1: null login context must fail-closed");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).updateById(any(RoleInfo.class));
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static RoleInfoVo targetRole(Long id) {
        RoleInfoVo vo = new RoleInfoVo();
        vo.setId(id);
        vo.setRoleCode("role-" + id);
        return vo;
    }
}
