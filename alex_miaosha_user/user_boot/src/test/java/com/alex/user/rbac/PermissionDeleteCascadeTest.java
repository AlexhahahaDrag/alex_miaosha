package com.alex.user.rbac;

import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.constants.SysConf;
import com.alex.user.permissionInfo.mapper.PermissionInfoMapper;
import com.alex.user.permissionInfo.service.impl.PermissionInfoServiceImp;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.rolePermissionInfo.entity.RolePermissionInfo;
import com.alex.user.rolePermissionInfo.service.RolePermissionInfoService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-PERM-002: permission delete must cascade-invalidate role-permission rows
 * and clear permission_context for users on affected roles.
 */
@ExtendWith(MockitoExtension.class)
public class PermissionDeleteCascadeTest {

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
    void deletePermissionInfo_invalidatesActiveRolePermissions() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));

        RolePermissionInfo active = rolePermission(1L, "200", "100", SysConf.VALID_STATUS);
        when(rolePermissionInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(active));
        when(rolePermissionInfoService.updateById(any(RolePermissionInfo.class))).thenReturn(true);
        when(roleUserInfoService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(permissionInfoMapper.deleteBatchIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> service.deletePermissionInfo("100"));

        assertEquals(SysConf.INVALID_STATUS, active.getStatus(),
                "RBAC-BE-PERM-002: active role-permission rows must be status-invalidated");
        verify(rolePermissionInfoService).updateById(active);
        verify(permissionInfoMapper).deleteBatchIds(anyList());
    }

    @Test
    void deletePermissionInfo_clearsPermissionContextForAffectedUsers() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));

        RolePermissionInfo active = rolePermission(1L, "200", "100", SysConf.VALID_STATUS);
        when(rolePermissionInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(active));
        when(rolePermissionInfoService.updateById(any(RolePermissionInfo.class))).thenReturn(true);

        RoleUserInfo boundUser = roleUser(9L, "200", "55", SysConf.VALID_STATUS);
        when(roleUserInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(boundUser));
        when(permissionInfoMapper.deleteBatchIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> service.deletePermissionInfo("100"));

        verify(permissionContextCacheService).invalidateAll(Collections.singleton(55L));
        verify(permissionInfoMapper).deleteBatchIds(anyList());
    }

    @Test
    void deletePermissionInfo_declaresTransactional() throws Exception {
        Transactional t = PermissionInfoServiceImp.class
                .getMethod("deletePermissionInfo", String.class)
                .getAnnotation(Transactional.class);
        assertNotNull(t, "deletePermissionInfo must declare @Transactional for cascade atomicity");
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static RolePermissionInfo rolePermission(Long id, String roleId, String permissionId, String status) {
        RolePermissionInfo row = new RolePermissionInfo();
        row.setId(id);
        row.setRoleId(roleId);
        row.setPermissionId(permissionId);
        row.setStatus(status);
        return row;
    }

    private static RoleUserInfo roleUser(Long id, String roleId, String userId, String status) {
        RoleUserInfo row = new RoleUserInfo();
        row.setId(id);
        row.setRoleId(roleId);
        row.setUserId(userId);
        row.setStatus(status);
        return row;
    }
}
