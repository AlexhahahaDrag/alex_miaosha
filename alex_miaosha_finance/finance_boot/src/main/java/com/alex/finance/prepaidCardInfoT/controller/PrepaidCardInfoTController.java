package com.alex.finance.prepaidCardInfoT.controller;

import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
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
import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.prepaidCardInfoT.service.PrepaidCardInfoTService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  消费卡信息表restApi
 * author:       alex
 * createDate:   2025-04-28 20:58:16
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "消费卡信息表相关接口", tags = {"消费卡信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/prepaid-card-info-t")
public class PrepaidCardInfoTController {

    private final PrepaidCardInfoTService prepaidCardInfoTService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费卡信息表分页", notes = "获取消费卡信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "prepaidCardInfoTVo", dataTypeClass = PrepaidCardInfoTVo.class)}
    )
    public Result<Page<PrepaidCardInfoTVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                             @RequestBody(required = false) PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return Result.success(prepaidCardInfoTService.getPage(pageNum, pageSize, prepaidCardInfoTVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费卡信息表详情", notes = "获取消费卡信息表详情", response = Result.class)
    @GetMapping
    public Result<PrepaidCardInfoTVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(prepaidCardInfoTService.queryPrepaidCardInfoT(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费卡信息表", notes = "新增消费卡信息表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return Result.success(prepaidCardInfoTService.addPrepaidCardInfoT(prepaidCardInfoTVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费卡信息表", notes = "修改消费卡信息表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return Result.success(prepaidCardInfoTService.updatePrepaidCardInfoT(prepaidCardInfoTVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费卡信息表", notes = "刪除消费卡信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(prepaidCardInfoTService.deletePrepaidCardInfoT(ids));
    }
}
