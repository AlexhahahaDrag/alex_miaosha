package com.alex.user.rbac;

import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.roleUserInfo.service.impl.RoleUserInfoServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * RBAC-BE-RELATION-002 + RBAC-BE-SCOPE-004:
 * 机构/角色 assign 成功后必须主动失效 permission_context 缓存，不能只靠 1 小时 TTL 兜底。
 */
@ExtendWith(MockitoExtension.class)
public class PermissionContextInvalidationTest {

    @Mock
    private OrgUserInfoMapper orgUserInfoMapper;
    @Mock
    private RoleUserInfoMapper roleUserInfoMapper;
    @Mock
    private PermissionContextCacheService permissionContextCacheService;

    @Test
    void assignSingleOrg_invalidatesPermissionContext_afterSuccess() {
        TestableOrgUserInfoService service = new TestableOrgUserInfoService(permissionContextCacheService);

        Boolean result = service.assignSingleOrg(100L, 20L);

        assertTrue(Boolean.TRUE.equals(result), "assignSingleOrg should return true");
        verify(permissionContextCacheService).invalidate(100L);
    }

    @Test
    void assignSingleOrg_doesNotInvalidate_whenSaveFails() {
        // 反例：新关系保存失败时不应失效缓存，避免"失败也清缓存"造成缓存被误清后又长时间不重建
        FailingSaveOrgUserInfoService service = new FailingSaveOrgUserInfoService(permissionContextCacheService);

        try {
            service.assignSingleOrg(100L, 20L);
        } catch (RuntimeException ignored) {
            // 预期抛出 SystemException：新关系保存失败
        }

        verify(permissionContextCacheService, never()).invalidate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void assignRoles_invalidatesPermissionContext_afterSuccess() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(permissionContextCacheService);

        Boolean result = service.assignRoles(100L, Arrays.asList(2L, 3L));

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true");
        verify(permissionContextCacheService).invalidate(100L);
    }

    @Test
    void assignRoles_invalidatesPermissionContext_evenWhenRoleIdsEmpty() {
        // 角色被清空也是一种"变更"，同样要失效缓存，不能只有"新增角色"才失效
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(permissionContextCacheService);

        Boolean result = service.assignRoles(100L, Collections.emptyList());

        assertTrue(Boolean.TRUE.equals(result), "assignRoles should return true even for empty roleIds");
        verify(permissionContextCacheService).invalidate(100L);
    }

    @Test
    void assignUsersToRole_invalidatesPermissionContext_forEachAssignedUser() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService(permissionContextCacheService);

        Boolean result = service.assignUsersToRole(2L, Arrays.asList(101L, 102L));

        assertTrue(Boolean.TRUE.equals(result), "assignUsersToRole should return true");
        verify(permissionContextCacheService).invalidateAll(argThat((Collection<Long> ids) ->
                ids != null && ids.size() == 2 && ids.contains(101L) && ids.contains(102L)));
    }

    @Test
    void assignUsersToRole_invalidatesRemovedUsers_whenReplaceDropsUser() {
        List<RoleUserInfo> existing = Arrays.asList(
                roleUserBinding(101L, 2L),
                roleUserBinding(102L, 2L));
        TestableRoleUserInfoServiceWithBindings service =
                new TestableRoleUserInfoServiceWithBindings(permissionContextCacheService, existing);

        Boolean result = service.assignUsersToRole(2L, Collections.singletonList(101L));

        assertTrue(Boolean.TRUE.equals(result), "assignUsersToRole should return true");
        verify(permissionContextCacheService).invalidateAll(argThat((Collection<Long> ids) ->
                ids != null && ids.size() == 2 && ids.contains(101L) && ids.contains(102L)));
    }

    @Test
    void assignUsersToRole_invalidatesOldUsers_whenClearBindings() {
        List<RoleUserInfo> existing = Arrays.asList(
                roleUserBinding(101L, 2L),
                roleUserBinding(102L, 2L));
        TestableRoleUserInfoServiceWithBindings service =
                new TestableRoleUserInfoServiceWithBindings(permissionContextCacheService, existing);

        Boolean result = service.assignUsersToRole(2L, Collections.emptyList());

        assertTrue(Boolean.TRUE.equals(result), "assignUsersToRole should return true");
        verify(permissionContextCacheService).invalidateAll(argThat((Collection<Long> ids) ->
                ids != null && ids.size() == 2 && ids.contains(101L) && ids.contains(102L)));
    }

    private static RoleUserInfo roleUserBinding(long userId, long roleId) {
        RoleUserInfo binding = new RoleUserInfo();
        binding.setUserId(String.valueOf(userId));
        binding.setRoleId(String.valueOf(roleId));
        binding.setStatus(com.alex.base.constants.SysConf.VALID_STATUS);
        return binding;
    }

    private static class TestableOrgUserInfoService extends OrgUserInfoServiceImp {

        private TestableOrgUserInfoService(PermissionContextCacheService cache) {
            super(null, noOpTransactionTemplate(), cache);
        }

        @Override
        public List<OrgUserInfo> list(Wrapper<OrgUserInfo> queryWrapper) {
            return new ArrayList<>();
        }

        @Override
        public boolean save(OrgUserInfo entity) {
            return true;
        }
    }

    private static class FailingSaveOrgUserInfoService extends OrgUserInfoServiceImp {

        private FailingSaveOrgUserInfoService(PermissionContextCacheService cache) {
            super(null, noOpTransactionTemplate(), cache);
        }

        @Override
        public List<OrgUserInfo> list(Wrapper<OrgUserInfo> queryWrapper) {
            return new ArrayList<>();
        }

        @Override
        public boolean save(OrgUserInfo entity) {
            return false;
        }
    }

    private static class TestableRoleUserInfoService extends RoleUserInfoServiceImp {

        private TestableRoleUserInfoService(PermissionContextCacheService cache) {
            super(null, cache);
        }

        @Override
        public List<RoleUserInfo> list(Wrapper<RoleUserInfo> queryWrapper) {
            return new ArrayList<>();
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

    private static class TestableRoleUserInfoServiceWithBindings extends RoleUserInfoServiceImp {

        private final List<RoleUserInfo> activeBindings;

        private TestableRoleUserInfoServiceWithBindings(
                PermissionContextCacheService cache, List<RoleUserInfo> activeBindings) {
            super(null, cache);
            this.activeBindings = activeBindings;
        }

        @Override
        public List<RoleUserInfo> list(Wrapper<RoleUserInfo> queryWrapper) {
            return new ArrayList<>(activeBindings);
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

    private static TransactionTemplate noOpTransactionTemplate() {
        return new TransactionTemplate((PlatformTransactionManager) null) {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        };
    }
}
