package com.alex.user.rbac;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermission.Scope;
import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, RbacRoleCodes.SUPER));

        Expression result = handler.getSqlSegment(null, ORG_ID_MS);

        assertNull(result, "super + ORG_ID should leave null where unchanged");
    }

    @Test
    public void testAdminOrgIdEqualsOrgId() {
        // RBAC-BE-SCOPE-002: 无子孙机构时，admin 的 ORG_ID scope 退化为仅本机构 EqualsTo。
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "admin"), OrgSubtreeLookup.NOOP);

        Expression result = handler.getSqlSegment(null, ORG_ID_MS);

        assertTrue(result instanceof EqualsTo, "admin + ORG_ID with no descendants should degrade to EqualsTo");
        EqualsTo eq = (EqualsTo) result;
        assertEquals("t_org_info.id", eq.getLeftExpression().toString(), "left should be annotation table.field");
        assertEquals("20", eq.getRightExpression().toString(), "right should be login org id");
    }

    @Test
    public void testAdminOrgIdIncludesDescendantsAsInExpression() {
        // RBAC-BE-SCOPE-002: admin 的 ORG_ID scope 需覆盖 selfOrgId ∪ 全部子孙机构。
        FakeOrgSubtreeLookup lookup = new FakeOrgSubtreeLookup(20L, Arrays.asList(21L, 22L));
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "admin"), lookup);

        Expression result = handler.getSqlSegment(null, ORG_ID_MS);

        assertTrue(result instanceof InExpression, "admin + ORG_ID with descendants should produce InExpression");
        InExpression in = (InExpression) result;
        assertEquals("t_org_info.id", in.getLeftExpression().toString(), "left should be annotation table.field");
        String sql = in.toString();
        assertTrue(sql.contains("20"), "IN list must include self org id");
        assertTrue(sql.contains("21"), "IN list must include descendant org id");
        assertTrue(sql.contains("22"), "IN list must include descendant org id");
        assertEquals(1, lookup.queriedOrgIds.size(), "lookup should be queried exactly once for self org id");
        assertEquals(20L, lookup.queriedOrgIds.get(0), "lookup should be queried with login user's own org id");
    }

    @Test
    public void testAdminOrgIdLookupFailureDegradesToSelfOnly() {
        // RBAC-BE-SCOPE-002: 子孙查询异常时必须降级为仅本机构，不能让请求 500 或数据泄露。
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "admin"), orgId -> {
            throw new RuntimeException("db unavailable");
        });

        Expression result = handler.getSqlSegment(null, ORG_ID_MS);

        assertTrue(result instanceof EqualsTo, "lookup failure should degrade to self-only EqualsTo");
        EqualsTo eq = (EqualsTo) result;
        assertEquals("20", eq.getRightExpression().toString(), "right should still be login org id");
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

    @Test
    void superviser_must_not_be_treated_as_super() {
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "superviser"));
        Expression result = handler.getSqlSegment(null, ORG_ID_MS);
        // 非超管必须追加过滤；超管才返回 null where
        assertNotNull(result, "RBAC-BE-SCOPE-001: superviser must not bypass data permission");
    }

    @Test
    void badmin_must_not_be_treated_as_admin() {
        DataPermissionHandlerImpl handler = handler(user(1L, 20L, "badmin"));
        Expression result = handler.getSqlSegment(null, USER_IDS_MS);
        // admin 走机构成员 IN；普通用户走 self EqualsTo
        assertTrue(result instanceof EqualsTo, "RBAC-BE-SCOPE-001: badmin must use user self-id filter");
    }

    private static DataPermissionHandlerImpl handler(TUserVo loginUser) {
        return new DataPermissionHandlerImpl(new FakeUserUtils(loginUser));
    }

    private static DataPermissionHandlerImpl handler(TUserVo loginUser, OrgSubtreeLookup orgSubtreeLookup) {
        return new DataPermissionHandlerImpl(new FakeUserUtils(loginUser), orgSubtreeLookup);
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

    /**
     * RBAC-BE-SCOPE-002: 固定返回 {@code descendantIds}，仅当查询的 orgId 匹配 {@code expectedOrgId}
     * 时才生效（其余情况返回空列表），并记录每次实际被查询的 orgId 供断言调用参数。
     */
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
