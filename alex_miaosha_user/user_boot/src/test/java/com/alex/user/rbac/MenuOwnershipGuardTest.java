package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.entity.MenuInfo;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.service.impl.MenuInfoServiceImp;
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
 * RBAC-BE-MENU-001: write-path ownership guard must go through scoped queryMenuInfo.
 */
@ExtendWith(MockitoExtension.class)
public class MenuOwnershipGuardTest {

    @Mock
    private MenuInfoMapper menuInfoMapper;
    @Mock
    private UserUtils userUtils;
    @Mock
    private RedisUtils redisUtils;

    private MenuInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new MenuInfoServiceImp(menuInfoMapper, redisUtils, userUtils);
    }

    @Test
    void updateMenuInfo_denied_whenScopedQueryReturnsNull() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.ADMIN));
        when(menuInfoMapper.queryMenuInfo("200")).thenReturn(null);

        MenuInfoVo vo = targetMenu(200L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updateMenuInfo(vo),
                "RBAC-BE-MENU-001: update must reject menus outside data scope");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("无权"),
                "exception message must contain 无权, actual=" + ex.getMsg());
        verify(menuInfoMapper, never()).updateById(any(MenuInfo.class));
    }

    @Test
    void queryMenuInfo_returnsNull_whenOutOfScope() {
        when(menuInfoMapper.queryMenuInfo("200")).thenReturn(null);

        MenuInfoVo result = assertDoesNotThrow(() -> service.queryMenuInfo("200"),
                "RBAC-BE-MENU-001: null mapper result must not NPE");
        assertNull(result, "out-of-scope menu query must return null, not throw");
    }

    @Test
    void updateMenuInfo_denied_whenParentIdEqualsSelf() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));

        MenuInfoVo vo = targetMenu(200L);
        vo.setParentId(200L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updateMenuInfo(vo),
                "updateMenuInfo must reject self-parent cycle");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("自身"),
                "exception message must mention 自身, actual=" + ex.getMsg());
        verify(menuInfoMapper, never()).updateById(any(MenuInfo.class));
    }

    @Test
    void updateMenuInfo_denied_whenParentIdIsDescendant() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        // 200 -> 201 -> 202; try setting 200's parentId to 202
        MenuInfoVo child201 = targetMenu(201L);
        child201.setParentId(200L);
        MenuInfoVo grandChild202 = targetMenu(202L);
        grandChild202.setParentId(201L);
        when(menuInfoMapper.queryMenuInfo("202")).thenReturn(grandChild202);
        when(menuInfoMapper.queryMenuInfo("201")).thenReturn(child201);

        MenuInfoVo vo = targetMenu(200L);
        vo.setParentId(202L);
        SystemException ex = assertThrows(SystemException.class, () -> service.updateMenuInfo(vo),
                "updateMenuInfo must reject descendant-parent cycle");
        assertTrue(ex.getMsg() != null && ex.getMsg().contains("下级"),
                "exception message must mention 下级, actual=" + ex.getMsg());
        verify(menuInfoMapper, never()).updateById(any(MenuInfo.class));
    }

    private static TUserVo loginUser(String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static MenuInfoVo targetMenu(Long id) {
        MenuInfoVo vo = new MenuInfoVo();
        vo.setId(id);
        vo.setName("menu-" + id);
        return vo;
    }
}
