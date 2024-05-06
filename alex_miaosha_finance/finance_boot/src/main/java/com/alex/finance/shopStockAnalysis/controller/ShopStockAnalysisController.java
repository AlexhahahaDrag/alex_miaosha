package com.alex.finance.shopStockAnalysis.controller;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopStockAnalysisVo;
import com.alex.base.common.Result;
import com.alex.finance.shopStockAnalysis.service.ShopStockAnalysisService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * author: majf
 * createDate: 2024/3/2 22:17
 * description: 商店财务分析业务控制
 * version: 1.0.0
 */
@ApiSort(150)
@Api(value = "商店库存分析相关接口", tags = {"商店库存分析相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-stock-analysis")
public class ShopStockAnalysisController {

    private final ShopStockAnalysisService shopStockAnalysisService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取店铺库存信息", notes = "获取店铺库存信息", response = Result.class)
    @GetMapping(value = "/getAllStockInfo")
    public Result<ShopStockAnalysisVo> getAllShopStockInfo() {
        return Result.success(shopStockAnalysisService.getAllShopStockInfo());
    }
}
