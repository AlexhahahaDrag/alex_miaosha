package com.alex.user.rbac;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RBAC-BE-RELATION-003: org-user / role-user page queries use USER_IDS scope on user_id.
 */
public class RelationPageDataPermissionTest {

    @Test
    void orgUserInfoGetPageUsesUserIdsOnUserId() throws NoSuchMethodException {
        DataPermission ann = getPageAnnotation(OrgUserInfoMapper.class, OrgUserInfoVo.class);
        assertEquals("t_org_user_info", ann.table());
        assertEquals("user_id", ann.field());
        assertEquals(DataPermission.Scope.USER_IDS, ann.scope());
    }

    @Test
    void roleUserInfoGetPageUsesUserIdsOnUserId() throws NoSuchMethodException {
        DataPermission ann = getPageAnnotation(RoleUserInfoMapper.class, RoleUserInfoVo.class);
        assertEquals("t_role_user_info", ann.table());
        assertEquals("user_id", ann.field());
        assertEquals(DataPermission.Scope.USER_IDS, ann.scope());
    }

    private static DataPermission getPageAnnotation(Class<?> mapperClass, Class<?> voClass)
            throws NoSuchMethodException {
        Method method = mapperClass.getMethod("getPage", Page.class, voClass);
        DataPermission ann = method.getAnnotation(DataPermission.class);
        assertNotNull(ann, () -> mapperClass.getSimpleName() + "#getPage must declare @DataPermission");
        return ann;
    }
}
