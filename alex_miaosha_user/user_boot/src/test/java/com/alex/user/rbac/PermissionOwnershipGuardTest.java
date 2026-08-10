package com.alex.user.rbac;

import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.user.permissionInfo.entity.PermissionInfo;
import com.alex.user.permissionInfo.mapper.PermissionInfoMapper;
import com.alex.user.permissionInfo.service.impl.PermissionInfoServiceImp;
import com.alex.user.rbac.service.PermissionContextCacheService;
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
 * RBAC-BE-PERM-001: write-path ownership guard must go through scoped queryPermissionInfo.
 */
@ExtendWith(MockitoExtension.class)
public class PermissionOwnershipGuardTest {

    @Mock
    private PermissionInfoMapper permissionInfoMapper;
    @Mock
    private UserUtils userUtils;
    @Mock
    private RolePermissionInfoService rolePermissionInfoService;
    @Mock
    private RoleUserInfoService roleUserInfoService;
    @Mock
    private PermissionContextCacheService permissionContextCacheService;

    private PermissionInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new PermissionInfoServiceImp(
                permissionInfoMapper,
                userUtils,
                rolePermissionInfoService,
                roleUserInfoService,
                permissionContextCacheService
        );
    }

    @Test
    void updatePermissionInfo_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(permissionInfoMapper.queryPermissionInfo(200L)).thenReturn(null);

        PermissionInfoVo vo = targetPermission(200L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updatePermissionInfo(vo),
                "RBAC-BE-PERM-001: update must reject permissions outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).updateById(any(PermissionInfo.class));
    }

    @Test
    void updatePermissionInfo_allowed_whenScopedQueryReturnsPermission() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        PermissionInfoVo visible = targetPermission(200L);
        when(permissionInfoMapper.queryPermissionInfo(200L)).thenReturn(visible);
        when(permissionInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(permissionInfoMapper.updateById(any(PermissionInfo.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updatePermissionInfo(targetPermission(200L)));
        verify(permissionInfoMapper).updateById(any(PermissionInfo.class));
    }

    @Test
    void deletePermissionInfo_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(permissionInfoMapper.queryPermissionInfo(200L)).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class, () -> service.deletePermissionInfo("200"),
                "RBAC-BE-PERM-001: delete must reject permissions outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).deleteBatchIds(any());
    }

    @Test
    void updatePermissionInfo_superAdmin_bypassesScopedQuery() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(permissionInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(permissionInfoMapper.updateById(any(PermissionInfo.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updatePermissionInfo(targetPermission(200L)));
        verify(permissionInfoMapper, never()).queryPermissionInfo(any());
        verify(permissionInfoMapper).updateById(any(PermissionInfo.class));
    }

    @Test
    void queryPermissionInfo_returnsNull_whenOutOfScope() {
        when(permissionInfoMapper.queryPermissionInfo(200L)).thenReturn(null);

        PermissionInfoVo result = assertDoesNotThrow(() -> service.queryPermissionInfo(200L),
                "RBAC-BE-PERM-001: null mapper result must not NPE");
        assertNull(result, "out-of-scope permission query must return null, not throw");
    }

    @Test
    void updatePermissionInfo_deniedWhenLoginUserNull() {
        when(userUtils.getLoginUser()).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class,
                () -> service.updatePermissionInfo(targetPermission(200L)),
                "null login context must fail-closed");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).updateById(any(PermissionInfo.class));
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static PermissionInfoVo targetPermission(Long id) {
        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setId(id);
        vo.setPermissionCode("perm-" + id);
        return vo;
    }
}
