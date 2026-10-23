package com.alex.user.dashboard;

import com.alex.api.user.dashboard.MicroServiceHealthVo;
import com.alex.api.user.dashboard.OrgAdminDashboardVo;
import com.alex.api.user.dashboard.SuperAdminDashboardVo;
import com.alex.api.user.dashboard.SystemAuditLogVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.common.exception.SystemException;
import com.alex.user.dashboard.service.impl.DashboardServiceImpl;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.user.mapper.TUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService 单元测试与多角色权限防护")
class DashboardServiceTest {

    @Mock
    private TUserMapper tUserMapper;

    @Mock
    private OrgInfoMapper orgInfoMapper;

    @Mock
    private OrgUserInfoMapper orgUserInfoMapper;

    @Mock
    private RoleInfoMapper roleInfoMapper;

    @Mock
    private UserUtils userUtils;

    @Mock
    private UserPermissionContextService userPermissionContextService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private final Long superUserId = 1L;
    private final Long normalUserId = 2L;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("非超管用户访问超管大盘应被拒绝抛出 SystemException 403")
    void testSuperAdminAccessDeniedForNormalUser() {
        when(userUtils.getUserId()).thenReturn(normalUserId);

        UserPermissionContextVo normalContext = new UserPermissionContextVo();
        normalContext.setSuperAdmin(false);
        RoleInfoVo normalRole = new RoleInfoVo();
        normalRole.setRoleCode("user");
        normalRole.setRoleName("普通用户");
        normalContext.setRoleList(Collections.singletonList(normalRole));

        when(userPermissionContextService.buildContext(eq(normalUserId), eq(false))).thenReturn(normalContext);

        assertThrows(SystemException.class, () -> dashboardService.getSuperAdminOverview());
    }

    @Test
    @DisplayName("超管用户成功获取大盘指标与 7 天日期态势数据")
    void testSuperAdminOverviewSuccess() {
        when(userUtils.getUserId()).thenReturn(superUserId);

        UserPermissionContextVo superContext = new UserPermissionContextVo();
        superContext.setSuperAdmin(true);
        RoleInfoVo superRole = new RoleInfoVo();
        superRole.setRoleCode(RbacRoleCodes.SUPER);
        superRole.setRoleName("超级管理员");
        superContext.setRoleList(Collections.singletonList(superRole));

        when(userPermissionContextService.buildContext(eq(superUserId), eq(false))).thenReturn(superContext);
        when(tUserMapper.selectCount(any())).thenReturn(15000L);
        when(orgInfoMapper.selectCount(any())).thenReturn(108L);

        SuperAdminDashboardVo vo = dashboardService.getSuperAdminOverview();
        assertNotNull(vo);
        assertEquals(15000L, vo.getTotalUsers());
        assertEquals(108L, vo.getActiveOrgs());
        assertNotNull(vo.getTrendDates());
        assertEquals(7, vo.getTrendDates().size(), "近 7 天趋势日期应包含 7 个刻度");
        assertEquals(7, vo.getTrendQps().size());
        assertEquals(7, vo.getTrendOrders().size());
        assertNotNull(vo.getServiceNodes());
        assertEquals(6, vo.getServiceNodes().size(), "应包含 6 个核心微服务节点");
    }

    @Test
    @DisplayName("机构管理员成功获取在册成员数与机构指标")
    void testOrgAdminOverviewSuccess() {
        when(userUtils.getUserId()).thenReturn(normalUserId);

        UserPermissionContextVo orgContext = new UserPermissionContextVo();
        orgContext.setSuperAdmin(false);
        OrgInfoVo orgInfoVo = new OrgInfoVo();
        orgInfoVo.setId(2001L);
        orgInfoVo.setOrgName("上海研发中心");
        orgContext.setOrgInfo(orgInfoVo);
        RoleInfoVo orgAdminRole = new RoleInfoVo();
        orgAdminRole.setRoleCode("admin");
        orgAdminRole.setRoleName("机构主管");
        orgContext.setRoleList(Collections.singletonList(orgAdminRole));

        when(userPermissionContextService.buildContext(eq(normalUserId), eq(false))).thenReturn(orgContext);
        when(orgUserInfoMapper.selectCount(any())).thenReturn(42L);
        when(roleInfoMapper.selectCount(any())).thenReturn(5L);

        OrgAdminDashboardVo vo = dashboardService.getOrgAdminOverview();
        assertNotNull(vo);
        assertEquals(2001L, vo.getOrgId());
        assertEquals("上海研发中心", vo.getOrgName());
        assertEquals(42L, vo.getMemberCount());
        assertEquals(5L, vo.getRoleCount());
        assertEquals("机构主管", vo.getAdminName());
    }

    @Test
    @DisplayName("微服务健康探针测试：自身 30006 端口天然 UP，其余降级容灾正常返回")
    void testMicroServiceHealthProbe() {
        List<MicroServiceHealthVo> healthList = dashboardService.getServicesHealth();
        assertNotNull(healthList);
        assertEquals(6, healthList.size());

        // 校验 30006 端口
        MicroServiceHealthVo userSvc = healthList.stream()
                .filter(s -> s.getPort() == 30006)
                .findFirst()
                .orElse(null);
        assertNotNull(userSvc);
        assertEquals("UP", userSvc.getStatus());
        assertEquals(1L, userSvc.getLatency());

        // 校验返回时间戳非空
        for (MicroServiceHealthVo svc : healthList) {
            assertNotNull(svc.getLastCheckTime());
            assertNotNull(svc.getStatus());
            assertNotNull(svc.getName());
        }
    }

    @Test
    @DisplayName("系统审计动态时间线正确生成且条目完整")
    void testAuditTimeline() {
        List<SystemAuditLogVo> list = dashboardService.getAuditTimeline();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        for (SystemAuditLogVo item : list) {
            assertNotNull(item.getId());
            assertNotNull(item.getTitle());
            assertNotNull(item.getOperator());
            assertNotNull(item.getOperateTime());
        }
    }
}
