package com.alex.api.finance.prepaidCardInfoT.api;

import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.base.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * description:  消费卡信息表controller
 * author:       alex
 * createDate:   2025-04-30 08:21:48
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PrepaidCardInfoTApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费卡信息表分页", notes = "获取消费卡信息表分页", response = Result.class)
    @PostMapping(value = "/api/v1//prepaid-card-info-t/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "prepaidCardInfoTVo")}
    )
    Result<Page<PrepaidCardInfoTVo>> getPrepaidCardInfoTPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                             @RequestBody(required = false) PrepaidCardInfoTVo prepaidCardInfoTVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费卡信息表详情", notes = "获取消费卡信息表详情", response = Result.class)
    @GetMapping(value = "/api/v1//prepaid-card-info-t")
    Result<PrepaidCardInfoTVo> queryPrepaidCardInfoT(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费卡信息表", notes = "新增消费卡信息表", response = Result.class)
    @PostMapping("/api/v1//prepaid-card-info-t")
    Result<Boolean> addPrepaidCardInfoT(@RequestBody PrepaidCardInfoTVo prepaidCardInfoTVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费卡信息表", notes = "修改消费卡信息表", response = Result.class)
    @PutMapping("/api/v1//prepaid-card-info-t")
    Result<Boolean> updatePrepaidCardInfoT(@RequestBody PrepaidCardInfoTVo prepaidCardInfoTVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费卡信息表", notes = "刪除消费卡信息表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePrepaidCardInfoT(@RequestParam("ids") String ids);
}