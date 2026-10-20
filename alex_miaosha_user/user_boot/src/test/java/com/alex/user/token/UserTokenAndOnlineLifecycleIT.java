package com.alex.user.token;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.online.service.OnlineUserService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.rbac.service.UserDeleteCleanupService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alex.user.tUserLogin.entity.TUserLogin;
import com.alex.user.token.service.TokenRefreshService;
import com.alex.user.user.mapper.TUserMapper;
import com.alex.user.user.service.impl.TUserServiceImpl;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 集成测试套件 1:
 * 用户 Token 刷新机制、在线会话管理时效与登出联动测试
 */
@ExtendWith(MockitoExtension.class)
public class UserTokenAndOnlineLifecycleIT {

    @Mock
    private RedisUtils redisUtils;
    @Mock
    private JwtTokenUtils jwtTokenUtils;
    @Mock
    private Audience audience;
    @Mock
    private TUserMapper tUserMapper;
    @Mock
    private OssApi ossApi;
    @Mock
    private UserUtils userUtils;
    @Mock
    private OrgUserInfoService orgUserInfoService;
    @Mock
    private RoleUserInfoService roleUserInfoService;
    @Mock
    private RoleInfoMapper roleInfoMapper;
    @Mock
    private UserPermissionContextService userPermissionContextService;
    @Mock
    private UserDeleteCleanupService userDeleteCleanupService;
    @Mock
    private PermissionContextCacheService permissionContextCacheService;
    @Mock
    private Executor asyncTaskExecutor;

    private OnlineUserService onlineUserService;
    private TokenRefreshService tokenRefreshService;
    private TUserServiceImpl userService;

    @BeforeEach
    void setUp() {
        onlineUserService = new OnlineUserService(redisUtils);
        tokenRefreshService = new TokenRefreshService(redisUtils, jwtTokenUtils, audience);
        userService = new TUserServiceImpl(
                tUserMapper,
                redisUtils,
                jwtTokenUtils,
                audience,
                ossApi,
                userUtils,
                orgUserInfoService,
                roleUserInfoService,
                roleInfoMapper,
                tokenRefreshService,
                onlineUserService,
                asyncTaskExecutor,
                userPermissionContextService,
                userDeleteCleanupService,
                permissionContextCacheService,
                null
        );
    }

    @Test
    @DisplayName("OnlineUserService: 在线用户过期时间必须以秒级累加计算，杜绝微秒Bug")
    void onlineUserService_buildAdmin_expireTimeInSeconds() {
        long expirationSeconds = 7200L;
        TUserLogin login = new TUserLogin();
        login.setUserId(1001L);
        login.setUsername("test_user");
        login.setTokenId("token-uuid-123");
        login.setToken("Bearer jwt-token-xyz");
        login.setLoginIp("127.0.0.1");
        login.setLastLoginTime(LocalDateTime.now());

        when(redisUtils.get(eq(LoginKey.loginIpSource), anyString())).thenReturn("内网IP");

        onlineUserService.addOnlineUserAsync(login, expirationSeconds);

        ArgumentCaptor<String> onlineAdminCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisUtils).setEx(eq(LoginKey.loginOnlineUser), eq("token-uuid-123"),
                onlineAdminCaptor.capture(), eq(7200L), eq(TimeUnit.SECONDS));

