package com.alex.api.finance.shopCart.api;

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
import com.alex.api.finance.shopCart.vo.ShopCartVo;

/**
 * description:  购物车表controller
 * author:       alex
 * createDate:   2024-04-03 11:36:19
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface ShopCartApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取购物车表分页", notes = "获取购物车表分页", response = Result.class)
    @PostMapping(value = "/api/v1//shop-cart/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopCartVo")}
    )
    Result<Page<ShopCartVo>> getShopCartPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) ShopCartVo shopCartVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取购物车表详情", notes = "获取购物车表详情", response = Result.class)
    @GetMapping(value = "/api/v1//shop-cart")
    Result<ShopCartVo> queryShopCart(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增购物车表", notes = "新增购物车表", response = Result.class)
    @PostMapping("/api/v1//shop-cart")
    Result<Boolean> addShopCart(@RequestBody ShopCartVo shopCartVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改购物车表", notes = "修改购物车表", response = Result.class)
    @PutMapping("/api/v1//shop-cart")
    Result<Boolean> updateShopCart(@RequestBody ShopCartVo shopCartVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除购物车表", notes = "刪除购物车表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteShopCart(@RequestParam("ids") String ids);
}