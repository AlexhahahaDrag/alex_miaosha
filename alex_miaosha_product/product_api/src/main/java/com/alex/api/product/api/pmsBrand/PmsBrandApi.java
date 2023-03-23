package com.alex.api.product.api.pmsBrand;

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
import com.alex.api.product.vo.pmsBrand.PmsBrandVo;

/**
 * @description:  品牌controller
 * @author:       alex
 * @createDate:   2023-03-02 19:16:11
 * @version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PmsBrandApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取品牌分页", notes = "获取品牌分页", response = Result.class)
    @PostMapping(value = "/api/v1//pms-brand/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsBrandVo")}
    )
    Result<Page<PmsBrandVo>> getPmsBrandPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PmsBrandVo pmsBrandVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取品牌详情", notes = "获取品牌详情", response = Result.class)
    @GetMapping(value = "/api/v1//pms-brand")
    Result<PmsBrandVo> queryPmsBrand(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增品牌", notes = "新增品牌", response = Result.class)
    @PostMapping("/api/v1//pms-brand")
    Result<Boolean> addPmsBrand(@RequestBody PmsBrandVo pmsBrandVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改品牌", notes = "修改品牌", response = Result.class)
    @PutMapping("/api/v1//pms-brand")
    Result<Boolean> updatePmsBrand(@RequestBody PmsBrandVo pmsBrandVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除品牌", notes = "刪除品牌", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePmsBrand(@RequestParam("ids") String ids);
}