package com.alex.api.product.api.pmsAttr;

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
import com.alex.api.product.vo.pmsAttr.PmsAttrVo;

/**
 * @description:  商品属性controller
 * @author:       alex
 * @createDate:   2023-12-21 22:50:20
 * @version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PmsAttrApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商品属性分页", notes = "获取商品属性分页", response = Result.class)
    @PostMapping(value = "/api/v1//pms-attr/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsAttrVo")}
    )
    Result<Page<PmsAttrVo>> getPmsAttrPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PmsAttrVo pmsAttrVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品属性详情", notes = "获取商品属性详情", response = Result.class)
    @GetMapping(value = "/api/v1//pms-attr")
    Result<PmsAttrVo> queryPmsAttr(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商品属性", notes = "新增商品属性", response = Result.class)
    @PostMapping("/api/v1//pms-attr")
    Result<Boolean> addPmsAttr(@RequestBody PmsAttrVo pmsAttrVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商品属性", notes = "修改商品属性", response = Result.class)
    @PutMapping("/api/v1//pms-attr")
    Result<Boolean> updatePmsAttr(@RequestBody PmsAttrVo pmsAttrVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商品属性", notes = "刪除商品属性", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePmsAttr(@RequestParam("ids") String ids);
}