package com.alex.product.controller.pmsAttr;

import com.alex.api.product.vo.pmsAttr.PmsAttrVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import com.alex.product.entity.pmsAttr.PmsAttr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.product.service.pmsAttr.PmsAttrService;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:  商品属性restApi
 * @author:       alex
 * @createDate:   2023-12-27 16:02:07
 * @version:      1.0.0
 */
@ApiSort(105)
@Api(value = "商品属性相关接口", tags = {"商品属性相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pms-attr")
public class PmsAttrController {

    private final PmsAttrService pmsAttrService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取商品属性分页", notes = "获取商品属性分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "pmsAttrVo")}
    )
    public Result<Page<PmsAttrVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) PmsAttrVo pmsAttrVo) {
        return Result.success(pmsAttrService.getPage(pageNum, pageSize, pmsAttrVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品属性详情", notes = "获取商品属性详情", response = Result.class)
    @GetMapping
    public Result<PmsAttrVo> query(@RequestParam(value = "id") String id) {
        return Result.success(pmsAttrService.queryPmsAttr(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增商品属性", notes = "新增商品属性", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PmsAttrVo pmsAttrVo) {
        return Result.success(pmsAttrService.addPmsAttr(pmsAttrVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改商品属性", notes = "修改商品属性", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PmsAttrVo pmsAttrVo) {
        return Result.success(pmsAttrService.updatePmsAttr(pmsAttrVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除商品属性", notes = "刪除商品属性", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(pmsAttrService.deletePmsAttr(ids));
    }
}
