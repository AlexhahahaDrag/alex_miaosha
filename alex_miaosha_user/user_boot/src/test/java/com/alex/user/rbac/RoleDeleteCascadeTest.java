package com.alex.user.rbac;

import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.constants.SysConf;
import com.alex.common.exception.SystemException;
import com.alex.user.permissionInfo.service.PermissionInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleInfo.service.impl.RoleInfoServiceImp;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.roleOrgInfo.entity.RoleOrgInfo;
import com.alex.user.roleOrgInfo.service.RoleOrgInfoService;
import com.alex.user.rolePermissionInfo.entity.RolePermissionInfo;
import com.alex.user.rolePermissionInfo.service.RolePermissionInfoService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-ROLE-002: role delete must cascade-invalidate role-permission rows
 * in the same transactional path (bound-user reject retained).
 */
@ExtendWith(MockitoExtension.class)
public class RoleDeleteCascadeTest {

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
    @Mock
    private RoleOrgInfoService roleOrgInfoService;
    @Mock
    private OrgUserInfoService orgUserInfoService;
    @Mock
    private OrgSubtreeLookup orgSubtreeLookup;

    private RoleInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new RoleInfoServiceImp(
                roleInfoMapper,
                permissionInfoService,
                rolePermissionInfoService,
                roleUserInfoService,
                permissionContextCacheService,
                userUtils,
                roleOrgInfoService,
                orgUserInfoService,
                orgSubtreeLookup
        );
    }

    @Test
    void deleteRoleInfo_invalidatesActiveRolePermissions_whenNoBoundUsers() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleUserInfoService.count(any(Wrapper.class))).thenReturn(0L);

        RolePermissionInfo active = rolePermission(1L, "200", "100", SysConf.VALID_STATUS);
        when(rolePermissionInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(active));
        when(rolePermissionInfoService.updateById(any(RolePermissionInfo.class))).thenReturn(true);
        when(roleOrgInfoService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(roleUserInfoService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(roleInfoMapper.deleteBatchIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteRoleInfo("200"));

        assertEquals(SysConf.INVALID_STATUS, active.getStatus(),
                "RBAC-BE-ROLE-002: active role-permission rows must be status-invalidated");
        verify(rolePermissionInfoService).updateById(active);
        verify(roleInfoMapper).deleteBatchIds(anyList());
    }

    @Test
    void deleteRoleInfo_invalidatesActiveRoleOrgs_whenNoBoundUsers() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleUserInfoService.count(any(Wrapper.class))).thenReturn(0L);
        when(rolePermissionInfoService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        RoleOrgInfo activeOrg = roleOrg(3L, "200", "20", SysConf.VALID_STATUS);
        when(roleOrgInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(activeOrg));
        when(roleOrgInfoService.updateById(any(RoleOrgInfo.class))).thenReturn(true);
        when(roleUserInfoService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(roleInfoMapper.deleteBatchIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteRoleInfo("200"));

        assertEquals(SysConf.INVALID_STATUS, activeOrg.getStatus(),
                "Task 5: active role-org rows must be status-invalidated on delete");
        verify(roleOrgInfoService).updateById(activeOrg);
        verify(roleInfoMapper).deleteBatchIds(anyList());
    }

    @Test
    void deleteRoleInfo_rejectsWhenBoundUsers_withoutCascade() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleUserInfoService.count(any(Wrapper.class))).thenReturn(1L);

        SystemException ex = assertThrows(SystemException.class, () -> service.deleteRoleInfo("200"),
                "bound active users must still block role delete");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("绑定"),
                "message should mention bound users, actual=" + ex.getMsg());
        verify(rolePermissionInfoService, never()).list(any(Wrapper.class));
        verify(rolePermissionInfoService, never()).updateById(any(RolePermissionInfo.class));
        verify(rolePermissionInfoService, never()).assignPermissions(any(), anyList());
        verify(roleInfoMapper, never()).deleteBatchIds(anyList());
    }

    @Test
    void deleteRoleInfo_invalidatesLeftoverRoleUsers_andClearsPermissionContext() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        // Guard passed (count==0) but dirty leftover valid row still present — harden path.
        when(roleUserInfoService.count(any(Wrapper.class))).thenReturn(0L);
        when(rolePermissionInfoService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(roleOrgInfoService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        RoleUserInfo leftover = roleUser(9L, "200", "55", SysConf.VALID_STATUS);
        when(roleUserInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(leftover));
        when(roleUserInfoService.updateById(any(RoleUserInfo.class))).thenReturn(true);
        when(roleInfoMapper.deleteBatchIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteRoleInfo("200"));

        assertEquals(SysConf.INVALID_STATUS, leftover.getStatus(),
                "leftover role_user rows should be status-invalidated");
        ArgumentCaptor<RoleUserInfo> captor = ArgumentCaptor.forClass(RoleUserInfo.class);
        verify(roleUserInfoService).updateById(captor.capture());
        assertEquals(SysConf.INVALID_STATUS, captor.getValue().getStatus());
        verify(permissionContextCacheService).invalidateAll(Collections.singleton(55L));
    }

    @Test
    void deleteRoleInfo_declaresTransactional() throws Exception {
        Transactional t = RoleInfoServiceImp.class
                .getMethod("deleteRoleInfo", String.class)
                .getAnnotation(Transactional.class);
        assertNotNull(t, "deleteRoleInfo must declare @Transactional for cascade atomicity");
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

    private static RoleOrgInfo roleOrg(Long id, String roleId, String orgId, String status) {
        RoleOrgInfo row = new RoleOrgInfo();
        row.setId(id);
        row.setRoleId(roleId);
        row.setOrgId(orgId);
        row.setStatus(status);
        return row;
    }
}
