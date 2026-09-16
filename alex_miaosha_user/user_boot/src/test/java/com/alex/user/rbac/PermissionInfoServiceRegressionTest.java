package com.alex.user.rbac;

import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.constants.SysConf;
import com.alex.common.exception.SystemException;
import com.alex.user.permissionInfo.entity.PermissionInfo;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-PERM-004: 权限点服务层回归补强——在一个测试类中组合覆盖
 * 「编码唯一性拒绝」「归属越权拒绝」「删除级联失效」三条关键路径，
 * 确保 Surefire 报告明确统计到 permission 服务的这些核心分支，
 * 防止后续重构悄悄削弱其中任意一条防线而无测试感知。
 *
 * 各分支的完整用例矩阵仍由 {@link PermissionCodeUniquenessTest}、
 * {@link PermissionOwnershipGuardTest}、{@link PermissionDeleteCascadeTest} 分别维护，
 * 本类只做"至少各 1 条"的组合回归锁定，不替代上述专项测试。
 */
@ExtendWith(MockitoExtension.class)
public class PermissionInfoServiceRegressionTest {

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
    void regression_addPermissionInfo_rejectsDuplicatePermissionCode() {
        when(permissionInfoMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setPermissionCode("DUP-PERM");
        vo.setPermissionName("dup name");

        SystemException ex = assertThrows(SystemException.class, () -> service.addPermissionInfo(vo),
                "RBAC-BE-PERM-004 regression: uniqueness guard must keep rejecting duplicate permissionCode");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("权限编码"),
                "message should mention 权限编码, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).insert(any(PermissionInfo.class));
    }

    @Test
    void regression_updatePermissionInfo_deniedWhenOutOfOwnershipScope() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(permissionInfoMapper.queryPermissionInfo(200L)).thenReturn(null);

        PermissionInfoVo vo = new PermissionInfoVo();
        vo.setId(200L);
        vo.setPermissionCode("perm-200");

        SystemException ex = assertThrows(SystemException.class, () -> service.updatePermissionInfo(vo),
                "RBAC-BE-PERM-004 regression: ownership guard must keep rejecting out-of-scope updates");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(permissionInfoMapper, never()).updateById(any(PermissionInfo.class));
    }

    @Test
    void regression_deletePermissionInfo_cascadesRolePermissionInvalidationAndCacheClear() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));

        RolePermissionInfo activeRolePermission = rolePermission(1L, "200", "100", SysConf.VALID_STATUS);
        when(rolePermissionInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(activeRolePermission));
        when(rolePermissionInfoService.updateById(any(RolePermissionInfo.class))).thenReturn(true);

        RoleUserInfo boundUser = roleUser(9L, "200", "55", SysConf.VALID_STATUS);
        when(roleUserInfoService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(boundUser));
        when(permissionInfoMapper.deleteBatchIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> service.deletePermissionInfo("100"),
                "RBAC-BE-PERM-004 regression: delete-cascade path must keep working end to end");

        assertEquals(SysConf.INVALID_STATUS, activeRolePermission.getStatus(),
                "delete cascade must status-invalidate active role-permission rows");
        verify(rolePermissionInfoService).updateById(activeRolePermission);
        verify(permissionContextCacheService).invalidateAll(Collections.singleton(55L));
        verify(permissionInfoMapper).deleteBatchIds(anyList());
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
