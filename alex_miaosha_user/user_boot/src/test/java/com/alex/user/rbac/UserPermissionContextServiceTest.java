package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.base.constants.SysConf;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.rbac.service.impl.UserPermissionContextServiceImpl;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alex.user.user.service.impl.TUserServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class UserPermissionContextServiceTest {

    @Test
    public void testTUserVoExposesPermissionContextAndPermissionCodes() {
        UserPermissionContextVo permissionContext = new UserPermissionContextVo();
        List<String> permissionCodes = Arrays.asList("user:list", "user:create");

        TUserVo userVo = new TUserVo();
        userVo.setPermissionContext(permissionContext);
        userVo.setPermissionCodes(permissionCodes);

        assertSame(permissionContext, userVo.getPermissionContext(), "permissionContext should round-trip through TUserVo");
        assertEquals(permissionCodes, userVo.getPermissionCodes(), "permissionCodes should be readable from TUserVo");
    }

    @Test
    public void testBuildContextMergesRolePermissionsAndUsesFirstOrg() {
        Long userId = 1001L;
        OrgInfoVo firstOrg = new OrgInfoVo();
        firstOrg.setOrgCode("org-a");
        OrgInfoVo secondOrg = new OrgInfoVo();
        secondOrg.setOrgCode("org-b");
        RoleInfoVo firstRole = role("user-admin", permissions("user:add"));
        RoleInfoVo secondRole = role("role-admin", permissions("user:add", "role:update"));

        UserPermissionContextService service = new UserPermissionContextServiceImpl(
                orgService(userId, Arrays.asList(firstOrg, secondOrg)),
                roleService(userId, Arrays.asList(firstRole, secondRole)),
                menuService(Collections.emptyList()),
                Runnable::run,
                null
        );

        UserPermissionContextVo context = service.buildContext(userId);

        assertSame(firstOrg, context.getOrgInfo(), "orgInfo should use the first organization");
        assertEquals(2, context.getRoleList().size(), "roleList should include all roles");
        assertEquals(Arrays.asList("user:add", "role:update"), context.getPermissionCodes(), "permission codes should be stable and unique");
        assertEquals(Arrays.asList("user:add", "role:update"), context.getButtonPermissionCodes(), "button permission codes should match merged permissions");
    }

    @Test
    public void testBuildContextKeepsAllMenusForSuperAdmin() {
        Long userId = 1002L;
        RoleInfoVo superRole = role("super_super", permissions("user:add"));
        MenuInfoVo allowedMenu = new MenuInfoVo();
        allowedMenu.setName("User");
        allowedMenu.setPermissionCode("user:add");
        MenuInfoVo restrictedMenu = new MenuInfoVo();
        restrictedMenu.setName("System");
        restrictedMenu.setPermissionCode("system:manage");
        List<MenuInfoVo> allMenus = Arrays.asList(allowedMenu, restrictedMenu);

        UserPermissionContextService service = new UserPermissionContextServiceImpl(
                orgService(userId, Collections.emptyList()),
                roleService(userId, Collections.singletonList(superRole)),
                menuService(allMenus),
                Runnable::run,
                null
        );

        UserPermissionContextVo context = service.buildContext(userId);

        assertEquals(Boolean.TRUE, context.getSuperAdmin(), "super_super role should mark context as super admin");
        assertSame(allMenus, context.getMenuList(), "super admin menus should not be filtered");
    }

    @Test
    public void testBuildContextIncludeMenusFalseSkipsMenuList() {
        Long userId = 1004L;
        RoleInfoVo role = role("user-admin", permissions("user:add"));
        MenuInfoVo menu = new MenuInfoVo();
        menu.setName("User");
        menu.setPermissionCode("user:add");

        UserPermissionContextService service = new UserPermissionContextServiceImpl(
                orgService(userId, Collections.emptyList()),
                roleService(userId, Collections.singletonList(role)),
                menuService(Collections.singletonList(menu)),
                Runnable::run,
                null
        );

        UserPermissionContextVo context = service.buildContext(userId, false);

        assertEquals(Collections.emptyList(), context.getMenuList(), "login slim context must not carry menus");
        assertEquals(Collections.singletonList("user:add"), context.getPermissionCodes());
    }

    @Test
    public void testApplyPermissionContextSetsLoginResponseCompatibilityFields() {
        TUserVo userVo = new TUserVo();
        OrgInfoVo orgInfo = new OrgInfoVo();
        orgInfo.setOrgCode("org-a");
        orgInfo.setOrgName("Org A");
        RoleInfoVo firstRole = new RoleInfoVo();
        firstRole.setRoleCode("role-a");
        firstRole.setRoleName("Role A");
        RoleInfoVo secondRole = new RoleInfoVo();
        secondRole.setRoleCode("role-b");
        secondRole.setRoleName("Role B");
        List<RoleInfoVo> roleList = Arrays.asList(firstRole, secondRole);
        List<String> permissionCodes = Arrays.asList("user:list", "role:update");
        List<String> buttonPermissionCodes = Arrays.asList("user:create", "role:delete");
        MenuInfoVo menu1 = new MenuInfoVo();
        menu1.setName("User");
        menu1.setPermissionCode("user:list");
        MenuInfoVo menu2 = new MenuInfoVo();
        menu2.setName("Role");
        menu2.setPermissionCode("role:update");
        List<MenuInfoVo> menuList = Arrays.asList(menu1, menu2);
        UserPermissionContextVo context = new UserPermissionContextVo();
        context.setOrgInfo(orgInfo);
        context.setRoleList(roleList);
        context.setPermissionCodes(permissionCodes);
        context.setButtonPermissionCodes(buttonPermissionCodes);
        context.setMenuList(menuList);

        TUserServiceImpl.applyPermissionContext(userVo, context);

        assertSame(context, userVo.getPermissionContext(), "permissionContext should be attached to login response");
        assertSame(orgInfo, userVo.getOrgInfoVo(), "legacy orgInfoVo should mirror context orgInfo");
        assertSame(roleList, userVo.getRoleInfoVoList(), "legacy roleInfoVoList should mirror context roleList");
        assertEquals("Org A", userVo.getOrgName(), "legacy orgName should mirror context orgInfo");
        assertEquals("org-a", userVo.getOrgCode(), "legacy orgCode should mirror context orgInfo");
        assertSame(permissionCodes, userVo.getPermissionCodes(), "legacy permissionCodes should mirror context permissionCodes");
        assertSame(buttonPermissionCodes, userVo.getButtonPermissionCodes(), "legacy buttonPermissionCodes should mirror context buttonPermissionCodes");
        assertSame(menuList, userVo.getMenuInfoVoList(), "legacy menuInfoVoList should mirror context menuList");
    }

    @Test
    public void testRefreshLoginPermissionContextRebuildsCachedUserContext() {
        Long userId = 1003L;
        TUserVo cachedUser = new TUserVo();
        cachedUser.setId(userId);
        OrgInfoVo orgInfo = new OrgInfoVo();
        orgInfo.setOrgCode("fresh-org");
        orgInfo.setOrgName("Fresh Org");
        RoleInfoVo roleInfo = new RoleInfoVo();
        roleInfo.setRoleCode("fresh-role");
        roleInfo.setRoleName("Fresh Role");
        UserPermissionContextVo freshContext = new UserPermissionContextVo();
        freshContext.setOrgInfo(orgInfo);
        freshContext.setRoleList(Collections.singletonList(roleInfo));
        freshContext.setPermissionCodes(Collections.singletonList("fresh:permission"));
        UserPermissionContextService contextService = new UserPermissionContextService() {
            @Override
            public UserPermissionContextVo buildContext(Long requestedUserId, boolean includeMenus) {
                assertEquals(userId, requestedUserId, "cached login should rebuild context for redis user id");
                assertEquals(false, includeMenus, "fast-path refresh should use slim context");
                return freshContext;
            }

            @Override
            public List<MenuInfoVo> listVisibleMenus(Long requestedUserId) {
                throw new UnsupportedOperationException();
            }
        };
        TUserServiceImpl service = new TUserServiceImpl(null, null, null, null, null,
                null, null, null, null, null, null, Runnable::run, contextService, null, null, null);
        TUserVo refreshedUser = service.refreshLoginPermissionContext(cachedUser);

        assertSame(cachedUser, refreshedUser, "cached login should keep using the redis user object");
        assertSame(freshContext, refreshedUser.getPermissionContext(), "cached login should attach fresh permission context");
        assertEquals("Fresh Org", refreshedUser.getOrgName(), "cached login should refresh legacy orgName");
    }

    @Test
    public void testCompleteLoginResponseDoesNotWaitForAvatar() {
        TUserVo userVo = new TUserVo();
        OrgInfoVo orgInfo = new OrgInfoVo();
        orgInfo.setOrgCode("org-a");
        RoleInfoVo roleInfo = new RoleInfoVo();
        roleInfo.setRoleCode("role-a");
        UserPermissionContextVo context = new UserPermissionContextVo();
        context.setOrgInfo(orgInfo);
        context.setRoleList(Collections.singletonList(roleInfo));
        CompletableFuture<Void> avatarFuture = new CompletableFuture<>();
        TUserServiceImpl.completeLoginResponse(userVo, avatarFuture, context);

        assertSame(context, userVo.getPermissionContext(), "login response should include permission context before returning");
        assertEquals(null, userVo.getAvatarUrl(), "login must not block waiting for avatar");
        avatarFuture.complete(null);
    }

    private static RoleInfoVo role(String roleCode, List<PermissionInfoVo> permissions) {
        RoleInfoVo roleInfo = new RoleInfoVo();
        roleInfo.setRoleCode(roleCode);
        roleInfo.setPermissionList(permissions);
        return roleInfo;
    }

    private static List<PermissionInfoVo> permissions(String... permissionCodes) {
        List<PermissionInfoVo> permissions = new ArrayList<>();
        for (String permissionCode : permissionCodes) {
            PermissionInfoVo p = new PermissionInfoVo();
            p.setPermissionCode(permissionCode);
            permissions.add(p);
        }
        return permissions;
    }

    private static OrgUserInfoService orgService(Long expectedUserId, List<OrgInfoVo> orgs) {
        return proxy(OrgUserInfoService.class, "getOrgInfoList", args -> {
            assertEquals(expectedUserId, args[0], "org query should use requested userId");
            return orgs;
        });
    }

    private static RoleUserInfoService roleService(Long expectedUserId, List<RoleInfoVo> roles) {
        return proxy(RoleUserInfoService.class, "getRoleInfoList", args -> {
            assertEquals(expectedUserId, args[0], "role query should use requested userId");
            assertEquals(Boolean.TRUE, args[1], "role query should include permissions");
            return roles;
        });
    }

    private static MenuInfoService menuService(List<MenuInfoVo> menus) {
        return proxy(MenuInfoService.class, "getList", args -> {
            MenuInfoVo query = (MenuInfoVo) args[0];
            assertEquals(SysConf.VALID_STATUS, query.getStatus(), "menu query should request valid menus");
            return menus;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> serviceType, String supportedMethod, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class<?>[]{serviceType}, (proxy, method, args) -> {
            if (supportedMethod.equals(method.getName())) {
                return handler.invoke(args == null ? new Object[0] : args);
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private interface MethodHandler {
        Object invoke(Object[] args);
    }
}
