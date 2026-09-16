package com.alex.user.rbac;

import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.service.impl.MenuInfoServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-MENU-002: reject menu delete when undeleted child menus exist.
 */
@ExtendWith(MockitoExtension.class)
public class MenuDeleteGuardTest {

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
        ReflectionTestUtils.setField(service, "baseMapper", menuInfoMapper);
    }

    @Test
    void deleteMenuInfo_rejectsWhenChildMenusExist_withoutDeleteBatchIds() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(menuInfoMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        SystemException ex = assertThrows(SystemException.class, () -> service.deleteMenuInfo("100"),
                "RBAC-BE-MENU-002: delete must reject menus that still have children");
        assertTrue(ex.getMsg() != null && (ex.getMsg().contains("子") || ex.getMsg().contains("下级")),
                "message must mention child menus, actual=" + ex.getMsg());
        verify(menuInfoMapper, never()).deleteBatchIds(anyList());
    }

    @Test
    void deleteMenuInfo_allowedWhenNoChildMenus() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER));
        when(menuInfoMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(menuInfoMapper.deleteBatchIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteMenuInfo("100"));
        verify(menuInfoMapper).deleteBatchIds(List.of("100"));
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
