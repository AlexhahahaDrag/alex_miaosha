package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.rbac.service.impl.UserPermissionContextServiceImpl;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 阶段二十九：用户中心 RBAC 动态权限上下文构建与 Redis 防污染集成测试
 */
public class UserPermissionContextBuildIT {

    @Mock
    private OrgUserInfoService orgUserInfoService;

    @Mock
    private RoleUserInfoService roleUserInfoService;

    @Mock
    private MenuInfoService menuInfoService;

    @Mock
    private RedisUtils redisUtils;

    private final Executor directExecutor = Runnable::run;
    private UserPermissionContextService contextService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contextService = new UserPermissionContextServiceImpl(
                orgUserInfoService,
                roleUserInfoService,
                menuInfoService,
                directExecutor,
                redisUtils
        );
    }

    private static PermissionInfoVo perm(String code) {
        PermissionInfoVo p = new PermissionInfoVo();
        p.setPermissionCode(code);
        return p;
    }

    private static RoleInfoVo createRole(String roleCode, List<PermissionInfoVo> permissions) {
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        role.setPermissionList(permissions);
        return role;
    }

    @Test
    @DisplayName("上下文构建：聚合多角色并去重权限码，提取第一有效机构")
    void buildContext_mergesMultiRolesAndExtractsFirstOrg() {
        Long userId = 10001L;

        OrgInfoVo org1 = new OrgInfoVo();
        org1.setId(101L);
        org1.setOrgCode("FINANCE_DEPT");

        OrgInfoVo org2 = new OrgInfoVo();
        org2.setId(102L);
        org2.setOrgCode("HR_DEPT");

        RoleInfoVo roleA = createRole("ACCOUNTANT", List.of(perm("finance:record:view"), perm("finance:record:add")));
        RoleInfoVo roleB = createRole("AUDITOR", List.of(perm("finance:record:view"), perm("finance:audit:approve")));

        when(redisUtils.get(eq(LoginKey.loginKey), contains(String.valueOf(userId)), eq(UserPermissionContextVo.class))).thenReturn(null);
        when(orgUserInfoService.getOrgInfoList(userId)).thenReturn(List.of(org1, org2));
        when(roleUserInfoService.getRoleInfoList(userId, true)).thenReturn(List.of(roleA, roleB));
        when(menuInfoService.getList(any())).thenReturn(Collections.emptyList());

        UserPermissionContextVo context = contextService.buildContext(userId, true);

        assertNotNull(context);
        assertEquals(101L, context.getOrgInfo().getId(), "必须取第一有效机构");
        assertEquals(2, context.getRoleList().size());
        // 验证去重后的权限码（按角色出现顺序保序）
        assertEquals(List.of("finance:record:view", "finance:record:add", "finance:audit:approve"), context.getPermissionCodes());
        assertEquals(context.getPermissionCodes(), context.getButtonPermissionCodes());
        assertFalse(context.getSuperAdmin());

        // 验证写入 Redis 缓存
        verify(redisUtils, times(1)).setEx(eq(LoginKey.loginKey), contains(String.valueOf(userId)), anyString(), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("超管直通契约：拥有 super_super 角色的用户全量放行菜单，不执行权限码裁剪")
    void buildContext_superAdminBypassesMenuFiltering() {
        Long userId = 99999L;
        RoleInfoVo superRole = createRole(RbacRoleCodes.SUPER, List.of(perm("all")));

        MenuInfoVo restrictedMenu = new MenuInfoVo();
        restrictedMenu.setId(88L);
        restrictedMenu.setName("高级系统设定");
        restrictedMenu.setPermissionCode("sys:secret:config");

        when(redisUtils.get(eq(LoginKey.loginKey), anyString(), eq(UserPermissionContextVo.class))).thenReturn(null);
        when(orgUserInfoService.getOrgInfoList(userId)).thenReturn(Collections.emptyList());
        when(roleUserInfoService.getRoleInfoList(userId, true)).thenReturn(List.of(superRole));
        when(menuInfoService.getList(any())).thenReturn(List.of(restrictedMenu));

        UserPermissionContextVo context = contextService.buildContext(userId, true);

        assertTrue(context.getSuperAdmin());
        assertEquals(1, context.getMenuList().size());
        assertEquals("高级系统设定", context.getMenuList().get(0).getName());
    }

    @Test
    @DisplayName("瘦身模式防污染：命中 Redis 缓存且 includeMenus=false 时，必须剥离 menuList 且保持原缓存对象完整")
    void buildContext_cachedSlimMode_stripsMenusSafely() {
        Long userId = 88888L;
        UserPermissionContextVo cached = new UserPermissionContextVo();
        cached.setPermissionCodes(List.of("user:view"));
        MenuInfoVo menu = new MenuInfoVo();
        menu.setName("工作台");
        cached.setMenuList(new ArrayList<>(List.of(menu)));

        when(redisUtils.get(eq(LoginKey.loginKey), contains(String.valueOf(userId)), eq(UserPermissionContextVo.class))).thenReturn(cached);

        // includeMenus = false
        UserPermissionContextVo slim = contextService.buildContext(userId, false);

        assertNotNull(slim);
        assertTrue(slim.getMenuList().isEmpty(), "瘦身模式下返回的 menuList 必须为空列表");
        // 原始缓存中的 menuList 不得被清空
        assertEquals(1, cached.getMenuList().size(), "原缓存对象的 menuList 严禁被清空");
        // 不触发全量菜单查询
        verify(menuInfoService, never()).getList(any());
    }

    @Test
    @DisplayName("高可用降级契约：Redis 读写抛出异常时，业务流程不中断，降级为实时计算")
    void buildContext_redisExceptionDegradesGracefully() {
        Long userId = 77777L;
        // 模拟 Redis 抛异常
        doThrow(new RuntimeException("Redis connection timed out"))
                .when(redisUtils).get(any(), any(), any());
        doThrow(new RuntimeException("Redis write timeout"))
                .when(redisUtils).setEx(any(), any(), anyString(), anyLong(), any());

        when(orgUserInfoService.getOrgInfoList(userId)).thenReturn(Collections.emptyList());
        when(roleUserInfoService.getRoleInfoList(userId, true)).thenReturn(Collections.emptyList());
        when(menuInfoService.getList(any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> {
            UserPermissionContextVo context = contextService.buildContext(userId, true);
            assertNotNull(context);
            assertTrue(context.getPermissionCodes().isEmpty());
        });
    }
}
