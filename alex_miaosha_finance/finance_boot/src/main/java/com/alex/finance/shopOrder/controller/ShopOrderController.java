package com.alex.finance.shopOrder.controller;

import com.alex.api.finance.shopOrder.vo.ShopOrderVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.shopOrder.service.ShopOrderService;
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

/**
 * description:  商店订单表restApi
 * author:       alex
 * createDate:   2024-04-09 15:20:01
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商店订单表相关接口", tags = {"商店订单表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-order")
public class ShopOrderController {

    private final ShopOrderService shopOrderService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商店订单表分页", notes = "获取商店订单表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopOrderVo", dataTypeClass = ShopOrderVo.class)}
    )
    public Result<Page<ShopOrderVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                             @RequestBody(required = false) ShopOrderVo shopOrderVo) {
        return Result.success(shopOrderService.getPage(pageNum, pageSize, shopOrderVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商店订单表详情", notes = "获取商店订单表详情", response = Result.class)
    @GetMapping
    public Result<ShopOrderVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(shopOrderService.queryShopOrder(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商店订单表", notes = "新增商店订单表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ShopOrderVo shopOrderVo) {
        return Result.success(shopOrderService.addShopOrder(shopOrderVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商店订单表", notes = "修改商店订单表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody ShopOrderVo shopOrderVo) {
        return Result.success(shopOrderService.updateShopOrder(shopOrderVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商店订单表", notes = "刪除商店订单表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(shopOrderService.deleteShopOrder(ids));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "提交订单", notes = "提交订单", response = Result.class)
    @PostMapping(path = "/submitOrder")
    public Result<Boolean> submitOrder(@RequestBody ShopOrderVo shopOrderVo) throws Exception {
        return Result.success(shopOrderService.submitOrder(shopOrderVo));
    }
}
