package com.alex.user.rbac;

import com.alex.user.roleOrgInfo.entity.RoleOrgInfo;
import com.alex.user.roleOrgInfo.mapper.RoleOrgInfoMapper;
import com.alex.user.roleOrgInfo.service.impl.RoleOrgInfoServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RoleOrgAssignmentServiceTest {

    @Test
    public void testAssignOrgsInvalidatesOldAndCreatesNew() {
        RoleOrgInfo old = assignment(1L, "1", "10", "1");
        TestableRoleOrgInfoService service = new TestableRoleOrgInfoService(old);

        Boolean result = service.assignOrgs(1L, Arrays.asList(20L, 30L));

        assertTrue(Boolean.TRUE.equals(result), "should return true");
        assertEquals("0", old.getStatus(), "old active should be invalidated");
        assertEquals(2, service.savedAssignments.size(), "two new rows");
        assertAssignment(service.savedAssignments.get(0), "1", "20", "1");
        assertAssignment(service.savedAssignments.get(1), "1", "30", "1");
    }

    @Test
    public void testAssignOrgsEmptyOnlyInvalidates() {
        RoleOrgInfo old = assignment(1L, "1", "10", "1");
        TestableRoleOrgInfoService service = new TestableRoleOrgInfoService(old);

        Boolean result = service.assignOrgs(1L, Collections.emptyList());

        assertTrue(Boolean.TRUE.equals(result), "should return true");
        assertEquals("0", old.getStatus(), "old active should be invalidated");
        assertEquals(0, service.savedAssignments.size(), "no new rows for empty list");
        assertEquals(0, service.saveBatchCalls, "saveBatch should not be called for empty list");
    }

    @Test
    public void testAssignOrgsDeduplicatesAndFiltersNull() {
        TestableRoleOrgInfoService service = new TestableRoleOrgInfoService();

        Boolean result = service.assignOrgs(1L, Arrays.asList(20L, 20L, null));

        assertTrue(Boolean.TRUE.equals(result), "should return true");
        assertEquals(1, service.savedAssignments.size(), "duplicate and null should yield one row");
        assertAssignment(service.savedAssignments.get(0), "1", "20", "1");
    }

    @Test
    public void testAssignOrgsNullRoleIdThrows() {
        TestableRoleOrgInfoService service = new TestableRoleOrgInfoService();

        assertThrows(RuntimeException.class, () -> service.assignOrgs(null, Arrays.asList(20L)),
                "null roleId should throw");
    }

    @Test
    public void testAssignOrgsDeclaresTransactional() throws Exception {
        Transactional t = RoleOrgInfoServiceImp.class
                .getMethod("assignOrgs", Long.class, List.class)
                .getAnnotation(Transactional.class);
        assertNotNull(t, "must declare @Transactional");
    }

    private static RoleOrgInfo assignment(Long id, String roleId, String orgId, String status) {
        RoleOrgInfo row = new RoleOrgInfo();
        row.setId(id);
        row.setRoleId(roleId);
        row.setOrgId(orgId);
        row.setStatus(status);
        return row;
    }

    private static void assertAssignment(RoleOrgInfo row, String roleId, String orgId, String status) {
        assertEquals(roleId, row.getRoleId(), "assignment should use requested roleId");
        assertEquals(orgId, row.getOrgId(), "assignment should use requested orgId");
        assertEquals(status, row.getStatus(), "assignment should use requested status");
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

    private static void assertThrows(Class<? extends Throwable> expectedType, ThrowingRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (expectedType.isInstance(throwable)) {
                return;
            }
            throw new AssertionError(message + ", expected exception: " + expectedType.getName()
                    + ", actual: " + throwable.getClass().getName(), throwable);
        }
        throw new AssertionError(message + ", expected exception: " + expectedType.getName());
    }

    private static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }

    private static class TestableRoleOrgInfoService extends RoleOrgInfoServiceImp {

        private final List<RoleOrgInfo> activeAssignments = new ArrayList<>();
        private final List<RoleOrgInfo> savedAssignments = new ArrayList<>();
        private int saveBatchCalls;

        private TestableRoleOrgInfoService(RoleOrgInfo... activeAssignments) {
            super((RoleOrgInfoMapper) null);
            this.activeAssignments.addAll(Arrays.asList(activeAssignments));
        }

        @Override
        public List<RoleOrgInfo> list(Wrapper<RoleOrgInfo> queryWrapper) {
            return new ArrayList<>(activeAssignments);
        }

        @Override
        public boolean updateById(RoleOrgInfo entity) {
            return true;
        }

        @Override
        public boolean saveBatch(Collection<RoleOrgInfo> entityList) {
            saveBatchCalls++;
            savedAssignments.addAll(entityList);
            return true;
        }
    }
}
