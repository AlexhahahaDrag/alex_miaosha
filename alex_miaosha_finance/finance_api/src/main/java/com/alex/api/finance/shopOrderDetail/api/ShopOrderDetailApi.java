package com.alex.api.finance.shopOrderDetail.api;

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
import com.alex.api.finance.shopOrderDetail.vo.ShopOrderDetailVo;

/**
 * description:  商店订单明细表controller
 * author:       alex
 * createDate:   2024-04-09 15:35:21
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface ShopOrderDetailApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商店订单明细表分页", notes = "获取商店订单明细表分页", response = Result.class)
    @PostMapping(value = "/api/v1//shop-order-detail/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "shopOrderDetailVo")}
    )
    Result<Page<ShopOrderDetailVo>> getShopOrderDetailPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) ShopOrderDetailVo shopOrderDetailVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商店订单明细表详情", notes = "获取商店订单明细表详情", response = Result.class)
    @GetMapping(value = "/api/v1//shop-order-detail")
    Result<ShopOrderDetailVo> queryShopOrderDetail(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商店订单明细表", notes = "新增商店订单明细表", response = Result.class)
    @PostMapping("/api/v1//shop-order-detail")
    Result<Boolean> addShopOrderDetail(@RequestBody ShopOrderDetailVo shopOrderDetailVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商店订单明细表", notes = "修改商店订单明细表", response = Result.class)
    @PutMapping("/api/v1//shop-order-detail")
    Result<Boolean> updateShopOrderDetail(@RequestBody ShopOrderDetailVo shopOrderDetailVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商店订单明细表", notes = "刪除商店订单明细表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteShopOrderDetail(@RequestParam("ids") String ids);
}