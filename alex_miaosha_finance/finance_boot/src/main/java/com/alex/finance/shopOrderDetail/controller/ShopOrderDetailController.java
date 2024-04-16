package com.alex.finance.shopOrderDetail.controller;

import com.alex.api.finance.shopOrderDetail.vo.ShopOrderDetailVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.shopOrderDetail.service.ShopOrderDetailService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  商店订单明细表restApi
 * author:       alex
 * createDate:   2024-04-09 15:35:21
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商店订单明细表相关接口", tags = {"商店订单明细表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-order-detail")
public class ShopOrderDetailController {

    private final ShopOrderDetailService shopOrderDetailService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商店订单明细表分页", notes = "获取商店订单明细表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopOrderDetailVo", dataTypeClass = ShopOrderDetailVo.class)}
    )
    public Result<Page<ShopOrderDetailVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                             @RequestBody(required = false) ShopOrderDetailVo shopOrderDetailVo) {
        return Result.success(shopOrderDetailService.getPage(pageNum, pageSize, shopOrderDetailVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商店订单明细表详情", notes = "获取商店订单明细表详情", response = Result.class)
    @GetMapping
    public Result<ShopOrderDetailVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(shopOrderDetailService.queryShopOrderDetail(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商店订单明细表", notes = "新增商店订单明细表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ShopOrderDetailVo shopOrderDetailVo) {
        return Result.success(shopOrderDetailService.addShopOrderDetail(shopOrderDetailVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商店订单明细表", notes = "修改商店订单明细表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody ShopOrderDetailVo shopOrderDetailVo) {
        return Result.success(shopOrderDetailService.updateShopOrderDetail(shopOrderDetailVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商店订单明细表", notes = "刪除商店订单明细表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(shopOrderDetailService.deleteShopOrderDetail(ids));
    }
}
