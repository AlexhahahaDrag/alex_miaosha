package com.alex.finance.budget;

import com.alex.api.finance.vo.finance.FinanceBudgetSaveReq;
import com.alex.api.finance.vo.finance.FinanceBudgetStatusVo;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.FinanceSummaryVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.finance.entity.FinanceBudgetInfo;
import com.alex.finance.finance.mapper.FinanceBudgetInfoMapper;
import com.alex.finance.finance.service.FinanceInfoService;
import com.alex.finance.finance.service.impl.FinanceBudgetInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * description: 零花钱预算 RBAC 与跨租户/跨机构越权防御集成测试
 * author: alex
 * createDate: 2026-10-15
 */
class FinanceBudgetOwnershipIT {

    private FinanceBudgetInfoMapper financeBudgetInfoMapper;
    private FinanceInfoService financeInfoService;
    private UserUtils userUtils;
    private FinanceBudgetInfoServiceImpl budgetService;

    @BeforeEach
    void setUp() {
        financeBudgetInfoMapper = mock(FinanceBudgetInfoMapper.class);
        financeInfoService = mock(FinanceInfoService.class);
        userUtils = mock(UserUtils.class);

        budgetService = new FinanceBudgetInfoServiceImpl(financeBudgetInfoMapper, financeInfoService);
        ReflectionTestUtils.setField(budgetService, "userUtils", userUtils);
    }

    private TUserVo buildUser(Long id, Long orgId, String roleCode, List<String> permissions) {
        TUserVo user = new TUserVo();
        user.setId(id);
        user.setOrgId(orgId);

        OrgInfoVo org = new OrgInfoVo();
        org.setId(orgId);
        org.setOrgName("测试家庭组");
        user.setOrgInfoVo(org);

        if (roleCode != null) {
            RoleInfoVo role = new RoleInfoVo();
            role.setRoleCode(roleCode);
            user.setRoleInfoVoList(Collections.singletonList(role));
        }
        if (permissions != null) {
            user.setPermissionCodes(permissions);
        }
        return user;
    }

    @Test
    @DisplayName("跨机构越权拦截：普通家庭成员越权查询其他机构预算时抛出 FinanceException(401)")
    void should_reject_cross_org_budget_query() {
        // 当前用户归属于机构 20
        TUserVo member = buildUser(2L, 20L, "family_member", Collections.singletonList("finance:financeManager:budget:view"));
        when(userUtils.getLoginUser()).thenReturn(member);
        when(userUtils.getOrgId()).thenReturn(20L);

        // 尝试查询机构 999 的预算
        FinanceException ex = assertThrows(FinanceException.class, () ->
                budgetService.getMonthlyBudgetStatus("2026-10", 999L, 2L)
        );
        assertEquals("无权访问其他机构数据", ex.getMsg());
    }

    @Test
    @DisplayName("跨机构越权拦截：家庭管理员越权保存其他机构预算时抛出 FinanceException(401)")
    void should_reject_cross_org_budget_save() {
        // 当前管理员归属于机构 20
        TUserVo admin = buildUser(1L, 20L, "family_admin", Collections.singletonList("finance:financeManager:budget:edit"));
        when(userUtils.getLoginUser()).thenReturn(admin);
        when(userUtils.getOrgId()).thenReturn(20L);

        // 尝试保存机构 999 的预算
        FinanceBudgetSaveReq req = new FinanceBudgetSaveReq();
        req.setOrgId(999L);
        req.setBudgetMonth("2026-10");
        req.setBudgetAmount(new BigDecimal("3000.00"));

        FinanceException ex = assertThrows(FinanceException.class, () ->
                budgetService.saveMonthlyBudget(req)
        );
        assertEquals("无权访问其他机构数据", ex.getMsg());
        verify(financeBudgetInfoMapper, never()).insert(any(FinanceBudgetInfo.class));
        verify(financeBudgetInfoMapper, never()).updateById(any(FinanceBudgetInfo.class));
    }

    @Test
    @DisplayName("角色权限边界拦截：普通家庭成员无 budget:edit 权限时禁止保存预算")
    void should_reject_member_without_budget_edit_permission() {
        // 普通家庭成员仅有 view 权限，无 edit 权限
        TUserVo member = buildUser(2L, 20L, "family_member", Collections.singletonList("finance:financeManager:budget:view"));
        when(userUtils.getLoginUser()).thenReturn(member);
        when(userUtils.getOrgId()).thenReturn(20L);

        FinanceBudgetSaveReq req = new FinanceBudgetSaveReq();
        req.setOrgId(20L);
        req.setBudgetMonth("2026-10");
        req.setBudgetAmount(new BigDecimal("2500.00"));

        FinanceException ex = assertThrows(FinanceException.class, () ->
                budgetService.saveMonthlyBudget(req)
        );
        assertEquals("无权保存预算设定，仅家庭管理员可操作", ex.getMsg());
        verify(financeBudgetInfoMapper, never()).insert(any(FinanceBudgetInfo.class));
        verify(financeBudgetInfoMapper, never()).updateById(any(FinanceBudgetInfo.class));
    }

    @Test
    @DisplayName("正常授权放行：本机构家庭管理员可正常配置与更新月度预算")
    void should_allow_same_org_family_admin_to_manage_budget() {
        // 本机构家庭管理员
        TUserVo admin = buildUser(1L, 20L, "family_admin", Collections.singletonList("finance:financeManager:budget:edit"));
        when(userUtils.getLoginUser()).thenReturn(admin);
        when(userUtils.getOrgId()).thenReturn(20L);

        FinanceBudgetSaveReq req = new FinanceBudgetSaveReq();
        req.setOrgId(20L);
        req.setBudgetMonth("2026-10");
        req.setBudgetAmount(new BigDecimal("2800.00"));
        req.setCategoryCodes(Collections.singletonList("餐饮美食"));

        // 数据库无记录时执行 insert
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq("2026-10"))).thenReturn(null);
        Boolean result = budgetService.saveMonthlyBudget(req);
        assertTrue(result);
        verify(financeBudgetInfoMapper, times(1)).insert(any(FinanceBudgetInfo.class));

        // 查询当月预算状态
        FinanceBudgetInfo saved = new FinanceBudgetInfo();
        saved.setId(100L);
        saved.setOrgId(20L);
        saved.setBudgetMonth("2026-10");
        saved.setBudgetAmount(new BigDecimal("2800.00"));
        saved.setCategoryCodes("餐饮美食");
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq("2026-10"))).thenReturn(saved);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("800.00"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(3L)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus("2026-10", 20L, 1L);
        assertNotNull(status);
        assertEquals(new BigDecimal("2800.00"), status.getBudgetAmount());
        assertEquals(new BigDecimal("800.00"), status.getActualExpense());
        assertEquals(new BigDecimal("2000.00"), status.getRemainingAmount());
        assertFalse(status.getIsOverBudget());
    }
}
