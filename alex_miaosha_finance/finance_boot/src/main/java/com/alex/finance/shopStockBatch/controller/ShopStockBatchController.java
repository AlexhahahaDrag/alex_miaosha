package com.alex.finance.shopStockBatch.controller;

import com.alex.api.finance.shopStockBatch.vo.ShopStockBatchVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.shopStockBatch.service.ShopStockBatchService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:  商店库存批次表restApi
 * author:       alex
 * createDate:   2024-05-10 17:30:40
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商店库存批次表相关接口", tags = {"商店库存批次表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-stock-batch")
public class ShopStockBatchController {

    private final ShopStockBatchService shopStockBatchService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商店库存批次表分页", notes = "获取商店库存批次表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopStockBatchVo", dataTypeClass = ShopStockBatchVo.class)}
    )
    public Result<Page<ShopStockBatchVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                  @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                  @RequestBody(required = false) ShopStockBatchVo shopStockBatchVo) {
        return Result.success(shopStockBatchService.getPage(pageNum, pageSize, shopStockBatchVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商店库存批次表详情", notes = "获取商店库存批次表详情", response = Result.class)
    @GetMapping
    public Result<ShopStockBatchVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(shopStockBatchService.queryShopStockBatch(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商店库存批次表", notes = "新增商店库存批次表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ShopStockBatchVo shopStockBatchVo) {
        return Result.success(shopStockBatchService.addShopStockBatch(shopStockBatchVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商店库存批次表", notes = "修改商店库存批次表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody ShopStockBatchVo shopStockBatchVo) {
        return Result.success(shopStockBatchService.updateShopStockBatch(shopStockBatchVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商店库存批次表", notes = "刪除商店库存批次表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(shopStockBatchService.deleteShopStockBatch(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "获取商店库存批次表列表", notes = "获取商店库存批次表列表", response = Result.class)
    @PostMapping(value = "/getList")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询条件", name = "shopStockBatchVo", dataTypeClass = ShopStockBatchVo.class)}
    )
    public Result<List<ShopStockBatchVo>> getList(@RequestBody(required = false) ShopStockBatchVo shopStockBatchVo) {
        return Result.success(shopStockBatchService.getList(shopStockBatchVo));
    }
}
