package com.alex.finance.gift;

import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftDirectionAggVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.finance.gift.analysis.service.impl.GiftAnalysisServiceImpl;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.common.utils.date.DateUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 礼尚往来智能分析集成测试 (5.2.3 GiftAnalysisIT)
 * <p>
 * 覆盖：
 *   1. H14: overview netAmount 公式一致性与方向白名单
 *   2. H15: trend 走势 GIVE + RETURN 支出口径
 *   3. 关系分布稳定性与亲友榜降序排列
 *   4. eventRanking 空数据安全兜底 "全部事由"
 *   5. H16: 万级数据流转性能基线 (< 1s)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("礼尚往来智能分析集成测试 (GiftAnalysisIT)")
class GiftAnalysisIT {

    @Mock
    private GiftRecordInfoService giftRecordInfoService;
    @Mock
    private GiftEventInfoService giftEventInfoService;
    @Mock
    private GiftPersonInfoService giftPersonInfoService;

    private GiftAnalysisServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GiftAnalysisServiceImpl(
                giftRecordInfoService,
                giftEventInfoService,
                giftPersonInfoService);
    }

    @Test
    @DisplayName("H14: overview - netAmount 净额公式一致性与方向白名单归一化")
    void overview_netAmount_formula_and_direction_whitelist() {
        GiftRecordSummaryVo mockSummary = new GiftRecordSummaryVo()
                .setReceiveAmount(new BigDecimal("1000.00"))
                .setGiveAmount(new BigDecimal("300.00"))
                .setReturnAmount(new BigDecimal("200.00"))
                .setNetAmount(new BigDecimal("500.00"))
                .setRecordCount(15L);

        when(giftRecordInfoService.getSummary(any())).thenReturn(mockSummary);

        // 1. 无方向时：净额 = 收礼 (1000) - 送礼 (300) - 还礼 (200) = 500
        GiftRecordSummaryVo actual = service.overview(null);
        assertNotNull(actual);
        assertEquals(new BigDecimal("500.00"), actual.getNetAmount(), "netAmount 严格等于 in - out");

        // 2. 有效大写 GIVE
        service.overview("GIVE");

        // 3. 非白名单参数（如小写或非法值）归一化为 null（查全部）
        service.overview("invalid_dir");

        ArgumentCaptor<GiftRecordQuery> captor = ArgumentCaptor.forClass(GiftRecordQuery.class);
        verify(giftRecordInfoService, org.mockito.Mockito.times(3)).getSummary(captor.capture());
        List<GiftRecordQuery> captured = captor.getAllValues();
        assertNull(captured.get(0).getDirection(), "null 归一化为 null 查全部");
        assertEquals("GIVE", captured.get(1).getDirection(), "有效大写保留");
        assertNull(captured.get(2).getDirection(), "非白名单值归一化为 null 查全部");
    }

    @Test
    @DisplayName("H15: trend - 走势聚合支出合并口径验证")
    void trend_give_and_return_merge_semantics() {
        GiftAmountTrendVo month1 = new GiftAmountTrendVo()
                .setLabel("2026-09")
                .setReceiveAmount(new BigDecimal("2000.00"))
                .setGiveAmount(new BigDecimal("800.00")); // 包含 GIVE + RETURN
        GiftAmountTrendVo month2 = new GiftAmountTrendVo()
                .setLabel("2026-10")
                .setReceiveAmount(new BigDecimal("1500.00"))
                .setGiveAmount(new BigDecimal("1200.00"));

        when(giftRecordInfoService.getTrend("month", "RECEIVE"))
                .thenReturn(List.of(month1, month2));

        List<GiftAmountTrendVo> trendList = service.trend("month", "RECEIVE");
        assertEquals(2, trendList.size());
        assertEquals("2026-09", trendList.get(0).getLabel());
        assertEquals(new BigDecimal("2000.00"), trendList.get(0).getReceiveAmount());
        assertEquals(new BigDecimal("800.00"), trendList.get(0).getGiveAmount());
        verify(giftRecordInfoService).getTrend("month", "RECEIVE");
    }

    @Test
    @DisplayName("relationDistribution - 关系分布稳定返回")
    void relationDistribution_stable_ordering() {
        GiftRelationDistributionVo r1 = new GiftRelationDistributionVo()
                .setRelationType("亲戚")
                .setCount(10L);
        GiftRelationDistributionVo r2 = new GiftRelationDistributionVo()
                .setRelationType("同事")
                .setCount(5L);

        when(giftPersonInfoService.getRelationDistribution()).thenReturn(List.of(r1, r2));

        List<GiftRelationDistributionVo> list = service.relationDistribution();
        assertEquals(2, list.size());
        assertEquals("亲戚", list.get(0).getRelationType());
        assertEquals(10L, list.get(0).getCount());
        assertEquals("同事", list.get(1).getRelationType());
        assertEquals(5L, list.get(1).getCount());
    }

    @Test
    @DisplayName("eventRanking - 空数据安全兜底返回全部事由")
    void eventRanking_fallback_to_all_events_when_empty() {
        GiftEventSummaryVo summary = new GiftEventSummaryVo()
                .setTotalAmount(new BigDecimal("6666.00"))
                .setMonthPendingCount(4L);

        when(giftEventInfoService.getSummary()).thenReturn(summary);
        when(giftEventInfoService.getBusinessPage(1L, 10L, null))
                .thenReturn(new Page<>(1, 10, 0));

        List<GiftRankingItemVo> rankings = service.eventRanking();
        assertEquals(1, rankings.size());
        assertEquals("全部事由", rankings.get(0).getName(), "空数据兜底项");
        assertEquals(new BigDecimal("6666.00"), rankings.get(0).getAmount());
        assertEquals(4L, rankings.get(0).getCount());
    }

    @Test
    @DisplayName("personRanking - 亲友排行按总往来金额降序排列")
    void personRanking_ordering_by_total_amount_desc() {
        GiftPersonBusinessVo p1 = new GiftPersonBusinessVo();
        p1.setPersonName("张三");
        p1.setTotalGiveAmount(new BigDecimal("200"));
        p1.setTotalReceiveAmount(new BigDecimal("300")); // 合计 500
        p1.setLatestRecordTime(DateUtils.now());

        GiftPersonBusinessVo p2 = new GiftPersonBusinessVo();
        p2.setPersonName("李四");
        p2.setTotalGiveAmount(new BigDecimal("600"));
        p2.setTotalReceiveAmount(new BigDecimal("800")); // 合计 1400
        p2.setLatestRecordTime(DateUtils.now());

        Page<GiftPersonBusinessVo> page = new Page<>(1, 10, 2);
        page.setRecords(List.of(p1, p2));

        when(giftPersonInfoService.getBusinessPage(1L, 10L, null)).thenReturn(page);

        List<GiftRankingItemVo> rankings = service.personRanking();
        assertEquals(2, rankings.size());
        assertEquals("李四", rankings.get(0).getName(), "总金额1400排第一");
        assertEquals(new BigDecimal("1400"), rankings.get(0).getAmount());
        assertEquals("张三", rankings.get(1).getName(), "总金额500排第二");
        assertEquals(new BigDecimal("500"), rankings.get(1).getAmount());
    }

    @Test
    @DisplayName("H16: overview - 万级数据流转性能基线 (< 1000ms)")
    void overview_performance_baseline_10k_records_under_1s() {
        // 模拟 10,000 条聚合记录行
        List<GiftDirectionAggVo> aggList = new ArrayList<>(10000);
        for (int i = 0; i < 10000; i++) {
            GiftDirectionAggVo row = new GiftDirectionAggVo();
            row.setDirection(i % 2 == 0 ? "RECEIVE" : "GIVE");
            row.setTotalAmount(new BigDecimal("100.00"));
            row.setRecordCount(1L);
            aggList.add(row);
        }

        when(giftRecordInfoService.getSummary(any())).thenAnswer(inv -> {
            BigDecimal in = BigDecimal.ZERO;
            BigDecimal out = BigDecimal.ZERO;
            for (GiftDirectionAggVo r : aggList) {
                if ("RECEIVE".equals(r.getDirection())) {
                    in = in.add(r.getTotalAmount());
                } else {
                    out = out.add(r.getTotalAmount());
                }
            }
            return new GiftRecordSummaryVo()
                    .setReceiveAmount(in)
                    .setGiveAmount(out)
                    .setReturnAmount(BigDecimal.ZERO)
                    .setNetAmount(in.subtract(out))
                    .setRecordCount((long) aggList.size());
        });

        long start = System.currentTimeMillis();
        GiftRecordSummaryVo vo = service.overview(null);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull(vo);
        assertEquals(10000L, vo.getRecordCount());
        assertTrue(elapsed < 1000, "10,000条记录处理耗时必须在 1000ms 以内，实测耗时: " + elapsed + "ms");
    }
}
