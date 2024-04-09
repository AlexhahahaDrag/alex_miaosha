package com.alex.product.controller.pmsShopWantProduct;

import com.alex.api.product.vo.pmsShopWantProduct.PmsShopWantProductVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.product.service.pmsShopWantProduct.PmsShopWantProductService;
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
 * description:  商品想买网上商品信息restApi
 * author:       alex
 * createDate:   2023-05-25 16:18:10
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商品想买网上商品信息相关接口", tags = {"商品想买网上商品信息相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pms-shop-want-product")
public class PmsShopWantProductController {

    private final PmsShopWantProductService pmsShopWantProductService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商品想买网上商品信息分页", notes = "获取商品想买网上商品信息分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "pmsShopWantProductVo")}
    )
    public Result<Page<PmsShopWantProductVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) PmsShopWantProductVo pmsShopWantProductVo) {
        return Result.success(pmsShopWantProductService.getPage(pageNum, pageSize, pmsShopWantProductVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品想买网上商品信息详情", notes = "获取商品想买网上商品信息详情", response = Result.class)
    @GetMapping
    public Result<PmsShopWantProductVo> query(@RequestParam(value = "id") String id) {
        return Result.success(pmsShopWantProductService.queryPmsShopWantProduct(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商品想买网上商品信息", notes = "新增商品想买网上商品信息", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PmsShopWantProductVo pmsShopWantProductVo) {
        return Result.success(pmsShopWantProductService.addPmsShopWantProduct(pmsShopWantProductVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商品想买网上商品信息", notes = "修改商品想买网上商品信息", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PmsShopWantProductVo pmsShopWantProductVo) {
        return Result.success(pmsShopWantProductService.updatePmsShopWantProduct(pmsShopWantProductVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商品想买网上商品信息", notes = "刪除商品想买网上商品信息", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(pmsShopWantProductService.deletePmsShopWantProduct(ids));
    }
}
