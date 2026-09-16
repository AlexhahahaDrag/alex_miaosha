package com.alex.user.rbac;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ROLE_ORG_BOUND scope: roles visible via valid t_role_org_info bindings in caller org scope S.
 */
public class RoleOrgBoundScopeTest {

    private static final String ROLE_ORG_BOUND_MS =
            Fixtures.class.getName() + ".roleOrgBoundScope";

    @Test
    public void testSuperLeavesWhereUnchanged() {
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, RbacRoleCodes.SUPER));

        Expression result = handler.getSqlSegment(null, ROLE_ORG_BOUND_MS);

        assertNull(result, "super + ROLE_ORG_BOUND should leave null where unchanged");
        if (result != null) {
            assertTrue(!result.toString().contains("t_role_org_info"),
                    "super must not append t_role_org_info filter");
        }
    }

    @Test
    public void testAdminWithDescendantsBuildsExistsInList() {
        FakeOrgSubtreeLookup lookup = new FakeOrgSubtreeLookup(20L, Arrays.asList(21L, 22L));
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "admin"), lookup);

        Expression result = handler.getSqlSegment(null, ROLE_ORG_BOUND_MS);

        assertTrue(result instanceof ExistsExpression, "admin + ROLE_ORG_BOUND should produce ExistsExpression");
        String sql = result.toString();
        assertTrue(sql.contains("t_role_org_info"), "EXISTS must target t_role_org_info");
        assertTrue(sql.contains("IN"), "multi-org scope must use IN list");
        assertTrue(sql.contains("'20'"), "IN list must include self org as string");
        assertTrue(sql.contains("'21'"), "IN list must include descendant org");
        assertTrue(sql.contains("'22'"), "IN list must include descendant org");
        assertTrue(sql.contains("CAST"), "role_id compare must CAST table.id AS CHAR");
        assertEquals(1, lookup.queriedOrgIds.size(), "lookup should be queried once");
        assertEquals(20L, lookup.queriedOrgIds.get(0), "lookup queried with login org");
    }

    @Test
    public void testUserBuildsExistsForSelfOrg() {
        DataPermissionHandlerImpl handler = handler(user(99L, 20L, "user"), OrgSubtreeLookup.NOOP);

        Expression result = handler.getSqlSegment(null, ROLE_ORG_BOUND_MS);

        assertTrue(result instanceof ExistsExpression, "user + ROLE_ORG_BOUND should produce ExistsExpression");
        String sql = result.toString();
        assertTrue(sql.contains("t_role_org_info"), "EXISTS must target t_role_org_info");
        assertTrue(sql.contains("'20'"), "self org must appear as string literal");
        assertTrue(!sql.contains("'21'"), "user must not expand descendants");
    }

    @Test
    public void testNoLoginOrgFailsClosed() {
        TUserVo loginUser = user(1L, null, "admin");
        loginUser.setOrgInfoVo(null);
        loginUser.setOrgId(null);
        DataPermissionHandlerImpl handler = handler(loginUser);

        Expression result = handler.getSqlSegment(null, ROLE_ORG_BOUND_MS);

        assertNotNull(result, "missing login org must fail closed");
        assertEquals("1 = 0", result.toString().trim(), "fail-closed should be 1 = 0");
    }

    private static DataPermissionHandlerImpl handler(TUserVo loginUser) {
        return new DataPermissionHandlerImpl(new FakeUserUtils(loginUser), OrgSubtreeLookup.NOOP);
    }

    private static DataPermissionHandlerImpl handler(TUserVo loginUser, OrgSubtreeLookup orgSubtreeLookup) {
        return new DataPermissionHandlerImpl(new FakeUserUtils(loginUser), orgSubtreeLookup);
    }

    private static TUserVo user(Long userId, Long orgId, String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(userId);

        if (orgId != null) {
            OrgInfoVo org = new OrgInfoVo();
            org.setId(orgId);
            user.setOrgInfoVo(org);
        }

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

    private static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new AssertionError(message + ", expected non-null");
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

    private static final class Fixtures {
        @DataPermission(table = "t_role_info", scope = DataPermissionScope.ROLE_ORG_BOUND)
        void roleOrgBoundScope() {
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

    private static final class FakeOrgSubtreeLookup implements OrgSubtreeLookup {
        private final Long expectedOrgId;
        private final List<Long> descendantIds;
        private final List<Long> queriedOrgIds = new ArrayList<>();

        private FakeOrgSubtreeLookup(Long expectedOrgId, List<Long> descendantIds) {
            this.expectedOrgId = expectedOrgId;
            this.descendantIds = descendantIds;
        }

        @Override
        public List<Long> findDescendantOrgIds(Long orgId) {
            queriedOrgIds.add(orgId);
            return expectedOrgId.equals(orgId) ? descendantIds : Collections.emptyList();
        }
    }
}
