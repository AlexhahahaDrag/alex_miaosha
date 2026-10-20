package com.alex.user.feign;

import com.alex.api.user.menuInfo.fallback.MenuInfoFallbackFactory;
import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.fallback.OrgInfoFallbackFactory;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.fallback.OrgUserInfoFallbackFactory;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.api.user.permissionInfo.fallback.PermissionInfoFallbackFactory;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.roleInfo.fallback.RoleInfoFallbackFactory;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.rolePermissionInfo.fallback.RolePermissionInfoFallbackFactory;
import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.api.user.roleUserInfo.fallback.RoleUserInfoFallbackFactory;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.api.user.userInfo.fallback.UserFallbackFactory;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User微服务各类 FallbackFactory 规范化实现与优雅降级集成测试")
class UserFallbackFactoryIT {

    @Test
    @DisplayName("1. 验证 UserFallbackFactory 返回安全接口代理，方法调用返回标准化降级错误码")
    void userFallbackFactory_returnsSafeProxyWithErrorCode() {
        UserFallbackFactory factory = new UserFallbackFactory();
        Throwable cause = new RuntimeException("Connection timed out to alex-user-dev");

        var proxy = factory.create(cause);
        assertNotNull(proxy, "FallbackFactory 必须返回非空接口代理实现");

        Result<List<TUserVo>> listResult = proxy.getList(new TUserVo());
        assertNotNull(listResult);
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), listResult.getCode());
        assertTrue(listResult.getMessage().contains("用户服务不可用"));

        Result<TUserVo> userResult = proxy.queryUser("123");
        assertNotNull(userResult);
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), userResult.getCode());

        assertNull(proxy.getUserByUsername("admin"));
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), proxy.authToken("token").getCode());
    }

    @Test
    @DisplayName("2. 验证 MenuInfoFallbackFactory 返回安全接口代理，调用时安全容错")
    void menuInfoFallbackFactory_returnsSafeProxyWithErrorCode() {
        MenuInfoFallbackFactory factory = new MenuInfoFallbackFactory();
        Throwable cause = new RuntimeException("Read timed out");

        var proxy = factory.create(cause);
        assertNotNull(proxy, "FallbackFactory 必须返回非空接口代理实现");

        Result<MenuInfoVo> menuResult = proxy.queryMenuInfo("999");
        assertNotNull(menuResult);
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), menuResult.getCode());
        assertTrue(menuResult.getMessage().contains("菜单服务不可用"));

        Result<Page<MenuInfoVo>> pageResult = proxy.getMenuInfoPage(1L, 10L, new MenuInfoVo());
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), pageResult.getCode());
    }

    @Test
    @DisplayName("3. 验证 OrgInfoFallbackFactory 与 OrgUserInfoFallbackFactory 安全降级")
    void orgFallbackFactories_returnSafeProxiesWithErrorCode() {
        OrgInfoFallbackFactory orgFactory = new OrgInfoFallbackFactory();
        var orgProxy = orgFactory.create(new RuntimeException("Org service 503"));
        assertNotNull(orgProxy);
        Result<OrgInfoVo> queryOrgResult = orgProxy.queryOrgInfo("1");
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), queryOrgResult.getCode());
        assertTrue(queryOrgResult.getMessage().contains("机构服务不可用"));

        OrgUserInfoFallbackFactory orgUserFactory = new OrgUserInfoFallbackFactory();
        var orgUserProxy = orgUserFactory.create(new RuntimeException("OrgUser service 504"));
        assertNotNull(orgUserProxy);
        Result<OrgUserInfoVo> queryOrgUserResult = orgUserProxy.queryOrgUserInfo("1");
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), queryOrgUserResult.getCode());
        assertTrue(queryOrgUserResult.getMessage().contains("用户机构服务不可用"));
    }

    @Test
    @DisplayName("4. 验证 Role 领域各大 FallbackFactory（RoleInfo/RolePermission/RoleUser）安全降级")
    void roleFallbackFactories_returnSafeProxiesWithErrorCode() {
        RoleInfoFallbackFactory roleFactory = new RoleInfoFallbackFactory();
        var roleProxy = roleFactory.create(new RuntimeException("Role service down"));
        assertNotNull(roleProxy);
        Result<RoleInfoVo> roleResult = roleProxy.queryRoleInfo("100");
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), roleResult.getCode());
        assertTrue(roleResult.getMessage().contains("角色服务不可用"));

        RolePermissionInfoFallbackFactory rolePermFactory = new RolePermissionInfoFallbackFactory();
        var rolePermProxy = rolePermFactory.create(new RuntimeException("RolePerm down"));
        assertNotNull(rolePermProxy);
        Result<RolePermissionInfoVo> rolePermResult = rolePermProxy.queryRolePermissionInfo("200");
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), rolePermResult.getCode());

        RoleUserInfoFallbackFactory roleUserFactory = new RoleUserInfoFallbackFactory();
        var roleUserProxy = roleUserFactory.create(new RuntimeException("RoleUser down"));
        assertNotNull(roleUserProxy);
        Result<RoleUserInfoVo> roleUserResult = roleUserProxy.queryRoleUserInfo("300");
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), roleUserResult.getCode());
    }

    @Test
    @DisplayName("5. 验证 PermissionInfoFallbackFactory 安全降级")
    void permissionInfoFallbackFactory_returnsSafeProxyWithErrorCode() {
        PermissionInfoFallbackFactory permFactory = new PermissionInfoFallbackFactory();
        var permProxy = permFactory.create(new RuntimeException("Perm service unavailable"));
        assertNotNull(permProxy);

        Result<PermissionInfoVo> permResult = permProxy.queryPermissionInfo("888");
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), permResult.getCode());
        assertTrue(permResult.getMessage().contains("权限服务不可用"));
    }
}
