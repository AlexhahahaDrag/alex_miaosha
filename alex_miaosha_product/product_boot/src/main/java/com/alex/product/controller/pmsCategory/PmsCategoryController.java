package com.alex.product.controller.pmsCategory;

import com.alex.api.product.vo.pmsCategory.PmsCategoryVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.product.service.pmsCategory.PmsCategoryService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  商品三级分类restApi
 * author:       alex
 * createDate:   2023-03-17 10:06:58
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商品三级分类相关接口", tags = {"商品三级分类相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1//pms-category")
public class PmsCategoryController {

    private final PmsCategoryService pmsCategoryService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商品三级分类分页", notes = "获取商品三级分类分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsCategoryVo")}
    )
    public Result<Page<PmsCategoryVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) PmsCategoryVo pmsCategoryVo) {
        return Result.success(pmsCategoryService.getPage(pageNum, pageSize, pmsCategoryVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品三级分类详情", notes = "获取商品三级分类详情", response = Result.class)
    @GetMapping
    public Result<PmsCategoryVo> query(@RequestParam(value = "id") String id) {
        return Result.success(pmsCategoryService.queryPmsCategory(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商品三级分类", notes = "新增商品三级分类", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PmsCategoryVo pmsCategoryVo) {
        return Result.success(pmsCategoryService.addPmsCategory(pmsCategoryVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商品三级分类", notes = "修改商品三级分类", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PmsCategoryVo pmsCategoryVo) {
        return Result.success(pmsCategoryService.updatePmsCategory(pmsCategoryVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商品三级分类", notes = "刪除商品三级分类", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(pmsCategoryService.deletePmsCategory(ids));
    }
}
