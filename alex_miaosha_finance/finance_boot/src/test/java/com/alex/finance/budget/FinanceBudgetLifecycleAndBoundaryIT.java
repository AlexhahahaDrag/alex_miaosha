package com.alex.finance.budget;

import com.alex.api.finance.vo.finance.FinanceBudgetSaveReq;
import com.alex.api.finance.vo.finance.FinanceBudgetStatusVo;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.FinanceSummaryVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.finance.entity.FinanceBudgetInfo;
import com.alex.finance.finance.mapper.FinanceBudgetInfoMapper;
import com.alex.finance.finance.service.FinanceInfoService;
import com.alex.finance.finance.service.impl.FinanceBudgetInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 集成测试：零花钱预算七点法边界、非法月份拦截、负数防御与继承生命周期
 */
@ExtendWith(MockitoExtension.class)
class FinanceBudgetLifecycleAndBoundaryIT {

    @Mock
    private FinanceBudgetInfoMapper financeBudgetInfoMapper;

    @Mock
    private FinanceInfoService financeInfoService;

    @Mock
    private UserUtils userUtils;

    private FinanceBudgetInfoServiceImpl budgetService;

    private TUserVo adminUser;

    @BeforeEach
    void setUp() {
        budgetService = new FinanceBudgetInfoServiceImpl(financeBudgetInfoMapper, financeInfoService);
        ReflectionTestUtils.setField(budgetService, "userUtils", userUtils);

        adminUser = new TUserVo();
        adminUser.setId(1001L);
        adminUser.setUsername("alex_admin");
        RoleInfoVo adminRole = new RoleInfoVo();
        adminRole.setRoleCode("ROLE_ADMIN");
        adminUser.setRoleInfoVoList(List.of(adminRole));

        lenient().when(userUtils.getOrgId()).thenReturn(20L);
        lenient().when(userUtils.getUserId()).thenReturn(1001L);
        lenient().when(userUtils.getLoginUser()).thenReturn(adminUser);
    }

    @Test
    @DisplayName("边界校验：非法月份格式（如 2026-13、2026/10、abc）抛出 FinanceException.param (400)")
    void testInvalidMonthFormatThrowsFinanceException() {
        // 非法月份：超界月份 13
        FinanceException ex1 = assertThrows(FinanceException.class,
                () -> budgetService.getMonthlyBudgetStatus("2026-13", 20L, 1001L));
        assertTrue(ex1.getMessage().contains("预算月份格式不正确"));

        // 非法月份：非破折号分隔
        FinanceException ex2 = assertThrows(FinanceException.class,
                () -> budgetService.getMonthlyBudgetStatus("2026/10", 20L, 1001L));
        assertTrue(ex2.getMessage().contains("预算月份格式不正确"));

        // 非法月份：纯文本
        FinanceException ex3 = assertThrows(FinanceException.class,
                () -> budgetService.getMonthlyBudgetStatus("invalid-month", 20L, 1001L));
        assertTrue(ex3.getMessage().contains("预算月份格式不正确"));
    }

    @Test
    @DisplayName("边界校验：保存预算时金额为负数（-100.00）强制拦截并拒绝保存")
    void testNegativeBudgetAmountThrowsFinanceException() {
        FinanceBudgetSaveReq req = new FinanceBudgetSaveReq();
        req.setBudgetMonth("2026-10");
        req.setBudgetAmount(new BigDecimal("-100.00"));
        req.setCategoryCodes(List.of("餐饮"));

        FinanceException ex = assertThrows(FinanceException.class,
                () -> budgetService.saveMonthlyBudget(req));
        assertTrue(ex.getMessage().contains("预算金额不能为负数"));
        verify(financeBudgetInfoMapper, never()).insert(any(FinanceBudgetInfo.class));
        verify(financeBudgetInfoMapper, never()).updateById(any(FinanceBudgetInfo.class));
    }

