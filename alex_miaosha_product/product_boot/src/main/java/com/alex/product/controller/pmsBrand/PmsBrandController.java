package com.alex.product.controller.pmsBrand;

import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.utils.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import com.alex.product.entity.pmsBrand.PmsBrand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.product.service.pmsBrand.PmsBrandService;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:  品牌restApi
 * @author:       alex
 * @createDate:   2023-03-02 15:31:52
 * @version:      1.0.0
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
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
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
}
