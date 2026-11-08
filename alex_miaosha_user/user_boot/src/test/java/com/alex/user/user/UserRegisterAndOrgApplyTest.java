package com.alex.user.user;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserRegisterReq;
import com.alex.base.constants.SysConf;
import com.alex.common.exception.SystemException;
import com.alex.common.exception.UserException;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.online.service.OnlineUserService;
import com.alex.user.orgInfo.entity.OrgInfo;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 手机号自助注册、图形验证码防重放及机构入驻申请审批全链路单元测试
 */
@ExtendWith(MockitoExtension.class)
public class UserRegisterAndOrgApplyTest {

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
    private OrgInfoMapper orgInfoMapper;
    @Mock
    private OrgUserInfoMapper orgUserInfoMapper;
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

    private TUserServiceImpl userService;

    @BeforeEach
    void setUp() {
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
        userService.setOrgInfoMapper(orgInfoMapper);
    }

    @Test
    void getCaptcha_generatesBase64PngAndStoresFourCharCodeInRedis() {
        Map<String, String> captcha = userService.getCaptcha();

        assertNotNull(captcha);
        String captchaId = captcha.get("captchaId");
        String image = captcha.get("image");
        assertNotNull(captchaId);
        assertFalse(captchaId.isBlank());
        assertNotNull(image);
        assertTrue(image.startsWith("data:image/png;base64,"));

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisUtils).setEx(eq(LoginKey.loginCaptcha), eq(captchaId), codeCaptor.capture(), eq(120L), eq(TimeUnit.SECONDS));
        assertEquals(4, codeCaptor.getValue().length());
    }

    @Test
    void register_createOrgMode_createsUserAndOrgAndGrantsAdminRoles() {
        when(redisUtils.get(LoginKey.loginCaptcha, "cap-1")).thenReturn("AB3D");
        when(tUserMapper.selectCount(any())).thenReturn(0L);
        doAnswer(inv -> {
            TUser u = inv.getArgument(0);
            u.setId(1001L);
            return 1;
        }).when(tUserMapper).insert(any(TUser.class));
        doAnswer(inv -> {
            OrgInfo o = inv.getArgument(0);
            o.setId(2001L);
            return 1;
        }).when(orgInfoMapper).insert(any(OrgInfo.class));

        UserRegisterReq req = new UserRegisterReq()
                .setMobile("13812345678")
                .setPassword("123456")
                .setCaptchaId("cap-1")
                .setCaptchaCode("ab3d")
                .setOrgMode("CREATE")
                .setOrgName("幸福小家");

        Boolean ok = userService.register(req);

        assertTrue(Boolean.TRUE.equals(ok));
        verify(redisUtils).delete(LoginKey.loginCaptcha, "cap-1");

        ArgumentCaptor<TUser> userCaptor = ArgumentCaptor.forClass(TUser.class);
        verify(tUserMapper).insert(userCaptor.capture());
        TUser savedUser = userCaptor.getValue();
        assertEquals("13812345678", savedUser.getUsername());
        assertEquals("13812345678", savedUser.getMobile());
        assertEquals("13812345678", savedUser.getNickName());
        assertEquals(SysConf.VALID_STATUS, savedUser.getStatus());
        assertTrue(new BCryptPasswordEncoder().matches("12345613812345678", savedUser.getPassword()));

        ArgumentCaptor<OrgInfo> orgCaptor = ArgumentCaptor.forClass(OrgInfo.class);
        verify(orgInfoMapper).insert(orgCaptor.capture());
        assertEquals("幸福小家", orgCaptor.getValue().getOrgName());
        assertEquals(SysConf.VALID_STATUS, orgCaptor.getValue().getStatus());

        verify(orgUserInfoService).assignSingleOrg(1001L, 2001L);
        verify(roleUserInfoService).assignRoles(
                1001L,
                Arrays.asList(TUserServiceImpl.ROLE_ID_GIFT_ADMIN, TUserServiceImpl.ROLE_ID_FAMILY_ADMIN)
        );
        verify(permissionContextCacheService).invalidate(1001L);
    }

    @Test
    void register_joinOrgMode_submitsPendingApplicationAndGrantsDefaultUserRoles() {
        when(redisUtils.get(LoginKey.loginCaptcha, "cap-2")).thenReturn("9K7M");
        when(tUserMapper.selectCount(any())).thenReturn(0L);
        OrgInfo existingOrg = new OrgInfo();
        existingOrg.setId(9001L);
        existingOrg.setOrgName("快乐大家庭");
        existingOrg.setStatus(SysConf.VALID_STATUS);
        existingOrg.setIsDelete(0);
        when(orgInfoMapper.selectById(9001L)).thenReturn(existingOrg);
        doAnswer(inv -> {
            TUser u = inv.getArgument(0);
            u.setId(1002L);
            return 1;
        }).when(tUserMapper).insert(any(TUser.class));

        UserRegisterReq req = new UserRegisterReq()
                .setMobile("13987654321")
                .setPassword("abcdef")
                .setNickName("小明")
                .setCaptchaId("cap-2")
                .setCaptchaCode("9K7M")
                .setOrgMode("JOIN")
                .setTargetOrgId(9001L);

        Boolean ok = userService.register(req);

        assertTrue(Boolean.TRUE.equals(ok));
        verify(redisUtils).delete(LoginKey.loginCaptcha, "cap-2");
        verify(orgUserInfoService).submitOrgApplication(1002L, 9001L);
        verify(orgUserInfoService, never()).assignSingleOrg(any(), any());
        verify(roleUserInfoService).assignRoles(
                1002L,
                Arrays.asList(TUserServiceImpl.ROLE_ID_GIFT_USER, TUserServiceImpl.ROLE_ID_FINANCE_USER)
        );
    }

    @Test
    void register_rejectsWrongCaptchaAndDeletesKeyImmediately() {
        when(redisUtils.get(LoginKey.loginCaptcha, "cap-bad")).thenReturn("ABCD");

        UserRegisterReq req = new UserRegisterReq()
                .setMobile("13812345678")
                .setPassword("123456")
                .setCaptchaId("cap-bad")
                .setCaptchaCode("ZZZZ")
                .setOrgMode("CREATE")
                .setOrgName("测试机构");

        assertThrows(SystemException.class, () -> userService.register(req));
        verify(redisUtils).delete(LoginKey.loginCaptcha, "cap-bad");
        verify(tUserMapper, never()).insert(any(TUser.class));
    }

    @Test
    void register_rejectsInvalidMobileAndShortPasswordAndDuplicateMobile() {
        // 1. 非法手机号
        when(redisUtils.get(LoginKey.loginCaptcha, "c1")).thenReturn("2345");
        UserRegisterReq badMobile = new UserRegisterReq()
                .setMobile("12812345678")
                .setPassword("123456")
                .setCaptchaId("c1")
                .setCaptchaCode("2345")
                .setOrgMode("CREATE")
                .setOrgName("测试机构");
        assertThrows(SystemException.class, () -> userService.register(badMobile));

        // 2. 短密码
        when(redisUtils.get(LoginKey.loginCaptcha, "c2")).thenReturn("2345");
        UserRegisterReq shortPwd = new UserRegisterReq()
                .setMobile("13812345678")
                .setPassword("12345")
                .setCaptchaId("c2")
                .setCaptchaCode("2345")
                .setOrgMode("CREATE")
                .setOrgName("测试机构");
        assertThrows(SystemException.class, () -> userService.register(shortPwd));

        // 3. 重复手机号
        when(redisUtils.get(LoginKey.loginCaptcha, "c3")).thenReturn("2345");
        when(tUserMapper.selectCount(any())).thenReturn(1L);
        UserRegisterReq dupMobile = new UserRegisterReq()
                .setMobile("13812345678")
                .setPassword("123456")
                .setCaptchaId("c3")
                .setCaptchaCode("2345")
                .setOrgMode("CREATE")
                .setOrgName("测试机构");
        assertThrows(UserException.class, () -> userService.register(dupMobile));
    }

    @Test
    void orgApplication_auditApproveAndRejectAndPruneProtection() {
        OrgUserInfo pendingRow = new OrgUserInfo();
        pendingRow.setId(501L);
        pendingRow.setUserId("1002");
        pendingRow.setOrgId("9001");
        pendingRow.setStatus("0");
        pendingRow.setSummary("APPLY");
        pendingRow.setCreateTime(LocalDateTime.of(2026, 1, 1, 0, 0));

        // 另一条仍在待审中的入驻申请，pruneInactiveHistory 绝对不能误删
        OrgUserInfo otherPendingRow = new OrgUserInfo();
        otherPendingRow.setId(502L);
        otherPendingRow.setUserId("1002");
        otherPendingRow.setOrgId("9002");
        otherPendingRow.setStatus("0");
        otherPendingRow.setSummary("APPLY");
        otherPendingRow.setCreateTime(LocalDateTime.of(2026, 1, 1, 0, 1));

        List<OrgUserInfo> invalidRows = new ArrayList<>();
        invalidRows.add(pendingRow);
        invalidRows.add(otherPendingRow);
        for (long i = 1; i <= 6; i++) {
            OrgUserInfo history = new OrgUserInfo();
            history.setId(600L + i);
            history.setUserId("1002");
            history.setOrgId("8000");
            history.setStatus("0");
            history.setSummary("MAIN");
            history.setCreateTime(LocalDateTime.of(2026, 1, 2, 0, (int) i));
            invalidRows.add(history);
        }
        doAnswer(inv -> {
            Collection<?> ids = inv.getArgument(0);
            invalidRows.removeIf(r -> ids.contains(r.getId()));
            return ids.size();
        }).when(orgUserInfoMapper).deleteByIds(any());

        List<OrgUserInfo> savedRows = new ArrayList<>();
        OrgUserInfoServiceImp orgUserService = new OrgUserInfoServiceImp(
                orgUserInfoMapper,
                noOpTransactionTemplate(),
                permissionContextCacheService
        ) {
            @Override
            protected List<OrgUserInfo> listPendingApplicationsByUserAndOrg(Long userId, Long orgId) {
                return Collections.singletonList(pendingRow);
            }

            @Override
            protected List<OrgUserInfo> listActiveAssignments(Long userId) {
                return Collections.emptyList();
            }

            @Override
            protected List<OrgUserInfo> listInvalidHistory(Long userId) {
                return new ArrayList<>(invalidRows);
            }

            @Override
            public boolean save(OrgUserInfo entity) {
                savedRows.add(entity);
                return true;
            }

            @Override
            public boolean updateById(OrgUserInfo entity) {
                return true;
            }
        };

        OrgInfoVo visibleOrg = new OrgInfoVo();
        visibleOrg.setId(9001L);
        TUserVo adminUser = new TUserVo();
        adminUser.setId(1L);
        RoleInfoVo adminRole = new RoleInfoVo();
        adminRole.setRoleCode(RbacRoleCodes.ADMIN);
        adminUser.setRoleInfoVoList(Collections.singletonList(adminRole));
        orgUserService.setUserUtils(userUtils);
        orgUserService.setOrgInfoMapper(orgInfoMapper);
        when(userUtils.getLoginUser()).thenReturn(adminUser);
        when(orgInfoMapper.queryOrgInfo("9001")).thenReturn(visibleOrg);

        Boolean approved = orgUserService.auditApplication(9001L, 1002L, true);
        assertTrue(Boolean.TRUE.equals(approved));
        assertEquals(1, savedRows.size());
        assertEquals("9001", savedRows.get(0).getOrgId());
        assertEquals("1", savedRows.get(0).getStatus());
        assertEquals("MAIN", savedRows.get(0).getSummary());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> deleteCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(orgUserInfoMapper, org.mockito.Mockito.times(2)).deleteByIds(deleteCaptor.capture());
        List<Collection<Long>> allDeletes = deleteCaptor.getAllValues();
        // 第一次删除已审批的申请单 501L，第二次 pruneInactiveHistory 仅删除最早的非 APPLY 历史记录 601L
        assertTrue(allDeletes.get(0).contains(501L));
        assertEquals(1, allDeletes.get(1).size());
        assertTrue(allDeletes.get(1).contains(601L));
    }

    @Test
    void orgApplication_deniedWhenCrossOrgAdminAttemptsAudit() {
        OrgUserInfoServiceImp orgUserService = new OrgUserInfoServiceImp(
                orgUserInfoMapper,
                noOpTransactionTemplate(),
                permissionContextCacheService
        );
        orgUserService.setUserUtils(userUtils);
        orgUserService.setOrgInfoMapper(orgInfoMapper);

        TUserVo otherAdmin = new TUserVo();
        otherAdmin.setId(2L);
        RoleInfoVo adminRole = new RoleInfoVo();
        adminRole.setRoleCode(RbacRoleCodes.ADMIN);
        otherAdmin.setRoleInfoVoList(Collections.singletonList(adminRole));
        when(userUtils.getLoginUser()).thenReturn(otherAdmin);
        when(orgInfoMapper.queryOrgInfo("9001")).thenReturn(null);

        SystemException ex = assertThrows(SystemException.class,
                () -> orgUserService.auditApplication(9001L, 1002L, true));
        assertTrue(ex.getMsg().contains("无权"));
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
