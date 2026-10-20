package com.alex.user.security;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.LoginException;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.online.service.OnlineUserService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.rbac.service.UserDeleteCleanupService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alex.user.token.service.TokenRefreshService;
import com.alex.user.user.entity.TUser;
import com.alex.user.user.mapper.TUserMapper;
import com.alex.user.user.service.impl.TUserServiceImpl;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 集成测试套件 3:
 * 用户认证契约、空参校验、密码错误重试与状态机防护测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserAuthenticationContractIT {

    @Mock
    private TUserMapper tUserMapper;
    @Mock
    private RedisUtils redisUtils;
    @Mock
    private JwtTokenUtils jwtTokenUtils;
    @Mock
    private Audience audience;
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
    private TokenRefreshService tokenRefreshService;
    @Mock
    private OnlineUserService onlineUserService;
    @Mock
    private Executor asyncTaskExecutor;
    @Mock
    private UserPermissionContextService userPermissionContextService;
    @Mock
    private UserDeleteCleanupService userDeleteCleanupService;
    @Mock
    private PermissionContextCacheService permissionContextCacheService;

    private TUserServiceImpl service;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, TUser.class);
    }

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(asyncTaskExecutor).execute(any(Runnable.class));

        service = new TUserServiceImpl(
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
        ReflectionTestUtils.setField(service, "baseMapper", tUserMapper);
    }

    @Test
    @DisplayName("登录参数校验: 用户名或密码为空时必须抛出 USER_USERNAME_OR_PASSWORD_EMPTY")
    void login_blankUsernameOrPassword_rejected() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        // 1. 用户名为空
        LoginException ex1 = assertThrows(LoginException.class,
                () -> service.login(request, "", "123456", false));
        assertEquals(ResultEnum.USER_USERNAME_OR_PASSWORD_EMPTY.getCode(), ex1.getCode());

        // 2. 密码为空
        LoginException ex2 = assertThrows(LoginException.class,
                () -> service.login(request, "admin", null, false));
        assertEquals(ResultEnum.USER_USERNAME_OR_PASSWORD_EMPTY.getCode(), ex2.getCode());
    }

    @Test
    @DisplayName("锁定防御: 密码错误累计超过 5 次时必须拦截登录并提示已被锁定")
    void login_exceedMaxRetryLimit_rejected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");

        // 模拟 Redis 中错误计数已达到 5 次
        when(redisUtils.get(anyString())).thenReturn("5");

        LoginException ex = assertThrows(LoginException.class,
                () -> service.login(request, "victim_user", "password123", false));
        assertEquals(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("禁用防御: 账号状态为 0 (禁用) 时无法通过状态白名单，抛出查询异常")
    void login_disabledAccount_rejected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        when(redisUtils.get(anyString())).thenReturn(null);
        when(redisUtils.get(any(), anyString(), eq(TUserVo.class))).thenReturn(null);

        // 查库由于强制要求 status = '1'，返回空列表
        when(tUserMapper.selectList(any())).thenReturn(Collections.emptyList());

        LoginException ex = assertThrows(LoginException.class,
                () -> service.login(request, "disabled_user", "password123", false));
        assertNotNull(ex);
        // 验证累加了失败重试计数（setEx 锁定30分钟）
        verify(redisUtils).setEx(anyString(), anyString(), anyInt(), eq(TimeUnit.MINUTES));
    }
}
