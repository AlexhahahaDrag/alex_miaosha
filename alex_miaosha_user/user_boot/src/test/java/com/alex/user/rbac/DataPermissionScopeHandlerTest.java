package com.alex.user.rbac;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermission.Scope;
import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * DataPermission.Scope handler unit tests (ORG_ID + USER_IDS branches).
 */
public class DataPermissionScopeHandlerTest {

    private static final String ORG_ID_MS =
            Fixtures.class.getName() + ".orgIdScope";
    private static final String USER_IDS_MS =
            Fixtures.class.getName() + ".userIdsScope";

    @Test
    public void testSuperOrgIdLeavesWhereUnchanged() {
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "super_admin"));

        Expression result = handler.getSqlSegment(null, ORG_ID_MS);

        assertNull(result, "super + ORG_ID should leave null where unchanged");
    }

    @Test
    public void testAdminOrgIdEqualsOrgId() {
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "admin"));

        Expression result = handler.getSqlSegment(null, ORG_ID_MS);

        assertTrue(result instanceof EqualsTo, "admin + ORG_ID should produce EqualsTo");
        EqualsTo eq = (EqualsTo) result;
        assertEquals("t_org_info.id", eq.getLeftExpression().toString(), "left should be annotation table.field");
        assertEquals("20", eq.getRightExpression().toString(), "right should be login org id");
    }

    @Test
    public void testUserOrgIdEqualsOrgId() {
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "user"));

        Expression result = handler.getSqlSegment(null, ORG_ID_MS);

        assertTrue(result instanceof EqualsTo, "user + ORG_ID should produce EqualsTo");
        EqualsTo eq = (EqualsTo) result;
        assertEquals("t_org_info.id", eq.getLeftExpression().toString(), "left should be annotation table.field");
        assertEquals("20", eq.getRightExpression().toString(), "right should be login org id");
    }

    @Test
    public void testAdminUserIdsBuildsOrgMemberInExpression() {
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "admin"));

        Expression result = handler.getSqlSegment(null, USER_IDS_MS);

        assertTrue(result instanceof InExpression, "admin + USER_IDS should produce InExpression");
        String sql = result.toString();
        assertTrue(sql.contains("alex_user.t_org_user_info"), "subselect should target org_user_info");
        assertTrue(sql.contains("status"), "subselect should filter status");
        assertTrue(sql.contains("is_delete"), "subselect should filter is_delete");
    }

    @Test
    public void testUserUserIdsEqualsSelfId() {
        DataPermissionHandlerImpl handler = handler(user(99L, 20L, "user"));

        Expression result = handler.getSqlSegment(null, USER_IDS_MS);

        assertTrue(result instanceof EqualsTo, "user + USER_IDS should produce EqualsTo");
        EqualsTo eq = (EqualsTo) result;
        assertEquals("t_user.operator", eq.getLeftExpression().toString(), "left should be annotation table.field");
        assertEquals("99", eq.getRightExpression().toString(), "right should be login user id");
    }

    private static DataPermissionHandlerImpl handler(TUserVo loginUser) {
        return new DataPermissionHandlerImpl(new FakeUserUtils(loginUser));
    }

    private static TUserVo user(Long userId, Long orgId, String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(userId);

        OrgInfoVo org = new OrgInfoVo();
        org.setId(orgId);
        user.setOrgInfoVo(org);

        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    private static void assertNull(Object actual, String message) {
        if (actual != null) {
            throw new AssertionError(message + ", expected null, actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ", expected: " + expected + ", actual: " + actual);
        }
    }

    /**
     * Annotation fixtures resolved via mappedStatementId Class.forName lookup.
     */
    private static final class Fixtures {
        @DataPermission(table = "t_org_info", field = "id", scope = Scope.ORG_ID)
        void orgIdScope() {
        }

        @DataPermission(table = "t_user", field = "operator")
        void userIdsScope() {
        }
    }

    private static final class FakeUserUtils extends UserUtils {
        private final TUserVo loginUser;

        private FakeUserUtils(TUserVo loginUser) {
            super(null);
            this.loginUser = loginUser;
        }

        @Override
        public TUserVo getLoginUser() {
            return loginUser;
        }
    }
}
