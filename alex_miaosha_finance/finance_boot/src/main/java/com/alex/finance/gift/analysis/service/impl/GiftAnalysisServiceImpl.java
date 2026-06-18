package com.alex.finance.gift.analysis.service.impl;

import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.finance.gift.analysis.service.GiftAnalysisService;
import com.alex.finance.gift.event.service.GiftEventInfoTService;
import com.alex.finance.gift.person.service.GiftPersonInfoTService;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftAnalysisServiceImpl implements GiftAnalysisService {

   private static final String DIRECTION_GIVE = "GIVE";
    private static final String DIRECTION_RECEIVE = "RECEIVE";
        private static final String DIRECTION_RETURN = "RETURN";

        private final GiftRecordInfoTService giftRecordInfoTService;
        private final GiftEventInfoTService giftEventInfoTService;
        private final GiftPersonInfoTService giftPersonInfoTService;

        @Override
        public GiftRecordSummaryVo overview() {
                return giftRecordInfoTService.getSummary(new GiftRecordQuery());
        }

        @Override
        public List<GiftAmountTrendVo> trend() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                Map<YearMonth, GiftAmountTrendVo> trendMap = giftRecordInfoTService.getList(new GiftRecordQuery())
                                .stream()
                                .filter(giftRecord -> giftRecord.getPayTime() != null)
                                .collect(Collectors.groupingBy(
                                                giftRecord -> YearMonth.from(giftRecord.getPayTime()),
                                                LinkedHashMap::new,
                                                Collectors.collectingAndThen(Collectors.toList(), records -> {
                                                        BigDecimal giveAmount = sumByDirection(records, DIRECTION_GIVE)
                                                                        .add(sumByDirection(records, DIRECTION_RETURN));
                                                        BigDecimal receiveAmount = sumByDirection(records,
                                                                        DIRECTION_RECEIVE);
                                                        return new GiftAmountTrendVo()
                                                                        .setGiveAmount(giveAmount)
                                                                        .setReceiveAmount(receiveAmount);
                                                })));
                return trendMap.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(entry -> entry.getValue().setLabel(entry.getKey().format(formatter)))
                                .toList();
        }

        @Override
        public List<GiftRelationDistributionVo> relationDistribution() {
                return giftPersonInfoTService.getList(new GiftPersonQuery()).stream()
                                .collect(Collectors.groupingBy(
                                                person -> person.getRelationType() == null ? "OTHER"
                                                                : person.getRelationType(),
                                                Collectors.counting()))
                                .entrySet()
                                .stream()
                                .map(entry -> new GiftRelationDistributionVo()
                                                .setRelationType(entry.getKey())
                                                .setCount(entry.getValue()))
                                .sorted(Comparator.comparing(GiftRelationDistributionVo::getCount).reversed())
                                .toList();
        }

        @Override
        public List<GiftRankingItemVo> eventRanking() {
                GiftEventSummaryVo summary = giftEventInfoTService.getSummary();
                Page<GiftEventBusinessVo> page = giftEventInfoTService.getBusinessPage(1L, 10L, null);
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
                Page<GiftPersonBusinessVo> page = giftPersonInfoTService.getBusinessPage(1L, 10L, null);
                return page.getRecords().stream()
                                .map(person -> new GiftRankingItemVo()
                                                .setName(person.getPersonName())
                                                .setAmount(defaultAmount(person.getTotalGiveAmount())
                                                                .add(defaultAmount(person.getTotalReceiveAmount())))
                                                .setCount(person.getLatestRecordTime() == null ? 0L : 1L))
                                .sorted(Comparator.comparing(GiftRankingItemVo::getAmount).reversed())
                                .toList();
        }

        private BigDecimal sumByDirection(List<GiftRecordInfoTVo> records, String direction) {
                return
                        records.stream()
            .filter(giftRecord -> Objects.equals(direction, giftRecord.getDirection()))
                                .map(GiftRecordInfoTVo::getAmount)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal defaultAmount(BigDecimal amount) {
                return amount == null ? BigDecimal.ZERO : amount;
        }

        private Long defaultLong(Long value) {
                return value == null ? 0L : value;
        }
}
