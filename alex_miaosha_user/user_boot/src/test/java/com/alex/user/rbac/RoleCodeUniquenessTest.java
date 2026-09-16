package com.alex.user.rbac;

import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.user.permissionInfo.service.PermissionInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.roleInfo.entity.RoleInfo;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleInfo.service.impl.RoleInfoServiceImp;
import com.alex.user.roleOrgInfo.service.RoleOrgInfoService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-ROLE-003: roleCode must be non-empty and globally unique (update excludes self).
 */
@ExtendWith(MockitoExtension.class)
public class RoleCodeUniquenessTest {

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
    void addRoleInfo_rejectsEmptyRoleCode_withoutInsert() {
        RoleInfoVo vo = new RoleInfoVo();
        vo.setRoleName("name-only");

        SystemException ex = assertThrows(SystemException.class, () -> service.addRoleInfo(vo),
                "RBAC-BE-ROLE-003: empty roleCode must be rejected on add");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("角色编码"),
                "message should mention 角色编码, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).insert(any(RoleInfo.class));
    }

    @Test
    void addRoleInfo_rejectsDuplicateRoleCode_withoutInsert() {
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        RoleInfoVo vo = new RoleInfoVo();
        vo.setRoleCode("DUP-ROLE");
        vo.setRoleName("dup name");

        SystemException ex = assertThrows(SystemException.class, () -> service.addRoleInfo(vo),
                "RBAC-BE-ROLE-003: duplicate roleCode must be rejected on add");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("角色编码"),
                "message should mention 角色编码, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).insert(any(RoleInfo.class));
    }

    @Test
    void addRoleInfo_allowsUniqueRoleCode() {
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleInfoMapper.insert(any(RoleInfo.class))).thenAnswer(inv -> {
            RoleInfo entity = inv.getArgument(0);
            entity.setId(99L);
            return 1;
        });
        when(roleOrgInfoService.assignOrgs(any(), any())).thenReturn(true);

        RoleInfoVo vo = new RoleInfoVo();
        vo.setRoleCode("NEW-ROLE");
        vo.setRoleName("new");
        vo.setOrgIds(Collections.singletonList("20"));

        assertDoesNotThrow(() -> service.addRoleInfo(vo));
        verify(roleInfoMapper).insert(any(RoleInfo.class));
        verify(roleOrgInfoService).assignOrgs(any(), any());
    }

    @Test
    void updateRoleInfo_rejectsDuplicateRoleCode_afterOwnership_withoutUpdate() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        RoleInfoVo vo = new RoleInfoVo();
        vo.setId(200L);
        vo.setRoleCode("TAKEN-CODE");

        SystemException ex = assertThrows(SystemException.class, () -> service.updateRoleInfo(vo),
                "RBAC-BE-ROLE-003: duplicate roleCode must be rejected on update");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("角色编码"),
                "message should mention 角色编码, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).updateById(any(RoleInfo.class));
    }

    @Test
    void updateRoleInfo_allowsSameRoleCodeForSelf() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(roleInfoMapper.updateById(any(RoleInfo.class))).thenReturn(1);

        RoleInfoVo vo = new RoleInfoVo();
        vo.setId(200L);
        vo.setRoleCode("KEEP-CODE");

        assertDoesNotThrow(() -> service.updateRoleInfo(vo));
        verify(roleInfoMapper).updateById(any(RoleInfo.class));
    }

    @Test
    void updateRoleInfo_checksUniquenessAfterOwnership() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(roleInfoMapper.queryRoleInfo("200")).thenReturn(null);

        RoleInfoVo vo = new RoleInfoVo();
        vo.setId(200L);
        vo.setRoleCode("ANY");

        assertThrows(SystemException.class, () -> service.updateRoleInfo(vo));
        verify(roleInfoMapper, never()).selectCount(any(Wrapper.class));
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
}
