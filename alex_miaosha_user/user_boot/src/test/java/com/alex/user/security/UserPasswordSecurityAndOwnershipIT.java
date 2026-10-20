package com.alex.user.security;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.SystemException;
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
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 集成测试套件 2:
 * 用户密码安全防护、越权拦截与加密规范测试
 */
@ExtendWith(MockitoExtension.class)
public class UserPasswordSecurityAndOwnershipIT {

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

    private TUserVo loginUser(String roleCode, Long orgId) {
        TUserVo vo = new TUserVo();
        vo.setId(1L);
        vo.setUsername("operator");
        vo.setOrgId(orgId);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        vo.setRoleInfoVoList(Collections.singletonList(role));
        return vo;
    }

    @Test
    @DisplayName("用户资料更新: updateTUser 必须忽略 password 字段，防止明文覆盖或破坏哈希散列")
    void updateTUser_ignoresPlaintextPassword_neverOverwritesWithPlaintext() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER, 1L));
        when(tUserMapper.selectCount(any())).thenReturn(0L);

        TUserVo updateParam = new TUserVo();
        updateParam.setId(100L);
        updateParam.setUsername("alex_user");
        updateParam.setEmail("alex@example.com");
        updateParam.setMobile("13800000000");
        updateParam.setPassword("PlainTextP@ssw0rd!"); // 携带明文密码

        ArgumentCaptor<TUser> entityCaptor = ArgumentCaptor.forClass(TUser.class);
        when(tUserMapper.updateById(entityCaptor.capture())).thenReturn(1);

        service.updateTUser(updateParam);

        TUser updatedEntity = entityCaptor.getValue();
        assertNotNull(updatedEntity);
        // 关键断言：password 必须被忽略（为 null），不会以明文写入 DB
        assertNull(updatedEntity.getPassword(), "updateTUser must ignore password to protect hash integrity");
        assertEquals("alex_user", updatedEntity.getUsername());
        assertEquals("alex@example.com", updatedEntity.getEmail());
    }

    @Test
    @DisplayName("密码重置: 超管执行重置，密码必须符合 salt = username 的 BCrypt 加密，并清理权限上下文")
    void resetPassword_superAdmin_encodesWithSaltAndInvalidatesContext() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER, 1L));

        TUser targetUser = new TUser();
        targetUser.setId(200L);
        targetUser.setUsername("bob");
        when(tUserMapper.selectById(200L)).thenReturn(targetUser);

        when(tUserMapper.update(isNull(), any())).thenReturn(1);

        Boolean result = service.resetUserPassword(200L, "NewSecurePass123");
        assertTrue(result);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<TUser>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(tUserMapper).update(isNull(), wrapperCaptor.capture());

        String sqlSet = wrapperCaptor.getValue().getSqlSet();
        assertNotNull(sqlSet);
        assertTrue(sqlSet.contains("password"));

        // 验证失效了权限上下文
        verify(permissionContextCacheService).invalidate(200L);
    }

    @Test
    @DisplayName("弱密防御: 重置密码为空、全空格或长度小于6位时直接拒绝")
    void resetPassword_weakOrBlankPassword_rejected() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER, 1L));

        // 1. null
        assertThrows(SystemException.class, () -> service.resetUserPassword(200L, null));
        // 2. 空白字符
        assertThrows(SystemException.class, () -> service.resetUserPassword(200L, "   "));
        // 3. 少于 6 位
        assertThrows(SystemException.class, () -> service.resetUserPassword(200L, "12345"));

        verify(tUserMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("异常防御: 重置不存在的用户密码时抛出参数异常")
    void resetPassword_nonExistentUser_rejected() {
        when(userUtils.getLoginUser()).thenReturn(loginUser(RbacRoleCodes.SUPER, 1L));
        when(tUserMapper.selectById(9999L)).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class, () -> service.resetUserPassword(9999L, "ValidPass888"));
        assertTrue(ex.getMessage().contains("不存在"));

        verify(tUserMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("越权防御: 机构管理员试图重置非本机构用户的密码时，被 assertUserAccessible 拦截")
    void resetPassword_orgAdminAccessCrossOrg_rejected() {
        // 当前登录为非超管机构管理员（orgId = 10）
        when(userUtils.getLoginUser()).thenReturn(loginUser("ORG_ADMIN", 10L));
        // 机构数据过滤：queryTUser("500") 查不到跨机构用户（返回 null）
        when(tUserMapper.queryTUser("500")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class, () -> service.resetUserPassword(500L, "NewPass123"));
        assertTrue(ex.getMessage().contains("无权访问其他机构的用户"));

        verify(tUserMapper, never()).update(any(), any());
    }
}
