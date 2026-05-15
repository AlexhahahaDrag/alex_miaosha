package com.alex.finance.gift.analysis.controller;

import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.base.common.Result;
import com.alex.finance.gift.event.service.GiftEventInfoTService;
import com.alex.finance.gift.person.service.GiftPersonInfoTService;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApiSort(134)
@Api(value = "gift analysis api", tags = {"gift analysis api"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-analysis")
public class GiftAnalysisController {

    private final GiftRecordInfoTService giftRecordInfoTService;
    private final GiftEventInfoTService giftEventInfoTService;
    private final GiftPersonInfoTService giftPersonInfoTService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "gift analysis overview", response = Result.class)
    @GetMapping(value = "/overview")
    public Result<GiftRecordSummaryVo> overview() {
        return Result.success(giftRecordInfoTService.getSummary(new GiftRecordQuery()));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "gift analysis trend", response = Result.class)
    @GetMapping(value = "/trend")
    public Result<List<GiftAmountTrendVo>> trend() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<YearMonth, GiftAmountTrendVo> trendMap = giftRecordInfoTService.getList(new GiftRecordQuery()).stream()
                .filter(record -> record.getPayTime() != null)
                .collect(Collectors.groupingBy(
                        record -> YearMonth.from(record.getPayTime()),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), records -> {
                            BigDecimal giveAmount = sumByDirection(records, "GIVE").add(sumByDirection(records, "RETURN"));
                            BigDecimal receiveAmount = sumByDirection(records, "RECEIVE");
                            return new GiftAmountTrendVo()
                                    .setGiveAmount(giveAmount)
                                    .setReceiveAmount(receiveAmount);
                        })
                ));
        List<GiftAmountTrendVo> rows = trendMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().setLabel(entry.getKey().format(formatter)))
                .collect(Collectors.toList());
        return Result.success(rows);
    }

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "gift relation distribution", response = Result.class)
    @GetMapping(value = "/relation-distribution")
    public Result<List<GiftRelationDistributionVo>> relationDistribution() {
        List<GiftRelationDistributionVo> rows = giftPersonInfoTService.getList(new GiftPersonQuery()).stream()
                .collect(Collectors.groupingBy(
                        person -> person.getRelationType() == null ? "OTHER" : person.getRelationType(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new GiftRelationDistributionVo()
                        .setRelationType(entry.getKey())
                        .setCount(entry.getValue()))
                .sorted(Comparator.comparing(GiftRelationDistributionVo::getCount).reversed())
                .collect(Collectors.toList());
        return Result.success(rows);
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "gift event ranking", response = Result.class)
    @GetMapping(value = "/event-ranking")
    public Result<List<GiftRankingItemVo>> eventRanking() {
        GiftEventSummaryVo summary = giftEventInfoTService.getSummary();
        Page<GiftEventBusinessVo> page = giftEventInfoTService.getBusinessPage(1L, 10L, null);
        List<GiftRankingItemVo> rows = page.getRecords().stream()
                .map(event -> new GiftRankingItemVo()
                        .setName(event.getEventName())
                        .setAmount(defaultAmount(event.getTotalAmount()))
                        .setCount(defaultLong(event.getParticipantCount())))
                .sorted(Comparator.comparing(GiftRankingItemVo::getAmount).reversed())
                .collect(Collectors.toList());
        if (rows.isEmpty()) {
            rows = List.of(new GiftRankingItemVo()
                    .setName("全部事由")
                    .setAmount(defaultAmount(summary.getTotalAmount()))
                    .setCount(defaultLong(summary.getMonthPendingCount())));
        }
        return Result.success(rows);
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "gift person ranking", response = Result.class)
    @GetMapping(value = "/person-ranking")
    public Result<List<GiftRankingItemVo>> personRanking() {
        Page<GiftPersonBusinessVo> page = giftPersonInfoTService.getBusinessPage(1L, 10L, null);
        List<GiftRankingItemVo> rows = page.getRecords().stream()
                .map(person -> new GiftRankingItemVo()
                        .setName(person.getPersonName())
                        .setAmount(defaultAmount(person.getTotalGiveAmount()).add(defaultAmount(person.getTotalReceiveAmount())))
                        .setCount(person.getLatestRecordTime() == null ? 0L : 1L))
                .sorted(Comparator.comparing(GiftRankingItemVo::getAmount).reversed())
                .collect(Collectors.toList());
        return Result.success(rows);
    }

    private BigDecimal sumByDirection(List<GiftRecordInfoTVo> records, String direction) {
        return records.stream()
                .filter(record -> Objects.equals(direction, record.getDirection()))
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
