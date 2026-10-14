package com.alex.finance.prepaidCardInfoT;

import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardConsumeVo;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidDashboardOverviewVo;
import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.date.DateUtils;
import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.alex.finance.prepaidCardInfoT.service.impl.PrepaidCardInfoTServiceImp;
import com.alex.finance.prepaidConsumeRecordT.service.PrepaidConsumeRecordTService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PrepaidCardInfoTServiceTest {

    private PrepaidCardInfoTMapper prepaidCardInfoTMapper;
    private PrepaidConsumeRecordTService prepaidConsumeRecordTService;
    private PrepaidCardInfoTServiceImp service;

    @BeforeEach
    void setUp() {
        prepaidCardInfoTMapper = mock(PrepaidCardInfoTMapper.class);
        prepaidConsumeRecordTService = mock(PrepaidConsumeRecordTService.class);
        service = new PrepaidCardInfoTServiceImp(prepaidCardInfoTMapper, prepaidConsumeRecordTService);
    }

    @Test
    @DisplayName("dashboardOverview：卡列表为空时返回全零总览")
    void testDashboardOverviewEmptyCards() {
        when(prepaidCardInfoTMapper.getList(any())).thenReturn(Collections.emptyList());

        PrepaidDashboardOverviewVo overview = service.dashboardOverview(1001L);

        Assertions.assertNotNull(overview);
        Assertions.assertEquals(0, overview.getTotalCards());
        Assertions.assertEquals(BigDecimal.ZERO, overview.getTotalBalance());
        Assertions.assertEquals(BigDecimal.ZERO, overview.getMonthExpense());
        Assertions.assertEquals(BigDecimal.ZERO, overview.getMonthRecharge());
    }

    @Test
    @DisplayName("dashboardOverview：卡列表存在数据时正确汇总卡数、总余额、月度收支与新增卡环比")
    void testDashboardOverviewWithCards() {
        Long cardId = 88L;
        PrepaidCardInfoTVo card1 = new PrepaidCardInfoTVo();
        card1.setId(cardId);
        card1.setCurrentBalance(new BigDecimal("500.00"));
        card1.setCreateTime(DateUtils.now());

        when(prepaidCardInfoTMapper.getList(any())).thenReturn(Collections.singletonList(card1));

        Map<String, Object> thisMonthSum = new HashMap<>();
        thisMonthSum.put("expenseAmount", new BigDecimal("120.00"));
        thisMonthSum.put("rechargeAmount", new BigDecimal("300.00"));

        Map<String, Object> lastMonthSum = new HashMap<>();
        lastMonthSum.put("expenseAmount", new BigDecimal("80.00"));
        lastMonthSum.put("rechargeAmount", new BigDecimal("200.00"));

        when(prepaidConsumeRecordTService.sumExpenseAndRecharge(eq(cardId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(thisMonthSum)
                .thenReturn(lastMonthSum);

        PrepaidDashboardOverviewVo overview = service.dashboardOverview(1001L);

        Assertions.assertNotNull(overview);
        Assertions.assertEquals(1, overview.getTotalCards());
        Assertions.assertEquals(new BigDecimal("500.00"), overview.getTotalBalance());
        Assertions.assertEquals(new BigDecimal("120.00"), overview.getMonthExpense());
        Assertions.assertEquals(new BigDecimal("300.00"), overview.getMonthRecharge());
        Assertions.assertEquals(new BigDecimal("40.00"), overview.getMonthExpenseMoM());
        Assertions.assertEquals(new BigDecimal("100.00"), overview.getMonthRechargeMoM());
        Assertions.assertEquals(1, overview.getTotalCardsMoM());
    }

    @Test
    @DisplayName("consumeAndRecharge：充值时未填写描述应默认设为'充值'且金额为正数")
    void testRechargeDefaultDescription() {
        Long cardId = 88L;
        PrepaidCardInfoTVo card = new PrepaidCardInfoTVo();
        card.setId(cardId);
        card.setCurrentBalance(new BigDecimal("100.00"));
        when(prepaidCardInfoTMapper.queryPrepaidCardInfoT(cardId)).thenReturn(card);
        when(prepaidCardInfoTMapper.updateById(any(PrepaidCardInfoT.class))).thenReturn(1);

        PrepaidCardConsumeVo req = new PrepaidCardConsumeVo();
        req.setId(cardId);
        req.setType("recharge");
        req.setConsumeAmount(new BigDecimal("200.00"));

        Boolean result = service.consumeAndRecharge(req);
        Assertions.assertTrue(result);

        ArgumentCaptor<PrepaidConsumeRecordTVo> recordCaptor = ArgumentCaptor.forClass(PrepaidConsumeRecordTVo.class);
        verify(prepaidConsumeRecordTService).addPrepaidConsumeRecordT(recordCaptor.capture());
        PrepaidConsumeRecordTVo savedRecord = recordCaptor.getValue();
        Assertions.assertEquals("充值", savedRecord.getDescription());
        Assertions.assertEquals(new BigDecimal("200.00"), savedRecord.getAmount());
    }

    @Test
    @DisplayName("consumeAndRecharge：消费时未填写描述应默认设为'消费'且金额为负数")
    void testConsumeDefaultDescription() {
        Long cardId = 88L;
        PrepaidCardInfoTVo card = new PrepaidCardInfoTVo();
        card.setId(cardId);
        card.setCurrentBalance(new BigDecimal("500.00"));
        when(prepaidCardInfoTMapper.queryPrepaidCardInfoT(cardId)).thenReturn(card);
        when(prepaidCardInfoTMapper.updateById(any(PrepaidCardInfoT.class))).thenReturn(1);

        PrepaidCardConsumeVo req = new PrepaidCardConsumeVo();
        req.setId(cardId);
        req.setType("consume");
        req.setConsumeAmount(new BigDecimal("150.00"));

        Boolean result = service.consumeAndRecharge(req);
        Assertions.assertTrue(result);

        ArgumentCaptor<PrepaidConsumeRecordTVo> recordCaptor = ArgumentCaptor.forClass(PrepaidConsumeRecordTVo.class);
        verify(prepaidConsumeRecordTService).addPrepaidConsumeRecordT(recordCaptor.capture());
        PrepaidConsumeRecordTVo savedRecord = recordCaptor.getValue();
        Assertions.assertEquals("消费", savedRecord.getDescription());
        Assertions.assertEquals(new BigDecimal("-150.00"), savedRecord.getAmount());
    }

    @Test
    @DisplayName("consumeAndRecharge：自定义描述优先保留")
    void testCustomDescriptionPreserved() {
        Long cardId = 88L;
        PrepaidCardInfoTVo card = new PrepaidCardInfoTVo();
        card.setId(cardId);
        card.setCurrentBalance(new BigDecimal("500.00"));
        when(prepaidCardInfoTMapper.queryPrepaidCardInfoT(cardId)).thenReturn(card);
        when(prepaidCardInfoTMapper.updateById(any(PrepaidCardInfoT.class))).thenReturn(1);

        PrepaidCardConsumeVo req = new PrepaidCardConsumeVo();
        req.setId(cardId);
        req.setType("consume");
        req.setConsumeAmount(new BigDecimal("50.00"));
        req.setDescription("买咖啡");

        Boolean result = service.consumeAndRecharge(req);
        Assertions.assertTrue(result);

        ArgumentCaptor<PrepaidConsumeRecordTVo> recordCaptor = ArgumentCaptor.forClass(PrepaidConsumeRecordTVo.class);
        verify(prepaidConsumeRecordTService).addPrepaidConsumeRecordT(recordCaptor.capture());
        Assertions.assertEquals("买咖啡", recordCaptor.getValue().getDescription());
    }

    @Test
    @DisplayName("consumeAndRecharge：未知操作类型抛出异常")
    void testUnknownTypeThrows() {
        PrepaidCardConsumeVo req = new PrepaidCardConsumeVo();
        req.setId(88L);
        req.setType("invalid_type");
        req.setConsumeAmount(new BigDecimal("50.00"));

        Assertions.assertThrows(FinanceException.class, () -> service.consumeAndRecharge(req));
    }
}
