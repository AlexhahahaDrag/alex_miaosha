package com.alex.finance.couponInfo.controller;

import com.alex.api.finance.couponInfo.vo.CouponInfoVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.LogRestRequest;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import com.alex.finance.couponInfo.entity.CouponInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.couponInfo.service.CouponInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消费券信息表 控制器
 * 
 * @author alex
 * @since 2025-12-17 11:43:14
 * @version 1.0.0
 */
@ApiSort(105)
@Api(value = "消费券信息表相关接口", tags = {"消费券信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupon-info")
public class CouponInfoController {

    /**
     * 消费券信息表 服务
     */
    private final CouponInfoService couponInfoService;

    /**
     * 分页查询消费券信息表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param couponInfoVo 查询条件
     * @return 分页结果
     */
    @LogRestRequest(apiName = "获取消费券信息表分页")
    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费券信息表分页", notes = "分页查询消费券信息表列表", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class, example = "10"),
            @ApiImplicitParam(value = "查询条件", name = "couponInfoVo", dataTypeClass = CouponInfoVo.class)}
    )
    public Result<Page<CouponInfoVo>> getPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
                                                 @RequestBody(required = false) CouponInfoVo couponInfoVo) {
        return Result.success(couponInfoService.getPage(pageNum, pageSize, couponInfoVo));
    }

    /**
     * 根据ID查询消费券信息表详情
     * 
     * @param id 主键ID
     * @return 消费券信息表详情
     */
    @LogRestRequest(apiName = "获取消费券信息表详情")
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费券信息表详情", notes = "根据ID查询消费券信息表详细信息", response = Result.class)
    @GetMapping(value = "/{id}")
    @ApiImplicitParam(value = "主键ID", name = "id", required = true, dataTypeClass = Long.class, paramType = "path")
    public Result<CouponInfoVo> getById(@PathVariable("id") Long id) {
        return Result.success(couponInfoService.queryCouponInfo(id));
    }

    /**
     * 新增消费券信息表
     * 
     * @param couponInfoVo 消费券信息表信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "新增消费券信息表")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费券信息表", notes = "创建新的消费券信息表记录", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody CouponInfoVo couponInfoVo) {
        return Result.success(couponInfoService.addCouponInfo(couponInfoVo));
    }

    /**
     * 修改消费券信息表
     * 
     * @param couponInfoVo 消费券信息表信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "修改消费券信息表")
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费券信息表", notes = "更新消费券信息表信息", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody CouponInfoVo couponInfoVo) {
        return Result.success(couponInfoService.updateCouponInfo(couponInfoVo));
    }

    /**
     * 删除消费券信息表
     * 
     * @param ids 主键ID列表，多个ID用逗号分隔
     * @return 操作结果
     */
    @LogRestRequest(apiName = "删除消费券信息表")
    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除消费券信息表", notes = "根据ID列表批量删除消费券信息表", response = Result.class)
    @DeleteMapping
    @ApiImplicitParam(value = "主键ID列表，多个ID用逗号分隔", name = "ids", required = true, dataTypeClass = String.class, example = "1,2,3")
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(couponInfoService.deleteCouponInfo(ids));
    }
}
