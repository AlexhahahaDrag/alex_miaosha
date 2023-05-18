package com.alex.product.controller.pmsShopProduct;

import com.alex.api.product.vo.pmsShopProduct.PmsShopProductVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.product.service.pmsShopProduct.PmsShopProductService;
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
 * @description: 商品网上商品信息restApi
 * @author: alex
 * @createDate: 2023-05-15 14:11:10
 * @version: 1.0.0
 */
@ApiSort(105)
@Api(value = "商品网上商品信息相关接口", tags = {"商品网上商品信息相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pms-shop-product")
public class PmsShopProductController {

    private final PmsShopProductService pmsShopProductService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商品网上商品信息分页", notes = "获取商品网上商品信息分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsShopProductVo")}
    )
    public Result<Page<PmsShopProductVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                  @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                  @RequestBody(required = false) PmsShopProductVo pmsShopProductVo) {
        return Result.success(pmsShopProductService.getPage(pageNum, pageSize, pmsShopProductVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品网上商品信息详情", notes = "获取商品网上商品信息详情", response = Result.class)
    @GetMapping
    public Result<PmsShopProductVo> query(@RequestParam(value = "id") String id) {
        return Result.success(pmsShopProductService.queryPmsShopProduct(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商品网上商品信息", notes = "新增商品网上商品信息", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PmsShopProductVo pmsShopProductVo) {
        return Result.success(pmsShopProductService.addPmsShopProduct(pmsShopProductVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商品网上商品信息", notes = "修改商品网上商品信息", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PmsShopProductVo pmsShopProductVo) {
        return Result.success(pmsShopProductService.updatePmsShopProduct(pmsShopProductVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商品网上商品信息", notes = "刪除商品网上商品信息", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(pmsShopProductService.deletePmsShopProduct(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "获取商品网上最新商品信息分页", notes = "获取商品网上最新商品信息分页", response = Result.class)
    @PostMapping(value = "/newestPage")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsShopProductVo")}
    )
    public Result<Page<PmsShopProductVo>> getNewestProductPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                               @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                               @RequestBody(required = false) PmsShopProductVo pmsShopProductVo) {
        return Result.success(pmsShopProductService.getNewestProductPage(pageNum, pageSize, pmsShopProductVo));
    }
}
