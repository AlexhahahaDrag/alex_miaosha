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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-PERM-003: permissionCode must be non-empty and globally unique (update excludes self).
 */
@ExtendWith(MockitoExtension.class)
public class PermissionCodeUniquenessTest {

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
    void addPermissionInfo_rejectsEmptyPermissionCode_withoutInsert() {
        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setPermissionName("name-only");

        SystemException ex = assertThrows(SystemException.class, () -> service.addPermissionInfo(vo),
                "RBAC-BE-PERM-003: empty permissionCode must be rejected on add");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("权限编码"),
                "message should mention 权限编码, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).insert(any(PermissionInfo.class));
    }

    @Test
    void addPermissionInfo_rejectsDuplicatePermissionCode_withoutInsert() {
        when(permissionInfoMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setPermissionCode("DUP-PERM");
        vo.setPermissionName("dup name");

        SystemException ex = assertThrows(SystemException.class, () -> service.addPermissionInfo(vo),
                "RBAC-BE-PERM-003: duplicate permissionCode must be rejected on add");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("权限编码"),
                "message should mention 权限编码, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).insert(any(PermissionInfo.class));
    }

    @Test
    void addPermissionInfo_allowsUniquePermissionCode() {
        when(permissionInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(permissionInfoMapper.insert(any(PermissionInfo.class))).thenAnswer(inv -> {
            PermissionInfo entity = inv.getArgument(0);
            entity.setId(99L);
            return 1;
        });

        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setPermissionCode("NEW-PERM");
        vo.setPermissionName("new");

        assertDoesNotThrow(() -> service.addPermissionInfo(vo));
        verify(permissionInfoMapper).insert(any(PermissionInfo.class));
    }

    @Test
    void updatePermissionInfo_rejectsDuplicatePermissionCode_afterOwnership_withoutUpdate() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(permissionInfoMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setId(200L);
        vo.setPermissionCode("TAKEN-CODE");

        SystemException ex = assertThrows(SystemException.class, () -> service.updatePermissionInfo(vo),
                "RBAC-BE-PERM-003: duplicate permissionCode must be rejected on update");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("权限编码"),
                "message should mention 权限编码, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).updateById(any(PermissionInfo.class));
    }

    @Test
    void updatePermissionInfo_allowsSamePermissionCodeForSelf() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(permissionInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(permissionInfoMapper.updateById(any(PermissionInfo.class))).thenReturn(1);

        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setId(200L);
        vo.setPermissionCode("KEEP-CODE");

        assertDoesNotThrow(() -> service.updatePermissionInfo(vo));
        verify(permissionInfoMapper).updateById(any(PermissionInfo.class));
    }

    @Test
    void updatePermissionInfo_checksUniquenessAfterOwnership() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(permissionInfoMapper.queryPermissionInfo(200L)).thenReturn(null);

        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setId(200L);
        vo.setPermissionCode("ANY");

        assertThrows(SystemException.class, () -> service.updatePermissionInfo(vo));
        verify(permissionInfoMapper, never()).selectCount(any(Wrapper.class));
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
}
