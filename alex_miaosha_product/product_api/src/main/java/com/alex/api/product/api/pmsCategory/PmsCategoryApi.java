package com.alex.api.product.api.pmsCategory;

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
import com.alex.api.product.vo.pmsCategory.PmsCategoryVo;

/**
 * @description:  商品三级分类controller
 * @author:       alex
 * @createDate:   2023-03-17 10:06:58
 * @version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PmsCategoryApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商品三级分类分页", notes = "获取商品三级分类分页", response = Result.class)
    @PostMapping(value = "/api/v1//pms-category/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsCategoryVo")}
    )
    Result<Page<PmsCategoryVo>> getPmsCategoryPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PmsCategoryVo pmsCategoryVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品三级分类详情", notes = "获取商品三级分类详情", response = Result.class)
    @GetMapping(value = "/api/v1//pms-category")
    Result<PmsCategoryVo> queryPmsCategory(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商品三级分类", notes = "新增商品三级分类", response = Result.class)
    @PostMapping("/api/v1//pms-category")
    Result<Boolean> addPmsCategory(@RequestBody PmsCategoryVo pmsCategoryVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商品三级分类", notes = "修改商品三级分类", response = Result.class)
    @PutMapping("/api/v1//pms-category")
    Result<Boolean> updatePmsCategory(@RequestBody PmsCategoryVo pmsCategoryVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商品三级分类", notes = "刪除商品三级分类", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePmsCategory(@RequestParam("ids") String ids);
}