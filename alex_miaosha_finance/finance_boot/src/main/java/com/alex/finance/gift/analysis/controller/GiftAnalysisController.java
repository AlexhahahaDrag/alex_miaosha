package com.alex.finance.gift.analysis.controller;

import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftPersonBalanceVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationReq;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationVo;
import com.alex.base.common.Result;
import com.alex.finance.gift.analysis.service.GiftAnalysisService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@ApiSort(134)
@Api(value = "gift analysis api", tags = { "gift analysis api" })
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-analysis")
public class GiftAnalysisController {

    private final GiftAnalysisService giftAnalysisService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "gift analysis overview", response = Result.class)
    @GetMapping(value = "/overview")
    public Result<GiftRecordSummaryVo> overview(
            @RequestParam(value = "direction", required = false) String direction) {
        return Result.success(giftAnalysisService.overview(direction));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "gift analysis trend", response = Result.class)
    @GetMapping(value = "/trend")
    public Result<List<GiftAmountTrendVo>> trend(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "direction", required = false) String direction) {
        return Result.success(giftAnalysisService.trend(period, direction));
    }

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "gift relation distribution", response = Result.class)
    @GetMapping(value = "/relation-distribution")
    public Result<List<GiftRelationDistributionVo>> relationDistribution() {
        return Result.success(giftAnalysisService.relationDistribution());
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "gift event ranking", response = Result.class)
    @GetMapping(value = "/event-ranking")
    public Result<List<GiftRankingItemVo>> eventRanking() {
        return Result.success(giftAnalysisService.eventRanking());
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "gift person ranking", response = Result.class)
    @GetMapping(value = "/person-ranking")
    public Result<List<GiftRankingItemVo>> personRanking() {
        return Result.success(giftAnalysisService.personRanking());
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "亲友互赠收支平衡矩阵", response = Result.class)
    @GetMapping(value = "/balance-matrix")
    public Result<List<GiftPersonBalanceVo>> balanceMatrix(
            @RequestParam(value = "relationType", required = false) String relationType,
            @RequestParam(value = "searchKey", required = false) String searchKey) {
        return Result.success(giftAnalysisService.balanceMatrix(relationType, searchKey));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "AI 智能回礼档位测算推荐", response = Result.class)
    @PostMapping(value = "/ai/recommend-return-gift")
    public Result<GiftReturnRecommendationVo> recommendReturnGift(
            @RequestBody GiftReturnRecommendationReq req) {
        return Result.success(giftAnalysisService.recommendReturnGift(req));
    }

    @ApiOperationSupport(order = 80, author = "alex")
    @ApiOperation(value = "导出亲友互赠收支平衡矩阵", response = void.class)
    @PostMapping(value = "/export-matrix")
    public void exportMatrix(
            @RequestParam(value = "relationType", required = false) String relationType,
            @RequestParam(value = "searchKey", required = false) String searchKey,
            javax.servlet.http.HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=gift_balance_matrix.xlsx");
        giftAnalysisService.exportBalanceMatrix(relationType, searchKey, response);
    }
}