        String adminStr = onlineAdminCaptor.getValue();
        assertNotNull(adminStr);
        // expireTime 应该存在且与当前时间的差距在 ~7200 秒左右，而不是相差不到 1 秒
        assertTrue(adminStr.contains("expireTime"));
        assertTrue(adminStr.contains("test_user"));
    }

    @Test
    @DisplayName("登出联动: 登出时必须同时清理 loginUuid, loginToken 与 loginOnlineUser")
    void logout_cleansUuidTokenAndOnlineUser() {
        when(audience.getTokenHeader()).thenReturn("Authorization");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "uuid-token-888");

        when(redisUtils.get(LoginKey.loginUuid, "uuid-token-888")).thenReturn("jwt-bar-token-999");

        Result<Boolean> result = userService.logout(request);

        assertNotNull(result);
        assertEquals(ResultEnum.SUCCESS.getCode(), result.getCode());

        // 验证清理了 loginUuid
        verify(redisUtils).delete(LoginKey.loginUuid, "uuid-token-888");
        // 验证清理了 loginToken
        verify(redisUtils).delete(LoginKey.loginToken, "jwt-bar-token-999");
        // 验证联动清理了 loginOnlineUser
        verify(redisUtils).delete(LoginKey.loginOnlineUser, "uuid-token-888");
    }

    @Test
    @DisplayName("登出防御: 未携带有效 Token 或为 undefined 时返回 PARAM_ERROR 且不清理 Redis")
    void logout_withInvalidOrMissingToken_returnsParamError() {
        when(audience.getTokenHeader()).thenReturn("Authorization");

        // 1. 无 header
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        Result<Boolean> res1 = userService.logout(request1);
        assertEquals(ResultEnum.PARAM_ERROR.getCode(), res1.getCode());

        // 2. undefined header
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", "undefined");
        Result<Boolean> res2 = userService.logout(request2);
        assertEquals(ResultEnum.PARAM_ERROR.getCode(), res2.getCode());

        // 3. null header
        MockHttpServletRequest request3 = new MockHttpServletRequest();
        request3.addHeader("Authorization", "null");
        Result<Boolean> res3 = userService.logout(request3);
        assertEquals(ResultEnum.PARAM_ERROR.getCode(), res3.getCode());

        verify(redisUtils, never()).delete(any(), anyString());
    }

    @Test
    @DisplayName("Token刷新: 存活时间低于阈值时，成功获取防并发锁并生成新 Token")
    void tokenRefresh_whenExpiringSoon_triggersRefreshAndSetsLock() {
        String token = "old-raw-jwt";
        String secret = "dGVzdC1zZWNyZXQ=";
        String uuidToken = "uuid-test-1";
        String barToken = "Bearer old-raw-jwt";

        // 模拟剩余存活时间为 100 秒，刷新阈值为 300 秒
        Date expDate = new Date(System.currentTimeMillis() + 100_000L);
        when(jwtTokenUtils.getExpiration(token, secret)).thenReturn(expDate);
        when(audience.getRefreshSecond()).thenReturn(300);
        when(audience.getExpiresSecond()).thenReturn(7200L);
        when(audience.getTokenHead()).thenReturn("Bearer ");

        // 锁获取成功
        when(redisUtils.setIfAbsent(eq(LoginKey.loginRefreshLock), eq(uuidToken), eq("1"), eq(15L), eq(TimeUnit.SECONDS)))
                .thenReturn(Boolean.TRUE);
        when(jwtTokenUtils.refreshToken(token, secret, 7200L * 1000L)).thenReturn("new-raw-jwt");
        when(redisUtils.get(LoginKey.loginToken, barToken)).thenReturn("{\"userId\":\"1001\",\"username\":\"alex\"}");

        tokenRefreshService.refreshTokenAsync(token, secret, uuidToken, barToken);

        // 验证加锁防重
        verify(redisUtils).setIfAbsent(LoginKey.loginRefreshLock, uuidToken, "1", 15L, TimeUnit.SECONDS);
        // 验证更新 loginUuid
        verify(redisUtils).setEx(LoginKey.loginUuid, uuidToken, "Bearer new-raw-jwt", 7200L, TimeUnit.SECONDS);
        // 验证写入新 loginToken
        verify(redisUtils).setEx(eq(LoginKey.loginToken), eq("Bearer new-raw-jwt"), anyString(), eq(7200L), eq(TimeUnit.SECONDS));
        // 验证删除旧 loginToken
        verify(redisUtils).delete(LoginKey.loginToken, barToken);
    }

    @Test
    @DisplayName("Token刷新并发防御: 当锁已被占用时，直接跳过重复刷新，防止雪崩")
    void tokenRefresh_concurrentCalls_blockedByLock() {
        String token = "old-raw-jwt";
        String secret = "dGVzdC1zZWNyZXQ=";
        String uuidToken = "uuid-test-2";
        String barToken = "Bearer old-raw-jwt";

        Date expDate = new Date(System.currentTimeMillis() + 100_000L);
        when(jwtTokenUtils.getExpiration(token, secret)).thenReturn(expDate);
        when(audience.getRefreshSecond()).thenReturn(300);

        // 锁获取失败（并发请求已在处理中）
        when(redisUtils.setIfAbsent(eq(LoginKey.loginRefreshLock), eq(uuidToken), eq("1"), eq(15L), eq(TimeUnit.SECONDS)))
                .thenReturn(Boolean.FALSE);

        tokenRefreshService.refreshTokenAsync(token, secret, uuidToken, barToken);

        // 验证没有生成新 token，没有覆写 redis
        verify(jwtTokenUtils, never()).refreshToken(anyString(), anyString(), anyLong());
        verify(redisUtils, never()).setEx(eq(LoginKey.loginUuid), anyString(), anyString(), anyLong(), any());
        verify(redisUtils, never()).delete(eq(LoginKey.loginToken), anyString());
    }

    @Test
    @DisplayName("Token刷新时效判断: 存活时间充足时无需刷新")
    void tokenRefresh_survivalTimeAmple_skipsRefresh() {
        String token = "healthy-jwt";
        String secret = "dGVzdC1zZWNyZXQ=";
        String uuidToken = "uuid-test-3";
        String barToken = "Bearer healthy-jwt";

        // 剩余 1000 秒，阈值 300 秒
        Date expDate = new Date(System.currentTimeMillis() + 1000_000L);
        when(jwtTokenUtils.getExpiration(token, secret)).thenReturn(expDate);
        when(audience.getRefreshSecond()).thenReturn(300);

        tokenRefreshService.refreshTokenAsync(token, secret, uuidToken, barToken);

        verify(redisUtils, never()).setIfAbsent(any(), anyString(), anyString(), anyLong(), any());
        verify(jwtTokenUtils, never()).refreshToken(anyString(), anyString(), anyLong());
    }
}
