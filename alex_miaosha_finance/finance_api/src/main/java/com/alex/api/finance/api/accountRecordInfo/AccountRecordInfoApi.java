package com.alex.api.finance.api.accountRecordInfo;

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
import com.alex.api.finance.vo.accountRecordInfo.AccountRecordInfoVo;

/**
 * @description:  controller
 * @author:       alex
 * @createDate:   2023-04-08 16:27:39
 * @version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface AccountRecordInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取分页", notes = "获取分页", response = Result.class)
    @PostMapping(value = "/api/v1//account-record-info/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "accountRecordInfoVo")}
    )
    Result<Page<AccountRecordInfoVo>> getAccountRecordInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) AccountRecordInfoVo accountRecordInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取详情", notes = "获取详情", response = Result.class)
    @GetMapping(value = "/api/v1//account-record-info")
    Result<AccountRecordInfoVo> queryAccountRecordInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增", notes = "新增", response = Result.class)
    @PostMapping("/api/v1//account-record-info")
    Result<Boolean> addAccountRecordInfo(@RequestBody AccountRecordInfoVo accountRecordInfoVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改", notes = "修改", response = Result.class)
    @PutMapping("/api/v1//account-record-info")
    Result<Boolean> updateAccountRecordInfo(@RequestBody AccountRecordInfoVo accountRecordInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除", notes = "刪除", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteAccountRecordInfo(@RequestParam("ids") String ids);
}