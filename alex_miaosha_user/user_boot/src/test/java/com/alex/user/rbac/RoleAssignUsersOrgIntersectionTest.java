package com.alex.user.rbac;

import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.permissionInfo.service.PermissionInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleInfo.entity.RoleInfo;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleInfo.service.impl.RoleInfoServiceImp;
import com.alex.user.roleOrgInfo.entity.RoleOrgInfo;
import com.alex.user.roleOrgInfo.service.RoleOrgInfoService;
import com.alex.user.rolePermissionInfo.service.RolePermissionInfoService;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
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
 * Task 5 / Spec §5.3: assignUsers requires user-org ∩ role-org non-empty.
 */
@ExtendWith(MockitoExtension.class)
public class RoleAssignUsersOrgIntersectionTest {

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
    void assignUsers_denied_whenUserOrgAndRoleOrgHaveNoIntersection() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleOrgInfoService.listValidByRoleId(100L))
                .thenReturn(Collections.singletonList(roleOrg("20")));
        when(orgUserInfoService.getOrgInfoList(55L))
                .thenReturn(Collections.singletonList(org(99L)));

        SystemException ex = assertThrows(SystemException.class,
                () -> service.assignUsers(100L, Collections.singletonList(55L)),
                "empty org intersection must reject assignUsers");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无交集"),
                "message must mention 无交集, actual=" + ex.getMsg());
        verify(roleUserInfoService, never()).assignUsersToRole(any(), any());
    }

    @Test
    void assignUsers_allowed_whenUserOrgIntersectsRoleOrg() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleOrgInfoService.listValidByRoleId(100L))
                .thenReturn(Arrays.asList(roleOrg("20"), roleOrg("21")));
        when(orgUserInfoService.getOrgInfoList(55L))
                .thenReturn(Collections.singletonList(org(21L)));
        when(roleUserInfoService.assignUsersToRole(eq(100L), anyList())).thenReturn(true);

        assertDoesNotThrow(() -> service.assignUsers(100L, Collections.singletonList(55L)));
        verify(roleUserInfoService).assignUsersToRole(100L, Collections.singletonList(55L));
    }

    @Test
    void assignUsers_denied_whenUserHasNoValidOrg() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(roleInfoMapper.queryRoleInfo("100")).thenReturn(visibleRole(100L));
        RoleInfo role = new RoleInfo();
        role.setRoleCode("ops");
        when(roleInfoMapper.selectById(100L)).thenReturn(role);
        when(roleOrgInfoService.listValidByRoleId(100L))
                .thenReturn(Collections.singletonList(roleOrg("20")));
        when(orgUserInfoService.getOrgInfoList(55L)).thenReturn(Collections.emptyList());

        SystemException ex = assertThrows(SystemException.class,
                () -> service.assignUsers(100L, Collections.singletonList(55L)));
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无交集"),
                "no valid user org is empty intersection, actual=" + ex.getMsg());
        verify(roleUserInfoService, never()).assignUsersToRole(any(), any());
    }

    @Test
    void assignUsers_skipsIntersection_whenUserIdsEmpty() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(roleUserInfoService.assignUsersToRole(eq(100L), anyList())).thenReturn(true);

        assertDoesNotThrow(() -> service.assignUsers(100L, Collections.emptyList()));
        verify(roleOrgInfoService, never()).listValidByRoleId(any());
        verify(roleUserInfoService).assignUsersToRole(100L, Collections.emptyList());
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
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

    private static RoleOrgInfo roleOrg(String orgId) {
        RoleOrgInfo row = new RoleOrgInfo();
        row.setOrgId(orgId);
        return row;
    }

    private static OrgInfoVo org(Long id) {
        OrgInfoVo vo = new OrgInfoVo();
        vo.setId(id);
        return vo;
    }
}
