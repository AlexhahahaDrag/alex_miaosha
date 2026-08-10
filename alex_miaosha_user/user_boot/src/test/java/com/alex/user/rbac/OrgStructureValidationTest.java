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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-ORG-002: orgCode uniqueness + parent existence + cycle prevention.
 */
@ExtendWith(MockitoExtension.class)
public class OrgStructureValidationTest {

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
    void addOrgInfo_rejectsDuplicateOrgCode_withoutInsert() {
        when(orgInfoMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        OrgInfoVo vo = new OrgInfoVo();
        vo.setOrgCode("DUP-CODE");
        vo.setOrgName("dup");
        vo.setParentId(0L);

        SystemException ex = assertThrows(SystemException.class, () -> service.addOrgInfo(vo),
                "RBAC-BE-ORG-002: duplicate orgCode must be rejected on add");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("机构编码"),
                "message should mention 机构编码, actual=" + ex.getMsg());
        verify(orgInfoMapper, never()).insert(any(OrgInfo.class));
    }

    @Test
    void updateOrgInfo_rejectsSelfAsParent_withoutUpdate() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(orgInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        OrgInfoVo vo = new OrgInfoVo();
        vo.setId(100L);
        vo.setOrgCode("ORG-100");
        vo.setParentId(100L);

        SystemException ex = assertThrows(SystemException.class, () -> service.updateOrgInfo(vo),
                "RBAC-BE-ORG-002: parentId == self must be rejected");
        assertTrue(ex.getMsg() != null && (ex.getMsg().contains("自身") || ex.getMsg().contains("父")),
                "message should mention self/parent, actual=" + ex.getMsg());
        verify(orgInfoMapper, never()).updateById(any(OrgInfo.class));
    }

    @Test
    void updateOrgInfo_rejectsCycle_withoutUpdate() {
        // A(1) -> parent B(2); B.parentId = A(1) => cycle
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(orgInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        OrgInfo parentB = new OrgInfo();
        parentB.setId(2L);
        parentB.setOrgCode("ORG-B");
        parentB.setParentId(1L);
        parentB.setIsDelete(0);
        when(orgInfoMapper.selectById(2L)).thenReturn(parentB);

        OrgInfoVo vo = new OrgInfoVo();
        vo.setId(1L);
        vo.setOrgCode("ORG-A");
        vo.setParentId(2L);

        SystemException ex = assertThrows(SystemException.class, () -> service.updateOrgInfo(vo),
                "RBAC-BE-ORG-002: A->B->A cycle must be rejected");
        assertTrue(ex.getMsg() != null && (ex.getMsg().contains("环") || ex.getMsg().contains("父")),
                "message should mention cycle/parent, actual=" + ex.getMsg());
        verify(orgInfoMapper, never()).updateById(any(OrgInfo.class));
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
