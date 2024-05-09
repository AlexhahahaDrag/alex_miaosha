package com.alex.finance.controller.shopFinanceAnalysis;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceAnalysisVo;
import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceChainYearVo;
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
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "searchDate", required = true, dataTypeClass = String.class)}
    )
    public Result<List<ShopFinanceAnalysisVo>> getDayShopFinanceInfo(@RequestParam(value = "searchDate") String searchDate) {
        return Result.success(shopFinanceAnalysisService.getDayShopFinanceInfo(searchDate));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "获取月收入明细", notes = "获取月收入明细", response = Result.class)
    @GetMapping(value = "/getMonthShopFinanceInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "searchDate", required = true, dataTypeClass = String.class)}
    )
    public Result<List<ShopFinanceAnalysisVo>> getMonthShopFinanceInfo(@RequestParam(value = "searchDate") String searchDate) {
        return Result.success(shopFinanceAnalysisService.getMonthShopFinanceInfo(searchDate));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "获取支付方式分布", notes = "获取支付方式分布", response = Result.class)
    @GetMapping(value = "/getPayWayInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "searchDate", required = true, dataTypeClass = String.class)}
    )
    public Result<List<ShopFinanceAnalysisVo>> getPayWayInfo(@RequestParam(value = "searchDate") String searchDate) {
        return Result.success(shopFinanceAnalysisService.getPayWayInfo(searchDate));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "获取商品名称分布", notes = "获取商品名称分布", response = Result.class)
    @GetMapping(value = "/getShopNameInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "searchDate", required = true, dataTypeClass = String.class)}
    )
    public Result<List<ShopFinanceAnalysisVo>> getShopNameInfo(@RequestParam(value = "searchDate") String searchDate) {
        return Result.success(shopFinanceAnalysisService.getShopNameInfo(searchDate));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "获取商品销售环同比", notes = "获取商品销售环同比", response = Result.class)
    @GetMapping(value = "/getChainAndYear")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "startDate", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "endDate", required = true, dataTypeClass = String.class)}
    )
    public Result<ShopFinanceChainYearVo> getChainAndYear(@RequestParam(value = "startDate") String startDate,
                                                          @RequestParam(value = "endDate") String endDate) {
        return Result.success(shopFinanceAnalysisService.getChainAndYear(startDate, endDate));
    }

    @ApiOperationSupport(order = 90, author = "alex")
    @ApiOperation(value = "获取当前利润信息", notes = "获取当前利润信息", response = Result.class)
    @GetMapping(value = "/getBenefitInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "startDate", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "endDate", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(value = "查找类型(year/month)", name = "searchType", required = true, dataTypeClass = String.class)}
    )
    public Result<ShopFinanceChainYearVo> getBenefitInfo(@RequestParam(value = "startDate") String startDate,
                                                         @RequestParam(value = "endDate") String endDate,
                                                         @RequestParam(value = "searchType") String searchType) {
        return Result.success(shopFinanceAnalysisService.getBenefitInfo(startDate, endDate, searchType));
    }
}
