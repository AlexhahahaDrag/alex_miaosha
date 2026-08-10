package com.alex.user.rbac;

import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.roleUserInfo.service.impl.RoleUserInfoServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoleUserAssignmentServiceTest {

    @Test
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

    @Test
    public void testAssignRolesDeduplicatesRoleIdsBeforeSaving() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService();

        Boolean result = service.assignRoles(100L, Arrays.asList(2L, 2L, 3L, 3L));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        assertEquals(2, service.savedAssignments.size(), "duplicate roleIds should only be saved once");
        assertAssignment(service.savedAssignments.get(0), "100", "2", "1");
        assertAssignment(service.savedAssignments.get(1), "100", "3", "1");
    }

    @Test
    public void testAssignRolesFiltersNullRoleIdsBeforeSaving() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService();

        Boolean result = service.assignRoles(100L, Arrays.asList(2L, null, 3L, null));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        assertEquals(2, service.savedAssignments.size(), "null roleIds should not be saved");
        assertAssignment(service.savedAssignments.get(0), "100", "2", "1");
        assertAssignment(service.savedAssignments.get(1), "100", "3", "1");
    }

    @Test
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


    @Test
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

    @Test
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

    @Test
    public void testAssignRolesDeclaresTransactionBoundary() throws NoSuchMethodException {
        Transactional transactional = RoleUserInfoServiceImp.class
                .getMethod("assignRoles", Long.class, List.class)
                .getAnnotation(Transactional.class);

        assertNotNull(transactional, "assignRoles should declare a transaction boundary");
    }

    @Test
    public void testAssignUsersToRoleInvalidatesOldActiveUsersAndCreatesNewActiveAssignments() {
        RoleUserInfo oldActiveAssignment = assignment(1L, "100", "2", "1");
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(oldActiveAssignment);

        Boolean result = service.assignUsersToRole(2L, Arrays.asList(101L, 102L));

        assertTrue(Boolean.TRUE.equals(result), "assignUsersToRole should return true");
        assertEquals("0", oldActiveAssignment.getStatus(), "old active role-user assignment should be invalidated");
        assertEquals(1, service.updatedAssignments.size(), "old role-user assignment should be updated once");
        assertEquals(2, service.savedAssignments.size(), "new role-user assignments should be saved");
        assertAssignment(service.savedAssignments.get(0), "101", "2", "1");
        assertAssignment(service.savedAssignments.get(1), "102", "2", "1");
    }

    @Test
    public void testAssignUsersToRoleDeduplicatesAndFiltersNullUserIds() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService();

        Boolean result = service.assignUsersToRole(2L, Arrays.asList(101L, null, 101L, 102L));

        assertTrue(Boolean.TRUE.equals(result), "assignUsersToRole should return true");
        assertEquals(2, service.savedAssignments.size(), "duplicate and null userIds should not be saved");
        assertAssignment(service.savedAssignments.get(0), "101", "2", "1");
        assertAssignment(service.savedAssignments.get(1), "102", "2", "1");
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

    private static class TestableRoleUserInfoService extends RoleUserInfoServiceImp {

        private final List<RoleUserInfo> activeAssignments = new ArrayList<>();
        private final List<RoleUserInfo> updatedAssignments = new ArrayList<>();
        private final List<RoleUserInfo> savedAssignments = new ArrayList<>();
        private boolean updateResult = true;
        private int saveBatchCalls;

        private TestableRoleUserInfoService(RoleUserInfo... activeAssignments) {
            super((RoleUserInfoMapper) null, noOpCache());
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

    /**
     * These tests assert on the DB-write behavior of assignRoles/assignUsersToRole; cache
     * invalidation itself is covered separately by PermissionContextInvalidationTest.
     */
    private static PermissionContextCacheService noOpCache() {
        return new PermissionContextCacheService() {
            @Override
            public void invalidate(Long userId) {
                // no-op for this test's scope
            }

            @Override
            public void invalidateAll(Collection<Long> userIds) {
                // no-op for this test's scope
            }
        };
    }
}
