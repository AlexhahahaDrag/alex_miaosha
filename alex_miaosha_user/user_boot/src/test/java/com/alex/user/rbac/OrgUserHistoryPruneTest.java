package com.alex.user.rbac;

import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
import com.alex.user.rbac.service.PermissionContextCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * RBAC-BE-RELATION-004: assignSingleOrg 成功后，须清理该用户堆积的失效（status=0）历史行，
 * 仅保留最近 N=5 条，更早的行需删除（entity 带 {@code @TableLogic}，等效逻辑删）。
 * 有效行数量始终 ≤1 的既有语义不受影响（不在本测试重复验证，已由
 * {@link OrgUserAssignmentServiceTest}/{@link OrgUserAssignGuardTest} 覆盖）。
 */
@ExtendWith(MockitoExtension.class)
public class OrgUserHistoryPruneTest {

    @Mock
    private OrgUserInfoMapper orgUserInfoMapper;

    @Test
    void assignSingleOrg_prunesInactiveHistory_keepsOnlyNewestFive() {
        // 模拟该用户名下已堆积 10 条失效历史行，id 越大代表 createTime 越新
        List<OrgUserInfo> invalidHistory = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        for (long id = 1; id <= 10; id++) {
            invalidHistory.add(invalidRow(id, base.plusMinutes(id)));
        }

        PruneTestableOrgUserInfoService service =
                new PruneTestableOrgUserInfoService(orgUserInfoMapper, invalidHistory);

        Boolean result = service.assignSingleOrg(100L, 20L);

        assertTrue(Boolean.TRUE.equals(result), "assignSingleOrg should still return true");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(orgUserInfoMapper).deleteBatchIds(captor.capture());
        Collection<Long> deletedIds = captor.getValue();

        assertEquals(5, deletedIds.size(), "must delete exactly the 5 oldest inactive rows (10 - keep 5)");
        assertTrue(deletedIds.containsAll(List.of(1L, 2L, 3L, 4L, 5L)),
                "the 5 oldest rows (ids 1-5) must be deleted, actual=" + deletedIds);
        for (Long keptId : List.of(6L, 7L, 8L, 9L, 10L)) {
            assertTrue(!deletedIds.contains(keptId), "newest 5 rows must be kept, but " + keptId + " was deleted");
        }
    }

    @Test
    void assignSingleOrg_doesNotPrune_whenInactiveHistoryWithinLimit() {
        List<OrgUserInfo> invalidHistory = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        for (long id = 1; id <= 5; id++) {
            invalidHistory.add(invalidRow(id, base.plusMinutes(id)));
        }

        PruneTestableOrgUserInfoService service =
                new PruneTestableOrgUserInfoService(orgUserInfoMapper, invalidHistory);

        Boolean result = service.assignSingleOrg(100L, 20L);

        assertTrue(Boolean.TRUE.equals(result), "assignSingleOrg should still return true");
        verify(orgUserInfoMapper, never()).deleteBatchIds(org.mockito.ArgumentMatchers.any());
    }

    private static OrgUserInfo invalidRow(Long id, LocalDateTime createTime) {
        OrgUserInfo row = new OrgUserInfo();
        row.setId(id);
        row.setUserId("100");
        row.setOrgId("10");
        row.setStatus("0");
        row.setCreateTime(createTime);
        return row;
    }

    /**
     * 只覆盖 pruning 相关的两个查询入口：
     * - listActiveAssignments：无当前有效行，避免干扰本测试关注点
     * - listInvalidHistory：返回预置的失效历史堆积数据
     * save/updateById 走最小可用桩，deleteBatchIds 走真实 mock 以便断言调用参数。
     */
    private static class PruneTestableOrgUserInfoService extends OrgUserInfoServiceImp {

        private final List<OrgUserInfo> invalidHistory;

        private PruneTestableOrgUserInfoService(OrgUserInfoMapper mapper, List<OrgUserInfo> invalidHistory) {
            super(mapper, noOpTransactionTemplate(), noOpCache());
            this.invalidHistory = invalidHistory;
        }

        @Override
        protected List<OrgUserInfo> listActiveAssignments(Long userId) {
            return new ArrayList<>();
        }

        @Override
        protected List<OrgUserInfo> listInvalidHistory(Long userId) {
            return new ArrayList<>(invalidHistory);
        }

        @Override
        public boolean save(OrgUserInfo entity) {
            return true;
        }

        @Override
        public boolean updateById(OrgUserInfo entity) {
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
