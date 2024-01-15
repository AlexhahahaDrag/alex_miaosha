package com.alex.product.controller.pmsSkuInfo;

import com.alex.api.product.vo.pmsSkuInfo.PmsSkuInfoVo;
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
import com.alex.product.service.pmsSkuInfo.PmsSkuInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  sku信息restApi
 * author:       alex
 * createDate:   2023-03-17 10:30:27
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "sku信息相关接口", tags = {"sku信息相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1//pms-sku-info")
public class PmsSkuInfoController {

    private final PmsSkuInfoService pmsSkuInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取sku信息分页", notes = "获取sku信息分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsSkuInfoVo")}
    )
    public Result<Page<PmsSkuInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) PmsSkuInfoVo pmsSkuInfoVo) {
        return Result.success(pmsSkuInfoService.getPage(pageNum, pageSize, pmsSkuInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取sku信息详情", notes = "获取sku信息详情", response = Result.class)
    @GetMapping
    public Result<PmsSkuInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(pmsSkuInfoService.queryPmsSkuInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增sku信息", notes = "新增sku信息", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PmsSkuInfoVo pmsSkuInfoVo) {
        return Result.success(pmsSkuInfoService.addPmsSkuInfo(pmsSkuInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改sku信息", notes = "修改sku信息", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PmsSkuInfoVo pmsSkuInfoVo) {
        return Result.success(pmsSkuInfoService.updatePmsSkuInfo(pmsSkuInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除sku信息", notes = "刪除sku信息", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(pmsSkuInfoService.deletePmsSkuInfo(ids));
    }
}
