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

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Task 3: assign-orgs scope guard + create role must bind orgs.
 */
@ExtendWith(MockitoExtension.class)
public class RoleOrgAssignApiGuardTest {

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
    void assignOrgs_denied_whenNonSuperBindsOutOfScopeOrg() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN, 20L));
        when(roleInfoMapper.queryRoleInfo("100")).thenReturn(visibleRole(100L));
        when(orgSubtreeLookup.findDescendantOrgIds(20L)).thenReturn(Collections.singletonList(21L));

        SystemException ex = assertThrows(SystemException.class,
                () -> service.assignOrgs(100L, Collections.singletonList(99L)),
                "non-super must not bind org outside caller scope S");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权绑定范围外机构"),
                "exception message must contain 无权绑定范围外机构, actual=" + ex.getMsg());
        verify(roleOrgInfoService, never()).assignOrgs(any(), anyList());
    }

    @Test
    void assignOrgs_allowed_whenAdminBindsSelfOrDescendant() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN, 20L));
        when(roleInfoMapper.queryRoleInfo("100")).thenReturn(visibleRole(100L));
        when(orgSubtreeLookup.findDescendantOrgIds(20L)).thenReturn(Collections.singletonList(21L));
        when(roleOrgInfoService.assignOrgs(eq(100L), anyList())).thenReturn(true);

        assertDoesNotThrow(() -> service.assignOrgs(100L, Arrays.asList(20L, 21L)));
        verify(roleOrgInfoService).assignOrgs(100L, Arrays.asList(20L, 21L));
    }

    @Test
    void addRoleInfo_denied_whenNoOrgIdsAndNoLoginOrg() {
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN, null));

        RoleInfoVo vo = new RoleInfoVo();
        vo.setRoleCode("NEW-ROLE");
        vo.setRoleName("new");

        SystemException ex = assertThrows(SystemException.class, () -> service.addRoleInfo(vo),
                "create without orgIds and without login org must fail");
        assertTrue(ex.getMsg() != null && (
                        ex.getMsg().contains("必须绑定机构") || ex.getMsg().contains("机构")),
                "exception message must mention org binding, actual=" + ex.getMsg());
        verify(roleInfoMapper, never()).insert(any(RoleInfo.class));
        verify(roleOrgInfoService, never()).assignOrgs(any(), anyList());
    }

    @Test
    void addRoleInfo_bindsDefaultLoginOrg_whenOrgIdsAbsent() {
        when(roleInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN, 20L));
        when(orgSubtreeLookup.findDescendantOrgIds(20L)).thenReturn(Collections.emptyList());
        when(roleInfoMapper.insert(any(RoleInfo.class))).thenAnswer(inv -> {
            RoleInfo entity = inv.getArgument(0);
            entity.setId(99L);
            return 1;
        });
        when(roleOrgInfoService.assignOrgs(eq(99L), anyList())).thenReturn(true);

        RoleInfoVo vo = new RoleInfoVo();
        vo.setRoleCode("NEW-ROLE");
        vo.setRoleName("new");

        assertDoesNotThrow(() -> service.addRoleInfo(vo));
        verify(roleOrgInfoService).assignOrgs(99L, Collections.singletonList(20L));
    }

    private static TUserVo loginUser(String roleCode, Long orgId) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        user.setOrgId(orgId);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static RoleInfoVo visibleRole(Long id) {
        RoleInfoVo vo = new RoleInfoVo();
        vo.setId(id);
        vo.setRoleCode("role-" + id);
        return vo;
    }
}
