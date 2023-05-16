package com.alex.api.product.api.pmsShopProduct;

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
import com.alex.api.product.vo.pmsShopProduct.PmsShopProductVo;

/**
 * @description:  商品网上商品信息controller
 * @author:       alex
 * @createDate:   2023-05-15 14:11:10
 * @version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PmsShopProductApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商品网上商品信息分页", notes = "获取商品网上商品信息分页", response = Result.class)
    @PostMapping(value = "/api/v1//pms-shop-product/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsShopProductVo")}
    )
    Result<Page<PmsShopProductVo>> getPmsShopProductPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PmsShopProductVo pmsShopProductVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品网上商品信息详情", notes = "获取商品网上商品信息详情", response = Result.class)
    @GetMapping(value = "/api/v1//pms-shop-product")
    Result<PmsShopProductVo> queryPmsShopProduct(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商品网上商品信息", notes = "新增商品网上商品信息", response = Result.class)
    @PostMapping("/api/v1//pms-shop-product")
    Result<Boolean> addPmsShopProduct(@RequestBody PmsShopProductVo pmsShopProductVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商品网上商品信息", notes = "修改商品网上商品信息", response = Result.class)
    @PutMapping("/api/v1//pms-shop-product")
    Result<Boolean> updatePmsShopProduct(@RequestBody PmsShopProductVo pmsShopProductVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商品网上商品信息", notes = "刪除商品网上商品信息", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePmsShopProduct(@RequestParam("ids") String ids);
}