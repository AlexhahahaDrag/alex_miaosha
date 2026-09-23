package com.alex.user.rbac;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.online.service.OnlineUserService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.rbac.service.UserDeleteCleanupService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alex.user.token.service.TokenRefreshService;
import com.alex.user.user.mapper.TUserMapper;
import com.alex.user.user.service.impl.TUserServiceImpl;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 测试用户分页查询 getPage 中多角色列表 roleInfoVoList 与聚合字符串的装配逻辑
 */
@ExtendWith(MockitoExtension.class)
public class UserPageRoleListTest {

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
    }

    @Test
    void getPage_populatesRoleInfoVoListAndRoleStrings() throws Exception {
        TUserVo loginUser = new TUserVo();
        loginUser.setId(1L);
        loginUser.setNickName("管理员");
        when(userUtils.getLoginUser()).thenReturn(loginUser);

        TUserVo user1 = new TUserVo();
        user1.setId(101L);
        user1.setUsername("user1");

        Page<TUserVo> mapperPage = new Page<>(1, 10);
        mapperPage.setRecords(Collections.singletonList(user1));
        when(tUserMapper.getPage(any(Page.class), any())).thenReturn(mapperPage);

        RoleInfoVo role1 = new RoleInfoVo();
        role1.setId(11L);
        role1.setRoleName("家庭管理员");
        role1.setRoleCode("admin");

        RoleInfoVo role2 = new RoleInfoVo();
        role2.setId(12L);
        role2.setRoleName("财务专员");
        role2.setRoleCode("finance_user");

        when(roleUserInfoService.getRoleInfoList(eq(101L), eq(false)))
                .thenReturn(Arrays.asList(role1, role2));

        Page<TUserVo> resultPage = service.getPage(1L, 10L, new TUserVo());

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getRecords().size());
        TUserVo record = resultPage.getRecords().get(0);

        List<RoleInfoVo> roleList = record.getRoleInfoVoList();
        assertNotNull(roleList);
        assertEquals(2, roleList.size());
        assertEquals("家庭管理员", roleList.get(0).getRoleName());
        assertEquals("财务专员", roleList.get(1).getRoleName());
        assertEquals("家庭管理员,财务专员", record.getRoleName());
        assertEquals("admin,finance_user", record.getRoleCode());
    }
}
