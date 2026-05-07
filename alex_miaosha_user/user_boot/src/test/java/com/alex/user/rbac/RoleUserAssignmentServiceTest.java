package com.alex.user.rbac;

import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.roleUserInfo.service.impl.RoleUserInfoServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RoleUserAssignmentServiceTest {

    public void testAssignRolesInvalidatesOldActiveAssignmentAndCreatesNewActiveAssignments() {
        RoleUserInfo oldActiveAssignment = assignment(1L, "100", "1", "1");
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(oldActiveAssignment);

        Boolean result = service.assignRoles(100L, Arrays.asList(2L, 3L));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        assertEquals("0", oldActiveAssignment.getStatus(), "old active assignment should be invalidated");
        assertEquals(1, service.updatedAssignments.size(), "old active assignment should be updated once");
        assertSame(oldActiveAssignment, service.updatedAssignments.get(0), "updated assignment should be the old active assignment");
        assertEquals(2, service.savedAssignments.size(), "new assignments should be saved");
        assertAssignment(service.savedAssignments.get(0), "100", "2", "1");
        assertAssignment(service.savedAssignments.get(1), "100", "3", "1");
    }

    public void testAssignRolesDeduplicatesRoleIdsBeforeSaving() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService();

        Boolean result = service.assignRoles(100L, Arrays.asList(2L, 2L, 3L, 3L));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        assertEquals(2, service.savedAssignments.size(), "duplicate roleIds should only be saved once");
        assertAssignment(service.savedAssignments.get(0), "100", "2", "1");
        assertAssignment(service.savedAssignments.get(1), "100", "3", "1");
    }

    public void testAssignRolesFiltersNullRoleIdsBeforeSaving() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService();

        Boolean result = service.assignRoles(100L, Arrays.asList(2L, null, 3L, null));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        assertEquals(2, service.savedAssignments.size(), "null roleIds should not be saved");
        assertAssignment(service.savedAssignments.get(0), "100", "2", "1");
        assertAssignment(service.savedAssignments.get(1), "100", "3", "1");
    }

    public void testAssignRolesWithOnlyNullRoleIdsOnlyInvalidatesOldActiveAssignments() {
        RoleUserInfo oldActiveAssignment = assignment(1L, "100", "1", "1");
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(oldActiveAssignment);

        Boolean result = service.assignRoles(100L, Arrays.asList(null, null));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        assertEquals("0", oldActiveAssignment.getStatus(), "old active assignment should be invalidated");
        assertEquals(1, service.updatedAssignments.size(), "old active assignment should be updated once");
        assertEquals(0, service.savedAssignments.size(), "no assignments should be saved for only null roleIds");
        assertEquals(0, service.saveBatchCalls, "saveBatch should not be called for only null roleIds");
    }


    public void testAssignRolesWithEmptyRoleIdsOnlyInvalidatesOldActiveAssignments() {
        RoleUserInfo oldActiveAssignment = assignment(1L, "100", "1", "1");
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(oldActiveAssignment);

        Boolean result = service.assignRoles(100L, Collections.emptyList());

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        assertEquals("0", oldActiveAssignment.getStatus(), "old active assignment should be invalidated");
        assertEquals(1, service.updatedAssignments.size(), "old active assignment should be updated once");
        assertEquals(0, service.savedAssignments.size(), "no new assignments should be saved for empty roleIds");
        assertEquals(0, service.saveBatchCalls, "saveBatch should not be called for empty roleIds");
    }

    public void testAssignRolesDoesNotSaveWhenInvalidatingOldAssignmentFails() {
        RoleUserInfo oldActiveAssignment = assignment(1L, "100", "1", "1");
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(oldActiveAssignment);
        service.updateResult = false;

        assertThrows(RuntimeException.class, () -> service.assignRoles(100L, Arrays.asList(2L, 3L)),
                "assignRoles should fail when invalidating old assignment fails");
        assertEquals(1, service.updatedAssignments.size(), "old active assignment should be updated once");
        assertEquals(0, service.savedAssignments.size(), "new assignments should not be saved after update failure");
        assertEquals(0, service.saveBatchCalls, "saveBatch should not be called after update failure");
    }

    public void testAssignRolesDeclaresTransactionBoundary() throws NoSuchMethodException {
        Transactional transactional = RoleUserInfoServiceImp.class
                .getMethod("assignRoles", Long.class, List.class)
                .getAnnotation(Transactional.class);

        assertNotNull(transactional, "assignRoles should declare a transaction boundary");
    }

    private static RoleUserInfo assignment(Long id, String userId, String roleId, String status) {
        RoleUserInfo roleUserInfo = new RoleUserInfo();
        roleUserInfo.setId(id);
        roleUserInfo.setUserId(userId);
        roleUserInfo.setRoleId(roleId);
        roleUserInfo.setStatus(status);
        return roleUserInfo;
    }

    private static void assertAssignment(RoleUserInfo roleUserInfo, String userId, String roleId, String status) {
        assertEquals(userId, roleUserInfo.getUserId(), "assignment should use requested userId");
        assertEquals(roleId, roleUserInfo.getRoleId(), "assignment should use requested roleId");
        assertEquals(status, roleUserInfo.getStatus(), "assignment should use requested status");
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

    private static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
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

    private static class TestableRoleUserInfoService extends RoleUserInfoServiceImp {

        private final List<RoleUserInfo> activeAssignments = new ArrayList<>();
        private final List<RoleUserInfo> updatedAssignments = new ArrayList<>();
        private final List<RoleUserInfo> savedAssignments = new ArrayList<>();
        private boolean updateResult = true;
        private int saveBatchCalls;

        private TestableRoleUserInfoService(RoleUserInfo... activeAssignments) {
            super((RoleUserInfoMapper) null);
            this.activeAssignments.addAll(Arrays.asList(activeAssignments));
        }

        @Override
        public List<RoleUserInfo> list(Wrapper<RoleUserInfo> queryWrapper) {
            return new ArrayList<>(activeAssignments);
        }

        @Override
        public boolean updateById(RoleUserInfo entity) {
            updatedAssignments.add(entity);
            return updateResult;
        }

        @Override
        public boolean saveBatch(Collection<RoleUserInfo> entityList) {
            saveBatchCalls++;
            savedAssignments.addAll(entityList);
            return true;
        }
    }
}
