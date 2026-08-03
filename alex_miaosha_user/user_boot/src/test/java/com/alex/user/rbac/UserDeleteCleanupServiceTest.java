package com.alex.user.rbac;

import com.alex.common.redis.key.KeyPrefix;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
import com.alex.user.rbac.service.impl.UserDeleteCleanupServiceImpl;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.roleUserInfo.service.impl.RoleUserInfoServiceImp;
import com.alex.user.tUserLogin.entity.TUserLogin;
import com.alex.user.tUserLogin.mapper.TUserLoginMapper;
import com.alex.user.tUserLogin.service.impl.TUserLoginServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TDD for user-delete cleanup: invalidate org/role, clear permission_context, kick sessions.
 */
public class UserDeleteCleanupServiceTest {

    @Test
    public void testCleanupInvalidatesActiveOrgAndRoleAssignments() {
        OrgUserInfo org = orgAssignment(1L, "100", "10", "1");
        RoleUserInfo role = roleAssignment(2L, "100", "20", "1");
        UserDeleteCleanupServiceImpl service = newService(
                new TestableOrgUserInfoService(org),
                new TestableRoleUserInfoService(role),
                new TestableTUserLoginService(),
                new RecordingRedisUtils());

        service.cleanupAfterUserDeleted("100");

        assertEquals("0", org.getStatus(), "active org assignment should be invalidated");
        assertEquals("0", role.getStatus(), "active role assignment should be invalidated");
    }

    @Test
    public void testCleanupDeletesPermissionContextKey() {
        RecordingRedisUtils redis = new RecordingRedisUtils();
        UserDeleteCleanupServiceImpl service = newService(
                new TestableOrgUserInfoService(),
                new TestableRoleUserInfoService(),
                new TestableTUserLoginService(),
                redis);

        service.cleanupAfterUserDeleted("100");

        assertTrue(redis.deleted.contains(prefixKey(LoginKey.loginKey, "permission_context:100")),
                "should delete permission_context:{userId}");
    }

    @Test
    public void testCleanupKicksSessionsUsingBarTokenAndOnlineUser() {
        TUserLogin login = login(100L, "tid-1", "db-token");
        RecordingRedisUtils redis = new RecordingRedisUtils();
        redis.store.put(prefixKey(LoginKey.loginUuid, "tid-1"), "bar-token-1");
        UserDeleteCleanupServiceImpl service = newService(
                new TestableOrgUserInfoService(),
                new TestableRoleUserInfoService(),
                new TestableTUserLoginService(login),
                redis);

        service.cleanupAfterUserDeleted("100");

        assertTrue(redis.deleted.contains(prefixKey(LoginKey.loginUuid, "tid-1")),
                "should delete loginUuid by tokenId");
        assertTrue(redis.deleted.contains(prefixKey(LoginKey.loginToken, "bar-token-1")),
                "should delete loginToken by redis barToken");
        assertTrue(redis.deleted.contains(prefixKey(LoginKey.loginOnlineUser, "tid-1")),
                "should delete loginOnlineUser by tokenId");
    }

    @Test
    public void testCleanupFallsBackToLoginTokenWhenBarTokenMissing() {
        TUserLogin login = login(100L, "tid-2", "fallback-token");
        RecordingRedisUtils redis = new RecordingRedisUtils();
        UserDeleteCleanupServiceImpl service = newService(
                new TestableOrgUserInfoService(),
                new TestableRoleUserInfoService(),
                new TestableTUserLoginService(login),
                redis);

        service.cleanupAfterUserDeleted("100");

        assertTrue(redis.deleted.contains(prefixKey(LoginKey.loginUuid, "tid-2")),
                "should delete loginUuid by tokenId");
        assertTrue(redis.deleted.contains(prefixKey(LoginKey.loginToken, "fallback-token")),
                "should fall back to login.token when barToken missing");
        assertTrue(redis.deleted.contains(prefixKey(LoginKey.loginOnlineUser, "tid-2")),
                "should delete loginOnlineUser by tokenId");
    }

    @Test
    public void testCleanupRedisExceptionDoesNotThrow() {
        TUserLogin login = login(100L, "tid-3", "tok");
        ThrowingRedisUtils redis = new ThrowingRedisUtils();
        UserDeleteCleanupServiceImpl service = newService(
                new TestableOrgUserInfoService(),
                new TestableRoleUserInfoService(),
                new TestableTUserLoginService(login),
                redis);

        try {
            service.cleanupAfterUserDeleted("100");
        } catch (Exception e) {
            throw new AssertionError("redis failures must be swallowed", e);
        }
    }

