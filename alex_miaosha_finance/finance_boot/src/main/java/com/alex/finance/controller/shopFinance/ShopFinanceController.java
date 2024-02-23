package com.alex.finance.controller.shopFinance;

import com.alex.api.finance.vo.shopFinance.ShopFinanceVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import com.alex.finance.entity.shopFinance.ShopFinance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.service.shopFinance.ShopFinanceService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  商店财务表restApi
 * author:       majf
 * createDate:   2024-02-23 21:19:49
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商店财务表相关接口", tags = {"商店财务表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-finance")
public class ShopFinanceController {

    private final ShopFinanceService shopFinanceService;

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取商店财务表分页", notes = "获取商店财务表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "shopFinanceVo")}
    )
    public Result<Page<ShopFinanceVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) ShopFinanceVo shopFinanceVo) {
        return Result.success(shopFinanceService.getPage(pageNum, pageSize, shopFinanceVo));
    }

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取商店财务表详情", notes = "获取商店财务表详情", response = Result.class)
    @GetMapping
    public Result<ShopFinanceVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(shopFinanceService.queryShopFinance(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增商店财务表", notes = "新增商店财务表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ShopFinanceVo shopFinanceVo) {
        return Result.success(shopFinanceService.addShopFinance(shopFinanceVo));
    }

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改商店财务表", notes = "修改商店财务表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody ShopFinanceVo shopFinanceVo) {
        return Result.success(shopFinanceService.updateShopFinance(shopFinanceVo));
    }

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除商店财务表", notes = "刪除商店财务表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(shopFinanceService.deleteShopFinance(ids));
    }
}
