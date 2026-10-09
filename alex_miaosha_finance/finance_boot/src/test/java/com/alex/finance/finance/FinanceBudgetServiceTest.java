package com.alex.finance.finance;

import com.alex.api.finance.vo.finance.FinanceBudgetSaveReq;
import com.alex.api.finance.vo.finance.FinanceBudgetStatusVo;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.FinanceSummaryVo;
import com.alex.finance.finance.entity.FinanceBudgetInfo;
import com.alex.finance.finance.mapper.FinanceBudgetInfoMapper;
import com.alex.finance.finance.service.FinanceInfoService;
import com.alex.finance.finance.service.impl.FinanceBudgetInfoServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FinanceBudgetServiceTest {

    private FinanceBudgetInfoMapper financeBudgetInfoMapper;
    private FinanceInfoService financeInfoService;
    private com.alex.api.user.user.UserUtils userUtils;
    private FinanceBudgetInfoServiceImpl budgetService;

    @BeforeEach
    void setUp() {
        financeBudgetInfoMapper = mock(FinanceBudgetInfoMapper.class);
        financeInfoService = mock(FinanceInfoService.class);
        userUtils = mock(com.alex.api.user.user.UserUtils.class);
        when(userUtils.getOrgId()).thenReturn(20L);
        when(userUtils.getUserId()).thenReturn(1001L);
        budgetService = new FinanceBudgetInfoServiceImpl(financeBudgetInfoMapper, financeInfoService);
        org.springframework.test.util.ReflectionTestUtils.setField(budgetService, "userUtils", userUtils);
    }

    @Test
    @DisplayName("零花钱预算：当月有明确配置时直接采用当月数据，且isInherited为false")
    void testCurrentMonthConfigHit() {
        Long userId = 1001L;
        String month = "2026-10";

        FinanceBudgetInfo current = new FinanceBudgetInfo();
        current.setId(10L);
        current.setOrgId(20L);
        current.setBelongTo(userId);
        current.setBudgetMonth(month);
        current.setBudgetAmount(new BigDecimal("2000.00"));
        current.setCategoryCodes("餐饮,休闲娱乐");
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(current);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("600.00"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(5L)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, userId);

        Assertions.assertNotNull(status);
        Assertions.assertEquals(month, status.getBudgetMonth());
        Assertions.assertEquals(new BigDecimal("2000.00"), status.getBudgetAmount());
        Assertions.assertEquals(new BigDecimal("600.00"), status.getActualExpense());
        Assertions.assertEquals(new BigDecimal("1400.00"), status.getRemainingAmount());
        Assertions.assertEquals(new BigDecimal("30.0"), status.getUsagePercent());
        Assertions.assertFalse(status.getIsOverBudget());
        Assertions.assertFalse(status.getIsInherited());
        Assertions.assertEquals(Arrays.asList("餐饮", "休闲娱乐"), status.getCategoryCodes());
    }

    @Test
    @DisplayName("零花钱预算：当月未设置时自动继承上月/最近历史月的金额与分类范围，且isInherited为true")
    void testInheritanceFromPreviousMonth() {
        Long userId = 1001L;
        String month = "2026-10";

        // 当月未配置
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(null);

        // 9月份历史配置
        FinanceBudgetInfo septHistory = new FinanceBudgetInfo();
        septHistory.setId(9L);
        septHistory.setOrgId(20L);
        septHistory.setBelongTo(userId);
        septHistory.setBudgetMonth("2026-09");
        septHistory.setBudgetAmount(new BigDecimal("1800.00"));
        septHistory.setCategoryCodes("餐饮,日常交通");
        when(financeBudgetInfoMapper.selectLatestBefore(eq(20L), eq(month))).thenReturn(septHistory);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("900.00"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(8L)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, userId);

        Assertions.assertNotNull(status);
        Assertions.assertEquals(month, status.getBudgetMonth());
        Assertions.assertEquals(new BigDecimal("1800.00"), status.getBudgetAmount());
        Assertions.assertEquals(new BigDecimal("900.00"), status.getActualExpense());
        Assertions.assertEquals(new BigDecimal("900.00"), status.getRemainingAmount());
        Assertions.assertEquals(new BigDecimal("50.0"), status.getUsagePercent());
        Assertions.assertTrue(status.getIsInherited()); // 继承自 9 月
        Assertions.assertEquals(Arrays.asList("餐饮", "日常交通"), status.getCategoryCodes());
    }

    @Test
    @DisplayName("零花钱预算：纯新用户没有任何历史配置时返回0元默认值")
    void testNewUserDefault() {
        Long userId = 9999L;
        String month = "2026-10";

        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(null);
        when(financeBudgetInfoMapper.selectLatestBefore(eq(20L), eq(month))).thenReturn(null);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(BigDecimal.ZERO)
                .totalIncome(BigDecimal.ZERO)
                .totalCount(0L)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, userId);

        Assertions.assertNotNull(status);
        Assertions.assertEquals(BigDecimal.ZERO, status.getBudgetAmount());
        Assertions.assertEquals(BigDecimal.ZERO, status.getActualExpense());
        Assertions.assertEquals(BigDecimal.ZERO, status.getRemainingAmount());
        Assertions.assertFalse(status.getIsOverBudget());
        Assertions.assertFalse(status.getIsInherited());
        Assertions.assertTrue(status.getCategoryCodes().isEmpty());
    }

    @Test
    @DisplayName("零花钱预算：支出超过预算时正确标记isOverBudget为true且剩余为负数")
    void testExpenseOverBudget() {
        Long userId = 1001L;
        String month = "2026-10";

        FinanceBudgetInfo current = new FinanceBudgetInfo();
        current.setOrgId(20L);
        current.setBelongTo(userId);
        current.setBudgetMonth(month);
        current.setBudgetAmount(new BigDecimal("1000.00"));
        current.setCategoryCodes("餐饮");
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(current);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("1250.00")) // 超支 250
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, userId);

        Assertions.assertNotNull(status);
        Assertions.assertEquals(new BigDecimal("-250.00"), status.getRemainingAmount());
        Assertions.assertEquals(new BigDecimal("125.0"), status.getUsagePercent());
        Assertions.assertTrue(status.getIsOverBudget());
    }

    @Test
    @DisplayName("保存配置：已有记录时执行updateById，无记录时执行insert")
    void testSaveMonthlyBudget() {
        Long userId = 1001L;
        String month = "2026-10";

        FinanceBudgetSaveReq req = FinanceBudgetSaveReq.builder()
                .belongTo(userId)
                .budgetMonth(month)
                .budgetAmount(new BigDecimal("3000.00"))
                .categoryCodes(Arrays.asList("餐饮", "娱乐", "数码"))
                .build();

        // 场景 A: 已存在记录
        FinanceBudgetInfo exist = new FinanceBudgetInfo();
        exist.setId(88L);
        exist.setOrgId(20L);
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(exist);

        Boolean result = budgetService.saveMonthlyBudget(req);
        Assertions.assertTrue(result);
        verify(financeBudgetInfoMapper, times(1)).updateById(exist);
        Assertions.assertEquals("餐饮,娱乐,数码", exist.getCategoryCodes());
        Assertions.assertEquals(new BigDecimal("3000.00"), exist.getBudgetAmount());

        // 场景 B: 不存在记录
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq("2026-11"))).thenReturn(null);
        req.setBudgetMonth("2026-11");
        budgetService.saveMonthlyBudget(req);
        verify(financeBudgetInfoMapper, times(1)).insert(any(FinanceBudgetInfo.class));
    }

    @Test
    @DisplayName("容错与纯净过滤：历史脏数据若包含'支出'伪分类，应被智能剔除，不影响实际消费统计")
    void testSanitizeCategoryCodesWithIncomeExpense() {
        Long userId = 1001L;
        String month = "2026-10";

        // 模拟用户历史误配置：分类存入了 "支出"
        FinanceBudgetInfo current = new FinanceBudgetInfo();
        current.setId(11L);
        current.setOrgId(20L);
        current.setBelongTo(userId);
        current.setBudgetMonth(month);
        current.setBudgetAmount(new BigDecimal("3000.00"));
        current.setCategoryCodes("支出"); // 误把"支出"存入
        current.setIncomeAndExpenses("expense");
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(current);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("19.31"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(2L)
                .build();

        // 验证 queryVo 没有被 typeCodes = ['支出'] 污染，typeCodes 应该为空从而统计全部
        ArgumentCaptor<FinanceInfoVo> voCaptor = ArgumentCaptor.forClass(FinanceInfoVo.class);
        when(financeInfoService.getFinanceSummary(voCaptor.capture())).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, userId);

        Assertions.assertNotNull(status);
        Assertions.assertEquals(new BigDecimal("19.31"), status.getActualExpense());
        Assertions.assertEquals("expense", status.getIncomeAndExpenses());
        // 伪分类"支出"被剔除，categoryCodes 变为空
        Assertions.assertTrue(status.getCategoryCodes().isEmpty());
        // queryVo 的 typeCodes 应该为 null 或空
        Assertions.assertNull(voCaptor.getValue().getTypeCodes());
        Assertions.assertEquals("expense", voCaptor.getValue().getIncomeAndExpenses());
    }

    @Test
    @DisplayName("收支多选：同时选择支出与收入时合并统计总额")
    void testMultiDirectionExpenseAndIncome() {
        Long userId = 1001L;
        String month = "2026-10";

        FinanceBudgetInfo current = new FinanceBudgetInfo();
        current.setId(12L);
        current.setOrgId(20L);
        current.setBelongTo(userId);
        current.setBudgetMonth(month);
        current.setBudgetAmount(new BigDecimal("5000.00"));
        current.setCategoryCodes("餐饮,工资");
        current.setIncomeAndExpenses("expense,income");
        when(financeBudgetInfoMapper.selectByMonth(eq(20L), eq(month))).thenReturn(current);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("1200.00"))
                .totalIncome(new BigDecimal("3500.00"))
                .totalCount(10L)
                .build();

        ArgumentCaptor<FinanceInfoVo> voCaptor = ArgumentCaptor.forClass(FinanceInfoVo.class);
        when(financeInfoService.getFinanceSummary(voCaptor.capture())).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, userId);

        Assertions.assertNotNull(status);
        // 双选时求和: 1200 + 3500 = 4700.00
        Assertions.assertEquals(new BigDecimal("4700.00"), status.getActualExpense());
        Assertions.assertEquals("expense,income", status.getIncomeAndExpenses());
        Assertions.assertNull(voCaptor.getValue().getIncomeAndExpenses()); // 双选时不限制单个收支方向
    }

    @Test
    @DisplayName("家庭组/全机构聚合：未指定单人时(belongTo为null)，queryVo.belongTo保持为null交给@DataPermission全员聚合")
    void testFamilyGroupOrgWideBudgetAggregationWhenBelongToNull() {
        String month = "2026-10";

        // 模拟全局/家庭组默认预算
        FinanceBudgetInfo groupBudget = new FinanceBudgetInfo();
        groupBudget.setId(15L);
        groupBudget.setBudgetMonth(month);
        groupBudget.setBudgetAmount(new BigDecimal("3000.00"));
        groupBudget.setIncomeAndExpenses("expense,income");
        when(financeBudgetInfoMapper.selectByMonth(any(), eq(month))).thenReturn(groupBudget);

        // 模拟同家庭组下全员合计数据（臭屁宝+小袋子）
        FinanceSummaryVo groupSummary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("19.31"))
                .totalIncome(new BigDecimal("6139.71"))
                .totalCount(3L)
                .build();

        ArgumentCaptor<FinanceInfoVo> voCaptor = ArgumentCaptor.forClass(FinanceInfoVo.class);
        when(financeInfoService.getFinanceSummary(voCaptor.capture())).thenReturn(groupSummary);

        // 入参 belongTo 传 null，表示全家庭组统计
        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, null);

        Assertions.assertNotNull(status);
        // 全家庭组总流水：19.31 + 6139.71 = 6159.02
        Assertions.assertEquals(new BigDecimal("6159.02"), status.getActualExpense());
        // 关键断言：queryVo 的 belongTo 必须为 null，确保触发底层 @DataPermission 机构过滤
        Assertions.assertNull(voCaptor.getValue().getBelongTo());
    }

    @Test
    @DisplayName("家庭组机构自动解析：当orgId为null且userUtils可用时，自动解析当前登录用户所在机构ID")
    void testUserUtilsAutoResolutionForOrgIdWhenOrgIdNull() {
        String month = "2026-10";
        Long mockOrgId = 99L;

        com.alex.api.user.user.UserUtils mockUserUtils = mock(com.alex.api.user.user.UserUtils.class);
        when(mockUserUtils.getOrgId()).thenReturn(mockOrgId);
        org.springframework.test.util.ReflectionTestUtils.setField(budgetService, "userUtils", mockUserUtils);

        FinanceBudgetInfo orgBudget = new FinanceBudgetInfo();
        orgBudget.setId(18L);
        orgBudget.setOrgId(mockOrgId);
        orgBudget.setBudgetMonth(month);
        orgBudget.setBudgetAmount(new BigDecimal("1000.00"));
        orgBudget.setIncomeAndExpenses("expense");
        when(financeBudgetInfoMapper.selectByMonth(eq(mockOrgId), eq(month))).thenReturn(orgBudget);

        FinanceSummaryVo summary = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("200.00"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(2L)
                .build();
        when(financeInfoService.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(summary);

        FinanceBudgetStatusVo status = budgetService.getMonthlyBudgetStatus(month, null, null);

        Assertions.assertNotNull(status);
        Assertions.assertEquals(new BigDecimal("1000.00"), status.getBudgetAmount());
        Assertions.assertEquals(mockOrgId, status.getOrgId());
        verify(mockUserUtils, times(1)).getOrgId();
        verify(financeBudgetInfoMapper).selectByMonth(eq(mockOrgId), eq(month));
    }
}