    @Test
    @DisplayName("边界校验：零预算金额（0.00）时除零安全，使用率恒为 0.0% 无崩溃")
    void testZeroBudgetAmountSafeZeroDivision() {
        String month = "2026-10";
        FinanceBudgetInfo current = new FinanceBudgetInfo();
        current.setId(10L);
        current.setOrgId(20L);
        current.setBudgetMonth(month);
        current.setBudgetAmount(BigDecimal.ZERO);
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(current);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("150.00"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(2L)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, 20L, 1001L);

        assertNotNull(status);
        assertEquals(BigDecimal.ZERO, status.getBudgetAmount());
        assertEquals(new BigDecimal("150.00"), status.getActualExpense());
        assertEquals(new BigDecimal("-150.00"), status.getRemainingAmount());
        assertEquals(new BigDecimal("0.0"), status.getUsagePercent(), "零预算额时使用率固定为 0.0%");
        assertFalse(status.getIsOverBudget(), "零预算额不应标记为超支以防前端界面异常爆红");
    }

    @Test
    @DisplayName("超支状态机：消费额 1500 > 预算 1000 时，超支标记为 true，剩余额为负数")
    void testOverBudgetStatusCalculation() {
        String month = "2026-10";
        FinanceBudgetInfo current = new FinanceBudgetInfo();
        current.setId(10L);
        current.setOrgId(20L);
        current.setBudgetMonth(month);
        current.setBudgetAmount(new BigDecimal("1000.00"));
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(current);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("1500.00"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(10L)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, 20L, 1001L);

        assertNotNull(status);
        assertTrue(status.getIsOverBudget(), "实际开销大于预算时必须标记为超支");
        assertEquals(new BigDecimal("-500.00"), status.getRemainingAmount());
        assertEquals(new BigDecimal("150.0"), status.getUsagePercent());
    }

    @Test
    @DisplayName("继承状态机：当月未设置时自动继承最近历史月配置，且标记 isInherited = true")
    void testInheritedBudgetFromHistoryMonth() {
        String month = "2026-11";
        // 当月未设
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(null);

        // 历史月 2026-10 设为 3000
        FinanceBudgetInfo history = new FinanceBudgetInfo();
        history.setId(5L);
        history.setOrgId(20L);
        history.setBudgetMonth("2026-10");
        history.setBudgetAmount(new BigDecimal("3000.00"));
        history.setCategoryCodes("餐饮,日常用品");
        when(financeBudgetInfoMapper.selectLatestBefore(eq(20L), eq(month))).thenReturn(history);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("800.00"))
                .totalIncome(BigDecimal.ZERO)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, 20L, 1001L);

        assertNotNull(status);
        assertTrue(status.getIsInherited(), "当月未配置时必须继承历史配置");
        assertEquals(new BigDecimal("3000.00"), status.getBudgetAmount());
        assertEquals(Arrays.asList("餐饮", "日常用品"), status.getCategoryCodes());
        assertEquals(new BigDecimal("2200.00"), status.getRemainingAmount());
    }

    @Test
    @DisplayName("分类清洗：保存时自动剥离'支出'、'收入'关键字并去重去空白")
    void testCategoryCodesCleaningOnSave() {
        FinanceBudgetSaveReq req = new FinanceBudgetSaveReq();
        req.setBudgetMonth("2026-10");
        req.setBudgetAmount(new BigDecimal("2500.00"));
        // 包含前后空格、重复项以及脏数据 "支出", "expense"
        req.setCategoryCodes(Arrays.asList(" 餐饮 ", "餐饮", "交通", "支出", "expense", "娱乐 "));

        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq("2026-10"))).thenReturn(null);

        budgetService.saveMonthlyBudget(req);

        ArgumentCaptor<FinanceBudgetInfo> captor = ArgumentCaptor.forClass(FinanceBudgetInfo.class);
        verify(financeBudgetInfoMapper, times(1)).insert(captor.capture());

        FinanceBudgetInfo saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("餐饮,交通,娱乐", saved.getCategoryCodes(), "必须彻底清洗关键字并去重去空白");
    }
}
