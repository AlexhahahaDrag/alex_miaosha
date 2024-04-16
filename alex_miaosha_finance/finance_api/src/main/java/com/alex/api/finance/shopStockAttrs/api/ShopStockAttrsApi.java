package com.alex.api.finance.shopStockAttrs.api;

import com.alex.base.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.alex.api.finance.shopStockAttrs.vo.ShopStockAttrsVo;

/**
 * description:  商店库存属性表controller
 * author:       alex
 * createDate:   2024-04-16 19:50:29
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface ShopStockAttrsApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商店库存属性表分页", notes = "获取商店库存属性表分页", response = Result.class)
    @PostMapping(value = "/api/v1//shop-stock-attrs/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopStockAttrsVo")}
    )
    Result<Page<ShopStockAttrsVo>> getShopStockAttrsPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) ShopStockAttrsVo shopStockAttrsVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商店库存属性表详情", notes = "获取商店库存属性表详情", response = Result.class)
    @GetMapping(value = "/api/v1//shop-stock-attrs")
    Result<ShopStockAttrsVo> queryShopStockAttrs(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商店库存属性表", notes = "新增商店库存属性表", response = Result.class)
    @PostMapping("/api/v1//shop-stock-attrs")
    Result<Boolean> addShopStockAttrs(@RequestBody ShopStockAttrsVo shopStockAttrsVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商店库存属性表", notes = "修改商店库存属性表", response = Result.class)
    @PutMapping("/api/v1//shop-stock-attrs")
    Result<Boolean> updateShopStockAttrs(@RequestBody ShopStockAttrsVo shopStockAttrsVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商店库存属性表", notes = "刪除商店库存属性表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteShopStockAttrs(@RequestParam("ids") String ids);
}