    @Test
    public void testDeleteTUserDeclaresTransactional() throws Exception {
        Transactional t = com.alex.user.user.service.impl.TUserServiceImpl.class
                .getMethod("deleteTUser", String.class)
                .getAnnotation(Transactional.class);
        assertNotNull(t, "deleteTUser must declare @Transactional");
        assertTrue(Arrays.asList(t.rollbackFor()).contains(Exception.class),
                "deleteTUser must rollbackFor Exception");
    }

    private static UserDeleteCleanupServiceImpl newService(
            TestableOrgUserInfoService orgService,
            TestableRoleUserInfoService roleService,
            TestableTUserLoginService loginService,
            RedisUtils redisUtils) {
        return new UserDeleteCleanupServiceImpl(orgService, roleService, loginService, redisUtils);
    }

    private static OrgUserInfo orgAssignment(Long id, String userId, String orgId, String status) {
        OrgUserInfo row = new OrgUserInfo();
        row.setId(id);
        row.setUserId(userId);
        row.setOrgId(orgId);
        row.setStatus(status);
        return row;
    }

    private static RoleUserInfo roleAssignment(Long id, String userId, String roleId, String status) {
        RoleUserInfo row = new RoleUserInfo();
        row.setId(id);
        row.setUserId(userId);
        row.setRoleId(roleId);
        row.setStatus(status);
        return row;
    }

    private static TUserLogin login(Long userId, String tokenId, String token) {
        TUserLogin row = new TUserLogin();
        row.setUserId(userId);
        row.setTokenId(tokenId);
        row.setToken(token);
        return row;
    }

    private static String prefixKey(KeyPrefix prefix, String key) {
        return prefix.getPrefix() + ":" + key;
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

    private static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new AssertionError(message);
        }
    }

    private static class TestableOrgUserInfoService extends OrgUserInfoServiceImp {
        private final List<OrgUserInfo> active = new ArrayList<>();

        private TestableOrgUserInfoService(OrgUserInfo... rows) {
            super((OrgUserInfoMapper) null, (TransactionTemplate) null);
            active.addAll(Arrays.asList(rows));
        }

        @Override
        public List<OrgUserInfo> list(Wrapper<OrgUserInfo> queryWrapper) {
            return new ArrayList<>(active);
        }

        @Override
        public boolean updateById(OrgUserInfo entity) {
            return true;
        }
    }

    private static class TestableRoleUserInfoService extends RoleUserInfoServiceImp {
        private final List<RoleUserInfo> active = new ArrayList<>();

        private TestableRoleUserInfoService(RoleUserInfo... rows) {
            super((RoleUserInfoMapper) null);
            active.addAll(Arrays.asList(rows));
        }

        @Override
        public List<RoleUserInfo> list(Wrapper<RoleUserInfo> queryWrapper) {
            return new ArrayList<>(active);
        }

        @Override
        public boolean updateById(RoleUserInfo entity) {
            return true;
        }
    }

    private static class TestableTUserLoginService extends TUserLoginServiceImp {
        private final List<TUserLogin> logins = new ArrayList<>();

        private TestableTUserLoginService(TUserLogin... rows) {
            super((TUserLoginMapper) null);
            logins.addAll(Arrays.asList(rows));
        }

        @Override
        public List<TUserLogin> list(Wrapper<TUserLogin> queryWrapper) {
            return new ArrayList<>(logins);
        }
    }

    private static class RecordingRedisUtils extends RedisUtils {
        private final Map<String, String> store = new HashMap<>();
        private final List<String> deleted = new ArrayList<>();

        private RecordingRedisUtils() {
            super(null);
        }

        @Override
        public String get(KeyPrefix prefix, String key) {
            return store.get(prefixKey(prefix, key));
        }

        @Override
        public void delete(KeyPrefix prefix, String key) {
            deleted.add(prefixKey(prefix, key));
        }
    }

    private static class ThrowingRedisUtils extends RedisUtils {
        private ThrowingRedisUtils() {
            super(null);
        }

        @Override
        public String get(KeyPrefix prefix, String key) {
            throw new RuntimeException("redis get failed");
        }

        @Override
        public void delete(KeyPrefix prefix, String key) {
            throw new RuntimeException("redis delete failed");
        }
    }
}
