package com.alex.product.controller.shopProduct;


import com.alex.api.product.vo.product.jd.Content;
import com.alex.base.common.Result;
import com.alex.product.service.shopProduct.ShopProductService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@ApiSort(80)
@Api(value = "网上商品相关接口", tags = {"网上商品相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopProduct")
public class ShopProductController {

    private final ShopProductService shopProductService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商城商品", notes = "获取商城商品", response = Result.class)
    @GetMapping("/shopProduct")
    public Result<List<Content>> getShopProduct() throws Exception {
        return Result.success(shopProductService.getShopProduct());
    }
}
