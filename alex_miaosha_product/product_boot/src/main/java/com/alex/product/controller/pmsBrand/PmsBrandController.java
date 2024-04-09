package com.alex.product.controller.pmsBrand;

import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import com.alex.base.common.Result;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.product.service.pmsBrand.PmsBrandService;
import com.alex.common.annotations.AvoidRepeatableCommit;
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
 * description:  品牌restApi
 * author:       alex
 * createDate:   2023-03-05 21:39:54
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "品牌相关接口", tags = {"品牌相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1//pms-brand")
public class PmsBrandController {

    private final PmsBrandService pmsBrandService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取品牌分页", notes = "获取品牌分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "pmsBrandVo")}
    )
    public Result<Page<PmsBrandVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) PmsBrandVo pmsBrandVo) {
        return Result.success(pmsBrandService.getPage(pageNum, pageSize, pmsBrandVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取品牌详情", notes = "获取品牌详情", response = Result.class)
    @GetMapping
    public Result<PmsBrandVo> query(@RequestParam(value = "id") String id) {
        return Result.success(pmsBrandService.queryPmsBrand(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增品牌", notes = "新增品牌", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PmsBrandVo pmsBrandVo) {
        return Result.success(pmsBrandService.addPmsBrand(pmsBrandVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改品牌", notes = "修改品牌", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PmsBrandVo pmsBrandVo) {
        return Result.success(pmsBrandService.updatePmsBrand(pmsBrandVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除品牌", notes = "刪除品牌", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(pmsBrandService.deletePmsBrand(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "test", notes = "刪除品牌", response = Result.class)
    @GetMapping(value = "test")
    public Result<String> test() {
        return Result.success(pmsBrandService.test());
    }
}
