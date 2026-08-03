package com.alex.user.rbac;

import com.alex.user.rolePermissionInfo.entity.RolePermissionInfo;
import com.alex.user.rolePermissionInfo.mapper.RolePermissionInfoMapper;
import com.alex.user.rolePermissionInfo.service.impl.RolePermissionInfoServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RolePermissionAssignmentServiceTest {

    @Test
    public void testAssignPermissionsInvalidatesOldAndCreatesNew() {
        RolePermissionInfo old = assignment(1L, "10", "100", "1");
        TestableRolePermissionInfoService service = new TestableRolePermissionInfoService(old);

        Boolean result = service.assignPermissions(10L, Arrays.asList(200L, 300L));

        assertTrue(Boolean.TRUE.equals(result), "should return true");
        assertEquals("0", old.getStatus(), "old active should be invalidated");
        assertEquals(2, service.savedAssignments.size(), "two new rows");
        assertAssignment(service.savedAssignments.get(0), "10", "200", "1");
        assertAssignment(service.savedAssignments.get(1), "10", "300", "1");
    }

    @Test
    public void testAssignPermissionsEmptyOnlyInvalidates() {
        RolePermissionInfo old = assignment(1L, "10", "100", "1");
        TestableRolePermissionInfoService service = new TestableRolePermissionInfoService(old);

        Boolean result = service.assignPermissions(10L, Collections.emptyList());

        assertTrue(Boolean.TRUE.equals(result), "should return true");
        assertEquals("0", old.getStatus(), "old active should be invalidated");
        assertEquals(0, service.savedAssignments.size(), "no new rows for empty list");
        assertEquals(0, service.saveBatchCalls, "saveBatch should not be called for empty list");
    }

    @Test
    public void testAssignPermissionsDeduplicatesAndFiltersNull() {
        TestableRolePermissionInfoService service = new TestableRolePermissionInfoService();

        Boolean result = service.assignPermissions(10L, Arrays.asList(200L, 200L, null));

        assertTrue(Boolean.TRUE.equals(result), "should return true");
        assertEquals(1, service.savedAssignments.size(), "duplicate and null should yield one row");
        assertAssignment(service.savedAssignments.get(0), "10", "200", "1");
    }

    @Test
    public void testAssignPermissionsNullRoleIdThrows() {
        TestableRolePermissionInfoService service = new TestableRolePermissionInfoService();

        assertThrows(RuntimeException.class, () -> service.assignPermissions(null, Arrays.asList(200L)),
                "null roleId should throw");
    }

    @Test
    public void testAssignPermissionsDeclaresTransactional() throws Exception {
        Transactional t = RolePermissionInfoServiceImp.class
                .getMethod("assignPermissions", Long.class, List.class)
                .getAnnotation(Transactional.class);
        assertNotNull(t, "must declare @Transactional");
    }

    private static RolePermissionInfo assignment(Long id, String roleId, String permissionId, String status) {
        RolePermissionInfo row = new RolePermissionInfo();
        row.setId(id);
        row.setRoleId(roleId);
        row.setPermissionId(permissionId);
        row.setStatus(status);
        return row;
    }

    private static void assertAssignment(RolePermissionInfo row, String roleId, String permissionId, String status) {
        assertEquals(roleId, row.getRoleId(), "assignment should use requested roleId");
        assertEquals(permissionId, row.getPermissionId(), "assignment should use requested permissionId");
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

    private static class TestableRolePermissionInfoService extends RolePermissionInfoServiceImp {

        private final List<RolePermissionInfo> activeAssignments = new ArrayList<>();
        private final List<RolePermissionInfo> savedAssignments = new ArrayList<>();
        private int saveBatchCalls;

        private TestableRolePermissionInfoService(RolePermissionInfo... activeAssignments) {
            super((RolePermissionInfoMapper) null);
            this.activeAssignments.addAll(Arrays.asList(activeAssignments));
        }

        @Override
        public List<RolePermissionInfo> list(Wrapper<RolePermissionInfo> queryWrapper) {
            return new ArrayList<>(activeAssignments);
        }

        @Override
        public boolean updateById(RolePermissionInfo entity) {
            return true;
        }

        @Override
        public boolean saveBatch(Collection<RolePermissionInfo> entityList) {
            saveBatchCalls++;
            savedAssignments.addAll(entityList);
            return true;
        }
    }
}
