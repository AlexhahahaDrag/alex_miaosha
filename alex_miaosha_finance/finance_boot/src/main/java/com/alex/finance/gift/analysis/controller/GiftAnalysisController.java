package com.alex.finance.gift.analysis.controller;

import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.base.common.Result;
import com.alex.finance.gift.event.service.GiftEventInfoTService;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@ApiSort(134)
@Api(value = "gift analysis api", tags = {"gift analysis api"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-analysis")
public class GiftAnalysisController {

    private final GiftRecordInfoTService giftRecordInfoTService;
    private final GiftEventInfoTService giftEventInfoTService;

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
        return Result.success(List.of());
    }

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "gift relation distribution", response = Result.class)
    @GetMapping(value = "/relation-distribution")
    public Result<List<GiftRelationDistributionVo>> relationDistribution() {
        return Result.success(List.of());
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "gift event ranking", response = Result.class)
    @GetMapping(value = "/event-ranking")
    public Result<List<GiftRankingItemVo>> eventRanking() {
        GiftEventSummaryVo summary = giftEventInfoTService.getSummary();
        GiftRankingItemVo item = new GiftRankingItemVo().setName("全部事由").setAmount(summary.getTotalAmount()).setCount(summary.getMonthPendingCount());
        return Result.success(List.of(item));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "gift person ranking", response = Result.class)
    @GetMapping(value = "/person-ranking")
    public Result<List<GiftRankingItemVo>> personRanking() {
        return Result.success(List.of());
    }
}
