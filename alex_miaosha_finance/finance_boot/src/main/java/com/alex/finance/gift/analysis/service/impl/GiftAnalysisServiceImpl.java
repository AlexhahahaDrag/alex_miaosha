package com.alex.finance.gift.analysis.service.impl;

import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.finance.gift.analysis.service.GiftAnalysisService;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftAnalysisServiceImpl implements GiftAnalysisService {

    private static final String DIRECTION_GIVE = "GIVE";
    private static final String DIRECTION_RECEIVE = "RECEIVE";
    private static final String DIRECTION_RETURN = "RETURN";

    private final GiftRecordInfoService giftRecordInfoService;
    private final GiftEventInfoService giftEventInfoService;
    private final GiftPersonInfoService giftPersonInfoService;

    /**
     * 统计总览：direction 白名单过滤后走 getSummary 的 SQL 聚合。
     */
    @Override
    public GiftRecordSummaryVo overview(String direction) {
        GiftRecordQuery query = new GiftRecordQuery();
        query.setDirection(normalizeDirection(direction));
        return giftRecordInfoService.getSummary(query);
    }

    /**
     * 收支趋势：下沉为 SQL DATE_FORMAT 分组聚合（原实现全量拉记录按 YearMonth 内存分组）。
     */
    @Override
    public List<GiftAmountTrendVo> trend(String period, String direction) {
        return giftRecordInfoService.getTrend(period, normalizeDirection(direction));
    }

    /**
     * 关系分布：下沉为 SQL GROUP BY 聚合（原实现全量拉亲友内存分组）。
     */
    @Override
    public List<GiftRelationDistributionVo> relationDistribution() {
        return giftPersonInfoService.getRelationDistribution();
    }

    @Override
    public List<GiftRankingItemVo> eventRanking() {
        GiftEventSummaryVo summary = giftEventInfoService.getSummary();
        Page<GiftEventBusinessVo> page = giftEventInfoService.getBusinessPage(1L, 10L, null);
        List<GiftRankingItemVo> rows = page.getRecords().stream()
                .map(event -> new GiftRankingItemVo()
                        .setName(event.getEventName())
                        .setAmount(defaultAmount(event.getTotalAmount()))
                        .setCount(defaultLong(event.getParticipantCount())))
                .sorted(Comparator.comparing(GiftRankingItemVo::getAmount).reversed())
                .toList();
        if (rows.isEmpty()) {
            rows = List.of(new GiftRankingItemVo()
                    .setName("全部事由")
                    .setAmount(defaultAmount(summary.getTotalAmount()))
                    .setCount(defaultLong(summary.getMonthPendingCount())));
        }
        return rows;
    }

    @Override
    public List<GiftRankingItemVo> personRanking() {
        Page<GiftPersonBusinessVo> page = giftPersonInfoService.getBusinessPage(1L, 10L, null);
        return page.getRecords().stream()
                .map(person -> new GiftRankingItemVo()
                        .setName(person.getPersonName())
                        .setAmount(defaultAmount(person.getTotalGiveAmount())
                                .add(defaultAmount(person.getTotalReceiveAmount())))
                        .setCount(person.getLatestRecordTime() == null ? 0L : 1L))
                .sorted(Comparator.comparing(GiftRankingItemVo::getAmount).reversed())
                .toList();
    }

    /** 方向参数白名单：非法值按"全部"处理 */
    private String normalizeDirection(String direction) {
        if (DIRECTION_GIVE.equals(direction)
                || DIRECTION_RECEIVE.equals(direction)
                || DIRECTION_RETURN.equals(direction)) {
            return direction;
        }
        return null;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
