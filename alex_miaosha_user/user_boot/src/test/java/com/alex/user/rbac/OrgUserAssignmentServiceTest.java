package com.alex.user.rbac;

import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrgUserAssignmentServiceTest {

    @Test
    public void testAssignSingleOrgInvalidatesOldActiveAssignmentAndCreatesNewActiveAssignment() {
        OrgUserInfo oldActiveAssignment = assignment(1L, "100", "10", "1");
        TestableOrgUserInfoService service = new TestableOrgUserInfoService(oldActiveAssignment);

        Boolean result = service.assignSingleOrg(100L, 20L);

        assertTrue(Boolean.TRUE.equals(result), "assignSingleOrg should return true");
        assertEquals("0", oldActiveAssignment.getStatus(), "old active assignment should be invalidated");
        assertEquals(1, service.updatedAssignments.size(), "old active assignment should be updated once");
        assertSame(oldActiveAssignment, service.updatedAssignments.get(0), "updated assignment should be the old active assignment");
        assertNotNull(service.savedAssignment, "new assignment should be saved");
        assertEquals("100", service.savedAssignment.getUserId(), "new assignment should use requested userId");
        assertEquals("20", service.savedAssignment.getOrgId(), "new assignment should use requested orgId");
        assertEquals("1", service.savedAssignment.getStatus(), "new assignment should be active");
    }

    @Test
    public void testAssignSingleOrgDoesNotSaveWhenInvalidatingOldAssignmentFails() {
        OrgUserInfo oldActiveAssignment = assignment(1L, "100", "10", "1");
        TestableOrgUserInfoService service = new TestableOrgUserInfoService(oldActiveAssignment);
        service.updateResult = false;

        assertThrows(RuntimeException.class, () -> service.assignSingleOrg(100L, 20L),
                "assignSingleOrg should fail when invalidating old assignment fails");
        assertEquals(1, service.updatedAssignments.size(), "old active assignment should be updated once");
        assertNull(service.savedAssignment, "new assignment should not be saved after update failure");
    }

    @Test
    public void testAssignSingleOrgDeclaresTransactionBoundary() throws NoSuchMethodException {
        Transactional transactional = OrgUserInfoServiceImp.class
                .getMethod("assignSingleOrg", Long.class, Long.class)
                .getAnnotation(Transactional.class);

        assertNull(transactional, "assignSingleOrg should let the per-user lock wrap the explicit transaction boundary");
    }

    @Test
    public void testAssignSingleOrgHoldsUserLockUntilTransactionCallbackCompletes() throws InterruptedException {
        BlockingTransactionTemplate transactionTemplate = new BlockingTransactionTemplate();
        BlockingOrgUserInfoService service = new BlockingOrgUserInfoService(transactionTemplate);

        Thread first = new Thread(() -> service.assignSingleOrg(100L, 20L), "assign-org-first");
        Thread second = new Thread(() -> service.assignSingleOrg(100L, 30L), "assign-org-second");

        first.start();
        assertTrue(transactionTemplate.firstCallbackEntered.await(2, TimeUnit.SECONDS), "first assignment should enter transaction callback");
        second.start();
        sleep(120L);

        assertEquals(1, transactionTemplate.callbackEntries.get(),
                "second assignment for same user should wait until first transaction callback completes");
        transactionTemplate.releaseFirstCallback.countDown();
        first.join(2000L);
        second.join(2000L);
        assertTrue(!first.isAlive() && !second.isAlive(), "assignment threads should finish");
        assertEquals(2, transactionTemplate.callbackEntries.get(), "both assignments should run after serialization");
        assertEquals(2, service.listCalls.get(), "both assignments should enter core assignment");
    }

    private static OrgUserInfo assignment(Long id, String userId, String orgId, String status) {
        OrgUserInfo orgUserInfo = new OrgUserInfo();
        orgUserInfo.setId(id);
        orgUserInfo.setUserId(userId);
        orgUserInfo.setOrgId(orgId);
        orgUserInfo.setStatus(status);
        return orgUserInfo;
    }

    private static void sleep(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("test sleep was interrupted");
        }
    }

    private static class TestableOrgUserInfoService extends OrgUserInfoServiceImp {

        private final List<OrgUserInfo> activeAssignments = new ArrayList<>();
        private final List<OrgUserInfo> updatedAssignments = new ArrayList<>();
        private boolean updateResult = true;
        private OrgUserInfo savedAssignment;

        private TestableOrgUserInfoService(OrgUserInfo activeAssignment) {
            super((OrgUserInfoMapper) null, noOpTransactionTemplate());
            this.activeAssignments.add(activeAssignment);
        }

        @Override
        public List<OrgUserInfo> list(Wrapper<OrgUserInfo> queryWrapper) {
            return new ArrayList<>(activeAssignments);
        }

        @Override
        public boolean updateById(OrgUserInfo entity) {
            updatedAssignments.add(entity);
            return updateResult;
        }

        @Override
        public boolean save(OrgUserInfo entity) {
            savedAssignment = entity;
            return true;
        }
    }

    private static TransactionTemplate noOpTransactionTemplate() {
        return new TransactionTemplate((PlatformTransactionManager) null) {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        };
    }

    private static class BlockingTransactionTemplate extends TransactionTemplate {

        private final AtomicInteger callbackEntries = new AtomicInteger();
        private final CountDownLatch firstCallbackEntered = new CountDownLatch(1);
        private final CountDownLatch releaseFirstCallback = new CountDownLatch(1);

        private BlockingTransactionTemplate() {
            super((PlatformTransactionManager) null);
        }

        @Override
        public <T> T execute(TransactionCallback<T> action) {
            int entry = callbackEntries.incrementAndGet();
            if (entry == 1) {
                firstCallbackEntered.countDown();
                assertTrue(await(releaseFirstCallback, 2, TimeUnit.SECONDS), "first transaction callback should be released");
            }
            return action.doInTransaction(null);
        }
    }

    private static class BlockingOrgUserInfoService extends OrgUserInfoServiceImp {

        private final AtomicInteger listCalls = new AtomicInteger();

        private BlockingOrgUserInfoService(TransactionTemplate transactionTemplate) {
            super((OrgUserInfoMapper) null, transactionTemplate);
        }

        @Override
        public List<OrgUserInfo> list(Wrapper<OrgUserInfo> queryWrapper) {
            listCalls.incrementAndGet();
            return new ArrayList<>();
        }

        @Override
        public boolean save(OrgUserInfo entity) {
            return true;
        }
    }

    private static boolean await(CountDownLatch latch, long timeout, TimeUnit unit) {
        try {
            return latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("test latch wait was interrupted");
        }
    }
}
