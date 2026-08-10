package com.alex.user.rbac;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.user.orgInfo.entity.OrgInfo;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import com.alex.user.orgInfo.service.impl.OrgInfoServiceImp;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
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
 * RBAC-BE-ORG-001: write-path ownership guard must go through scoped queryOrgInfo.
 */
@ExtendWith(MockitoExtension.class)
public class OrgOwnershipGuardTest {

    @Mock
    private OrgInfoMapper orgInfoMapper;
    @Mock
    private OrgUserInfoService orgUserInfoService;
    @Mock
    private UserUtils userUtils;

    private OrgInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new OrgInfoServiceImp(orgInfoMapper, orgUserInfoService, userUtils);
    }

    @Test
    void updateOrgInfo_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(orgInfoMapper.queryOrgInfo("200")).thenReturn(null);

        OrgInfoVo vo = targetOrg(200L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updateOrgInfo(vo),
                "RBAC-BE-ORG-001: update must reject orgs outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(orgInfoMapper, never()).updateById(any(OrgInfo.class));
    }

    @Test
    void updateOrgInfo_allowed_whenScopedQueryReturnsOrg() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        OrgInfoVo visible = targetOrg(200L);
        when(orgInfoMapper.queryOrgInfo("200")).thenReturn(visible);
        when(orgInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(orgInfoMapper.updateById(any(OrgInfo.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updateOrgInfo(targetOrg(200L)));
        verify(orgInfoMapper).updateById(any(OrgInfo.class));
    }

    @Test
    void deleteOrgInfo_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(orgInfoMapper.queryOrgInfo("200")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class, () -> service.deleteOrgInfo("200"),
                "RBAC-BE-ORG-001: delete must reject orgs outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(orgInfoMapper, never()).deleteBatchIds(any());
    }

    @Test
    void updateOrgInfo_deniedWhenLoginUserNull() {
        // I1: 登录上下文不可用时必须 fail-closed，不能默认放行
        when(userUtils.getLoginUser()).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class, () -> service.updateOrgInfo(targetOrg(200L)),
                "I1: null login context must fail-closed");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(orgInfoMapper, never()).updateById(any(OrgInfo.class));
    }

    @Test
    void updateOrgInfo_superAdmin_bypassesScopedQuery() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(orgInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(orgInfoMapper.updateById(any(OrgInfo.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.updateOrgInfo(targetOrg(200L)));
        verify(orgInfoMapper, never()).queryOrgInfo(any());
        verify(orgInfoMapper).updateById(any(OrgInfo.class));
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static OrgInfoVo targetOrg(Long id) {
        OrgInfoVo vo = new OrgInfoVo();
        vo.setId(id);
        // ORG-002 requires non-empty orgCode on write paths that reach persistence.
        vo.setOrgCode("ORG-" + id);
        vo.setParentId(0L);
        return vo;
    }
}
