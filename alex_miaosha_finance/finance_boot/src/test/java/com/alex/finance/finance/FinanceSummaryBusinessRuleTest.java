package com.alex.finance.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.FinanceSummaryVo;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.finance.service.impl.FinanceInfoServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinanceSummaryBusinessRuleTest {

    @Test
    void testSummaryCalculatesBalanceCorrectly() {
        FinanceInfoMapper mapper = mock(FinanceInfoMapper.class);
        FinanceInfoServiceImp service = new FinanceInfoServiceImp(mapper, null, null);

        FinanceSummaryVo mockAgg = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("500.00"))
                .totalIncome(new BigDecimal("1200.00"))
                .totalCount(15L)
                .build();

        when(mapper.getFinanceSummary(any())).thenReturn(mockAgg);

        FinanceSummaryVo result = service.getFinanceSummary(new FinanceInfoVo());
        assertNotNull(result);
        assertEquals(new BigDecimal("1200.00"), result.getTotalIncome());
        assertEquals(new BigDecimal("500.00"), result.getTotalExpense());
        assertEquals(new BigDecimal("700.00"), result.getTotalBalance());
        assertEquals(15L, result.getTotalCount());
    }

    @Test
    void testSummaryHandlesExpenseOnlyCorrectly() {
        FinanceInfoMapper mapper = mock(FinanceInfoMapper.class);
        FinanceInfoServiceImp service = new FinanceInfoServiceImp(mapper, null, null);

        FinanceSummaryVo mockAgg = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("350.50"))
                .totalIncome(BigDecimal.ZERO)
                .totalCount(8L)
                .build();

        when(mapper.getFinanceSummary(any())).thenReturn(mockAgg);

        FinanceSummaryVo result = service.getFinanceSummary(new FinanceInfoVo().setIncomeAndExpenses("expense"));
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(new BigDecimal("350.50"), result.getTotalExpense());
        assertEquals(new BigDecimal("-350.50"), result.getTotalBalance());
        assertEquals(8L, result.getTotalCount());
    }

    @Test
    void testSummaryHandlesNullMapperResultGracefully() {
        FinanceInfoMapper mapper = mock(FinanceInfoMapper.class);
        FinanceInfoServiceImp service = new FinanceInfoServiceImp(mapper, null, null);

        when(mapper.getFinanceSummary(any())).thenReturn(null);

        FinanceSummaryVo result = service.getFinanceSummary(new FinanceInfoVo());
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpense());
        assertEquals(BigDecimal.ZERO, result.getTotalBalance());
        assertEquals(0L, result.getTotalCount());
    }

    @Test
    void testSummaryNetBalanceAccountingLogic() {
        FinanceInfoMapper mapper = mock(FinanceInfoMapper.class);
        FinanceInfoServiceImp service = new FinanceInfoServiceImp(mapper, null, null);

        // 真实日常账单剔除转账后的数值模拟：支出 857,538.10，收入 984,349.51，结余 +126,811.41，笔数 4,913
        FinanceSummaryVo mockAgg = FinanceSummaryVo.builder()
                .totalExpense(new BigDecimal("857538.10"))
                .totalIncome(new BigDecimal("984349.51"))
                .totalCount(4913L)
                .build();

        when(mapper.getFinanceSummary(any())).thenReturn(mockAgg);

        FinanceSummaryVo result = service.getFinanceSummary(new FinanceInfoVo());
        assertNotNull(result);
        assertEquals(new BigDecimal("984349.51"), result.getTotalIncome());
        assertEquals(new BigDecimal("857538.10"), result.getTotalExpense());
        assertEquals(new BigDecimal("126811.41"), result.getTotalBalance());
        assertEquals(4913L, result.getTotalCount());
    }
}
