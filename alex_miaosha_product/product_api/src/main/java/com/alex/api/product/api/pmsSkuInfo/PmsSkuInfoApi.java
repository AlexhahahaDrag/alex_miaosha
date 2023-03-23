package com.alex.api.product.api.pmsSkuInfo;

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
import com.alex.api.product.vo.pmsSkuInfo.PmsSkuInfoVo;

/**
 * @description:  sku信息controller
 * @author:       alex
 * @createDate:   2023-03-17 10:30:27
 * @version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PmsSkuInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取sku信息分页", notes = "获取sku信息分页", response = Result.class)
    @PostMapping(value = "/api/v1//pms-sku-info/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsSkuInfoVo")}
    )
    Result<Page<PmsSkuInfoVo>> getPmsSkuInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PmsSkuInfoVo pmsSkuInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取sku信息详情", notes = "获取sku信息详情", response = Result.class)
    @GetMapping(value = "/api/v1//pms-sku-info")
    Result<PmsSkuInfoVo> queryPmsSkuInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增sku信息", notes = "新增sku信息", response = Result.class)
    @PostMapping("/api/v1//pms-sku-info")
    Result<Boolean> addPmsSkuInfo(@RequestBody PmsSkuInfoVo pmsSkuInfoVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改sku信息", notes = "修改sku信息", response = Result.class)
    @PutMapping("/api/v1//pms-sku-info")
    Result<Boolean> updatePmsSkuInfo(@RequestBody PmsSkuInfoVo pmsSkuInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除sku信息", notes = "刪除sku信息", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePmsSkuInfo(@RequestParam("ids") String ids);
}