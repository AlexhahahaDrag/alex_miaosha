package com.alex.finance.analysis;

import com.alex.api.finance.vo.financeAnalysis.AnalysisVo;
import com.alex.api.finance.vo.financeAnalysis.BalanceVo;
import com.alex.finance.analysis.mapper.FinanceAnalysisMapper;
import com.alex.finance.analysis.service.impl.FinanceAnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 集成测试：财务分析收支余额、环比与同比趋势计算、除零安全及跨年月份边界
 */
@ExtendWith(MockitoExtension.class)
class FinanceAnalysisTrendCalculationIT {

    @Mock
    private FinanceAnalysisMapper financeAnalysisMapper;

    private FinanceAnalysisServiceImpl financeAnalysisService;

    @BeforeEach
    void setUp() {
        financeAnalysisService = new FinanceAnalysisServiceImpl(financeAnalysisMapper);
    }

    @Test
    @DisplayName("跨年月份边界：1月份查询时 MoM 自动跨年计算为上年12月，YoY 为上年1月")
    void testBalanceCalculation_JanuaryCrossYearMomCalculation() {
        String searchDate = "2026-01";
        Long userId = 1001L;

        AnalysisVo currentItem = new AnalysisVo();
        currentItem.setTypeCode("wechat_pay");
        currentItem.setAmount(new BigDecimal("1000.00"));
        when(financeAnalysisMapper.getBalance(eq(userId), eq("2026-01"))).thenReturn(List.of(currentItem));

        AnalysisVo momItem = new AnalysisVo();
        momItem.setTypeCode("wechat_pay");
        momItem.setAmount(new BigDecimal("800.00"));
        // 关键断言点：MoM 必须查询 2025-12
        when(financeAnalysisMapper.getBalance(eq(userId), eq("2025-12"))).thenReturn(List.of(momItem));

        AnalysisVo yoyItem = new AnalysisVo();
        yoyItem.setTypeCode("wechat_pay");
        yoyItem.setAmount(new BigDecimal("500.00"));
        // 关键断言点：YoY 必须查询 2025-01
        when(financeAnalysisMapper.getBalance(eq(userId), eq("2025-01"))).thenReturn(List.of(yoyItem));

        BalanceVo balance = financeAnalysisService.getBalance(userId, searchDate);

        assertNotNull(balance);
        verify(financeAnalysisMapper).getBalance(eq(userId), eq("2025-12"));
        verify(financeAnalysisMapper).getBalance(eq(userId), eq("2025-01"));

        // 环比：(1000 - 800) / 800 = +25.0%
        assertEquals("+25.0%", balance.getMomTrend());
        // 同比：(1000 - 500) / 500 = +100.0%
        assertEquals("+100.0%", balance.getYoyTrend());
    }

    @Test
    @DisplayName("除零安全：基期为 0 时环比/同比趋势安全输出 +100% / -100% / 0.0% 杜绝除零崩溃")
    void testTrendCalculation_ZeroPreviousAmountSafeZeroDivision() {
        String searchDate = "2026-05";

        AnalysisVo currentItem = new AnalysisVo();
        currentItem.setTypeCode("alipay");
        currentItem.setAmount(new BigDecimal("600.00"));
        when(financeAnalysisMapper.getBalance(isNull(), eq("2026-05"))).thenReturn(List.of(currentItem));

        // 基期总额为 0
        when(financeAnalysisMapper.getBalance(isNull(), eq("2026-04"))).thenReturn(Collections.emptyList());
        when(financeAnalysisMapper.getBalance(isNull(), eq("2025-05"))).thenReturn(Collections.emptyList());

        BalanceVo balance = financeAnalysisService.getBalance(0L, searchDate);

        assertNotNull(balance);
        // 当前 > 0，基期 = 0 ➔ +100%
        assertEquals("+100%", balance.getMomTrend());
        assertEquals("+100%", balance.getYoyTrend());
    }

    @Test
    @DisplayName("人员ID规范化：belongTo 为 0 或负数时安全归一化为 null 汇总全员流水")
    void testNormalizeBelongTo_PassesNullToMapper() {
        when(financeAnalysisMapper.getBalance(isNull(), eq("2026-08"))).thenReturn(Collections.emptyList());

        financeAnalysisService.getBalance(0L, "2026-08");
        verify(financeAnalysisMapper, times(1)).getBalance(isNull(), eq("2026-08"));

        financeAnalysisService.getBalance(-5L, "2026-08");
        verify(financeAnalysisMapper, times(2)).getBalance(isNull(), eq("2026-08"));
    }

    @Test
    @DisplayName("收支明细与天/月花费统计：透传参数并由 Mapper 精确处理收支方向")
    void testDayAndMonthExpenseDelegation() {
        AnalysisVo vo1 = new AnalysisVo();
        vo1.setInfoDate("2026-10-01");
        vo1.setAmount(new BigDecimal("120.50"));

        when(financeAnalysisMapper.getDayExpense(eq(1001L), eq("2026-10"))).thenReturn(List.of(vo1));
        List<AnalysisVo> dayList = financeAnalysisService.getDayExpense(1001L, "2026-10");
        assertEquals(1, dayList.size());
        assertEquals(new BigDecimal("120.50"), dayList.get(0).getAmount());

        when(financeAnalysisMapper.getMonthExpense(eq(1001L), eq("2026"))).thenReturn(List.of(vo1));
        List<AnalysisVo> monthList = financeAnalysisService.getMonthExpense(1001L, "2026");
        assertEquals(1, monthList.size());
    }
}
