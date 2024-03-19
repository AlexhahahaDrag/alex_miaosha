package com.alex.finance.controller.shopStock;

import com.alex.api.finance.vo.shopStock.ShopStockVo;
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
import com.alex.finance.entity.shopStock.ShopStock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.service.shopStock.ShopStockService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  商店库存表restApi
 * author:       alex
 * createDate:   2024-03-12 16:49:20
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商店库存表相关接口", tags = {"商店库存表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-stock")
public class ShopStockController {

    private final ShopStockService shopStockService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商店库存表分页", notes = "获取商店库存表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "shopStockVo")}
    )
    public Result<Page<ShopStockVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                @RequestBody(required = false) ShopStockVo shopStockVo) {
        return Result.success(shopStockService.getPage(pageNum, pageSize, shopStockVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商店库存表详情", notes = "获取商店库存表详情", response = Result.class)
    @GetMapping
    public Result<ShopStockVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(shopStockService.queryShopStock(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商店库存表", notes = "新增商店库存表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ShopStockVo shopStockVo) {
        return Result.success(shopStockService.addShopStock(shopStockVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商店库存表", notes = "修改商店库存表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody ShopStockVo shopStockVo) {
        return Result.success(shopStockService.updateShopStock(shopStockVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商店库存表", notes = "刪除商店库存表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(shopStockService.deleteShopStock(ids));
    }
}