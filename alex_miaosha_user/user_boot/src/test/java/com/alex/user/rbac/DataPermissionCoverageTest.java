package com.alex.user.rbac;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.permissionInfo.mapper.PermissionInfoMapper;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.user.mapper.TUserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * RBAC-BE-SCOPE-003: inventory of {@link DataPermission} on user/org/role/permission/menu query mappers.
 */
public class DataPermissionCoverageTest {

    @Test
    void tUserMapperQueryMethodsHaveDataPermission() throws NoSuchMethodException {
        assertHasDataPermission(TUserMapper.class, "queryTUser", String.class);
        assertHasDataPermission(TUserMapper.class, "getUserInfo", TUserVo.class);
        assertHasDataPermission(TUserMapper.class, "getPage", Page.class, TUserVo.class);
        assertHasDataPermission(TUserMapper.class, "getList", TUserVo.class);
    }

    @Test
    void orgInfoMapperQueryMethodsHaveDataPermission() throws NoSuchMethodException {
        assertHasDataPermission(OrgInfoMapper.class, "queryOrgInfo", String.class);
        assertHasDataPermission(OrgInfoMapper.class, "getPage", Page.class, OrgInfoVo.class);
        assertHasDataPermission(OrgInfoMapper.class, "getList", OrgInfoVo.class);
    }

    @Test
    void roleInfoMapperQueryMethodsHaveDataPermission() throws NoSuchMethodException {
        assertHasDataPermission(RoleInfoMapper.class, "queryRoleInfo", String.class);
        assertHasDataPermission(RoleInfoMapper.class, "getPage", Page.class, RoleInfoVo.class);
        assertRoleOrgBoundScope(RoleInfoMapper.class, "queryRoleInfo", String.class);
        assertRoleOrgBoundScope(RoleInfoMapper.class, "getPage", Page.class, RoleInfoVo.class);
    }

    @Test
    void permissionInfoMapperQueryMethodsHaveDataPermission() throws NoSuchMethodException {
        assertHasDataPermission(PermissionInfoMapper.class, "queryPermissionInfo", Long.class);
        assertHasDataPermission(PermissionInfoMapper.class, "getPage", Page.class, PermissionInfoVo.class);
    }

    @Test
    void menuInfoMapperQueryMethodsHaveDataPermission() throws NoSuchMethodException {
        assertHasDataPermission(MenuInfoMapper.class, "queryMenuInfo", String.class);
        assertHasDataPermission(MenuInfoMapper.class, "getPage", Page.class, MenuInfoVo.class);
        assertHasDataPermission(MenuInfoMapper.class, "getList", MenuInfoVo.class);
    }

    /**
     * C1 fix (batch2 final review)：{@code getListAll} 是全量菜单树 Redis 缓存
     * （全局键 {@code menu_all_tree}）的专用查询路径，**有意** 不挂 {@code @DataPermission}。
     * 如果这条断言失败（未来有人给 getListAll 加了注解），说明全局缓存又会被
     * 调用者的数据范围污染，必须先看 {@code MenuInfoServiceImp#getList} 的 isFullQuery 分支。
     */
    @Test
    void menuInfoMapperGetListAllIsIntentionallyUnscoped() throws NoSuchMethodException {
        Method method = MenuInfoMapper.class.getMethod("getListAll", MenuInfoVo.class);
        assertNull(method.getAnnotation(DataPermission.class),
                "MenuInfoMapper#getListAll must stay unscoped: it backs the global menu_all_tree cache "
                        + "and must never be filtered by caller data scope");
    }

    @Test
    void orgUserInfoMapperGetPageHasDataPermission() throws NoSuchMethodException {
        assertHasDataPermission(OrgUserInfoMapper.class, "getPage", Page.class, OrgUserInfoVo.class);
    }

    @Test
    void roleUserInfoMapperGetPageHasDataPermission() throws NoSuchMethodException {
        assertHasDataPermission(RoleUserInfoMapper.class, "getPage", Page.class, RoleUserInfoVo.class);
    }

    private static void assertHasDataPermission(Class<?> mapperClass, String methodName, Class<?>... paramTypes)
            throws NoSuchMethodException {
        Method method = mapperClass.getMethod(methodName, paramTypes);
        assertNotNull(method.getAnnotation(DataPermission.class),
                () -> mapperClass.getSimpleName() + "#" + methodName + " must declare @DataPermission");
    }

    private static void assertRoleOrgBoundScope(Class<?> mapperClass, String methodName, Class<?>... paramTypes)
            throws NoSuchMethodException {
        Method method = mapperClass.getMethod(methodName, paramTypes);
        DataPermission ann = method.getAnnotation(DataPermission.class);
        assertNotNull(ann);
        assertEquals(DataPermissionScope.ROLE_ORG_BOUND, ann.scope(),
                () -> mapperClass.getSimpleName() + "#" + methodName + " must use ROLE_ORG_BOUND");
        assertEquals("t_role_info", ann.table());
    }
}
