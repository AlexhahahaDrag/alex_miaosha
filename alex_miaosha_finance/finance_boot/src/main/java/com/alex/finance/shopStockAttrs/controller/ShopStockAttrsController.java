package com.alex.finance.shopStockAttrs.controller;

import com.alex.api.finance.shopStockAttrs.vo.ShopStockAttrsVo;
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
import com.alex.finance.shopStockAttrs.entity.ShopStockAttrs;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.shopStockAttrs.service.ShopStockAttrsService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  商店库存属性表restApi
 * author:       alex
 * createDate:   2024-04-16 19:50:29
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商店库存属性表相关接口", tags = {"商店库存属性表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shop-stock-attrs")
public class ShopStockAttrsController {

    private final ShopStockAttrsService shopStockAttrsService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商店库存属性表分页", notes = "获取商店库存属性表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopStockAttrsVo", dataTypeClass = ShopStockAttrsVo.class)}
    )
    public Result<Page<ShopStockAttrsVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                             @RequestBody(required = false) ShopStockAttrsVo shopStockAttrsVo) {
        return Result.success(shopStockAttrsService.getPage(pageNum, pageSize, shopStockAttrsVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商店库存属性表详情", notes = "获取商店库存属性表详情", response = Result.class)
    @GetMapping
    public Result<ShopStockAttrsVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(shopStockAttrsService.queryShopStockAttrs(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商店库存属性表", notes = "新增商店库存属性表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ShopStockAttrsVo shopStockAttrsVo) {
        return Result.success(shopStockAttrsService.addShopStockAttrs(shopStockAttrsVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商店库存属性表", notes = "修改商店库存属性表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody ShopStockAttrsVo shopStockAttrsVo) {
        return Result.success(shopStockAttrsService.updateShopStockAttrs(shopStockAttrsVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商店库存属性表", notes = "刪除商店库存属性表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(shopStockAttrsService.deleteShopStockAttrs(ids));
    }
}
