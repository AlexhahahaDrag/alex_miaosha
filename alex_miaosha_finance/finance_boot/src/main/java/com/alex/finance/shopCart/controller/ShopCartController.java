package com.alex.finance.shopCart.controller;

import com.alex.api.finance.shopCart.vo.ShopCartVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.shopCart.service.ShopCartService;
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
 * description:  购物车表restApi
 * author:       alex
 * createDate:   2024-04-03 11:36:19
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "购物车表相关接口", tags = {"购物车表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-cart")
public class ShopCartController {

    private final ShopCartService shopCartService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取购物车表分页", notes = "获取购物车表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopCartVo", dataTypeClass = ShopCartVo.class)}
    )
    public Result<Page<ShopCartVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) ShopCartVo shopCartVo) {
        return Result.success(shopCartService.getPage(pageNum, pageSize, shopCartVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取购物车表详情", notes = "获取购物车表详情", response = Result.class)
    @GetMapping
    public Result<ShopCartVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(shopCartService.queryShopCart(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增购物车表", notes = "新增购物车表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ShopCartVo shopCartVo) {
        return Result.success(shopCartService.addShopCart(shopCartVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改购物车表", notes = "修改购物车表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody ShopCartVo shopCartVo) {
        return Result.success(shopCartService.updateShopCart(shopCartVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除购物车表", notes = "刪除购物车表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(shopCartService.deleteShopCart(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "获取购物车表列表数据", notes = "获取购物车表列表数据", response = Result.class)
    @PostMapping(value = "/list")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询条件", name = "shopCartVo", dataTypeClass = ShopCartVo.class)}
    )
    public Result<List<ShopCartVo>> list(@RequestParam(value = "ids", required = false) String ids) {
        return Result.success(shopCartService.list(ids));
    }
}
