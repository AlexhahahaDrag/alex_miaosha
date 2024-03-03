package com.alex.finance.controller.shopFinanceAnalysis;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceAnalysisVo;
import com.alex.base.common.Result;
import com.alex.finance.service.shopFinanceAnalysis.ShopFinanceAnalysisService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * author: majf
 * createDate: 2024/3/2 22:17
 * description: 商店财务分析业务控制
 * version: 1.0.0
 */
@ApiSort(105)
@Api(value = "商店财务分析相关接口", tags = {"商店财务分析相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-finance-analysis")
public class ShopFinanceAnalysisController {

    private final ShopFinanceAnalysisService shopFinanceAnalysisService;

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "获取天收入明细", notes = "获取天收入明细", response = Result.class)
    @GetMapping(value = "/getDayShopFinanceInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "searchDate", required = true)}
    )
    public Result<List<ShopFinanceAnalysisVo>> getDayShopFinanceInfo(@RequestParam(value = "searchDate") String searchDate) {
        return Result.success(shopFinanceAnalysisService.getDayShopFinanceInfo(searchDate));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "获取月收入明细", notes = "获取月收入明细", response = Result.class)
    @GetMapping(value = "/getMonthShopFinanceInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "searchDate", required = true)}
    )
    public Result<List<ShopFinanceAnalysisVo>> getMonthShopFinanceInfo(@RequestParam(value = "searchDate") String searchDate) {
        return Result.success(shopFinanceAnalysisService.getMonthShopFinanceInfo(searchDate));
    }
}