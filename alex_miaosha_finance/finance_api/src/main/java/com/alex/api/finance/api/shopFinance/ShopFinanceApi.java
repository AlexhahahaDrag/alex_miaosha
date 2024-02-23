package com.alex.api.finance.api.shopFinance;

import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.alex.api.finance.vo.shopFinance.ShopFinanceVo;

/**
 * description:  商店财务表controller
 * author:       majf
 * createDate:   2024-02-23 21:19:49
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface ShopFinanceApi {

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取商店财务表分页", notes = "获取商店财务表分页", response = Result.class)
    @PostMapping(value = "/api/v1//shop-finance/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "shopFinanceVo")}
    )
    Result<Page<ShopFinanceVo>> getShopFinancePage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) ShopFinanceVo shopFinanceVo);

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取商店财务表详情", notes = "获取商店财务表详情", response = Result.class)
    @GetMapping(value = "/api/v1//shop-finance")
    Result<ShopFinanceVo> queryShopFinance(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增商店财务表", notes = "新增商店财务表", response = Result.class)
    @PostMapping("/api/v1//shop-finance")
    Result<Boolean> addShopFinance(@RequestBody ShopFinanceVo shopFinanceVo);

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改商店财务表", notes = "修改商店财务表", response = Result.class)
    @PutMapping("/api/v1//shop-finance")
    Result<Boolean> updateShopFinance(@RequestBody ShopFinanceVo shopFinanceVo);

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除商店财务表", notes = "刪除商店财务表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteShopFinance(@RequestParam("ids") String ids);
}