package com.alex.user.rbac;

import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.roleUserInfo.service.impl.RoleUserInfoServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;

/**
 * RBAC-BE-RELATION-004 对称点：与 OrgUserInfoServiceImp 一致，assignRoles 成功后
 * 也需清理该用户堆积的失效（status=0）角色关系历史行，仅保留最近 5 条。
 * assignUsersToRole 一次会影响多个不同用户，不属于"该 userId"维度的对称点，不在本测试范围内。
 */
@ExtendWith(MockitoExtension.class)
public class RoleUserHistoryPruneTest {

    @Mock
    private RoleUserInfoMapper roleUserInfoMapper;

    @Test
    void assignRoles_prunesInactiveHistory_keepsOnlyNewestFive() {
        List<RoleUserInfo> invalidHistory = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        for (long id = 1; id <= 10; id++) {
            invalidHistory.add(invalidRow(id, base.plusMinutes(id)));
        }

        PruneTestableRoleUserInfoService service =
                new PruneTestableRoleUserInfoService(roleUserInfoMapper, invalidHistory);

        Boolean result = service.assignRoles(100L, Arrays.asList(2L, 3L));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should still return true");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> captor = ArgumentCaptor.forClass(Collection.class);
        org.mockito.Mockito.verify(roleUserInfoMapper).deleteBatchIds(captor.capture());
        Collection<Long> deletedIds = captor.getValue();

        assertEquals(5, deletedIds.size(), "must delete exactly the 5 oldest inactive rows (10 - keep 5)");
        assertTrue(deletedIds.containsAll(List.of(1L, 2L, 3L, 4L, 5L)),
                "the 5 oldest rows (ids 1-5) must be deleted, actual=" + deletedIds);
        for (Long keptId : List.of(6L, 7L, 8L, 9L, 10L)) {
            assertTrue(!deletedIds.contains(keptId), "newest 5 rows must be kept, but " + keptId + " was deleted");
        }
    }

    @Test
    void assignRoles_doesNotPrune_whenInactiveHistoryWithinLimit() {
        List<RoleUserInfo> invalidHistory = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        for (long id = 1; id <= 5; id++) {
            invalidHistory.add(invalidRow(id, base.plusMinutes(id)));
        }

        PruneTestableRoleUserInfoService service =
                new PruneTestableRoleUserInfoService(roleUserInfoMapper, invalidHistory);

        Boolean result = service.assignRoles(100L, Collections.emptyList());

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should still return true");
        org.mockito.Mockito.verify(roleUserInfoMapper, never())
                .deleteBatchIds(org.mockito.ArgumentMatchers.any());
    }

    private static RoleUserInfo invalidRow(Long id, LocalDateTime createTime) {
        RoleUserInfo row = new RoleUserInfo();
        row.setId(id);
        row.setUserId("100");
        row.setRoleId("1");
        row.setStatus("0");
        row.setCreateTime(createTime);
        return row;
    }

    private static class PruneTestableRoleUserInfoService extends RoleUserInfoServiceImp {

        private final List<RoleUserInfo> invalidHistory;

        private PruneTestableRoleUserInfoService(RoleUserInfoMapper mapper, List<RoleUserInfo> invalidHistory) {
            super(mapper, noOpCache());
            this.invalidHistory = invalidHistory;
        }

        @Override
        public List<RoleUserInfo> list(com.baomidou.mybatisplus.core.conditions.Wrapper<RoleUserInfo> queryWrapper) {
            // assignRoles 内部的"当前有效行"查询与 listInvalidHistory 都会走到这里；
            // 本测试无当前有效行需要失效，直接复用同一 seam 返回失效历史堆积数据即可，
            // 因为 assignRoles 在两处调用之间不依赖该返回值的可变身份。
            return new ArrayList<>(invalidHistory);
        }

        @Override
        protected List<RoleUserInfo> listInvalidHistory(Long userId) {
            return new ArrayList<>(invalidHistory);
        }

        @Override
        public boolean updateById(RoleUserInfo entity) {
            return true;
        }

        @Override
        public boolean saveBatch(Collection<RoleUserInfo> entityList) {
            return true;
        }
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
