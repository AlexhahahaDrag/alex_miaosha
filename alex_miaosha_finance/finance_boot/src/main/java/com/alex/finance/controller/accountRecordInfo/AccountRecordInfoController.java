package com.alex.finance.controller.accountRecordInfo;

import com.alex.api.finance.vo.accountRecordInfo.AccountCountInfoVo;
import com.alex.api.finance.vo.accountRecordInfo.AccountRecordInfoVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.service.accountRecordInfo.AccountRecordInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:  restApi
 * author:       alex
 * createDate:   2023-04-08 16:27:39
 * version:      1.0.0
 */
@ApiSort(50)
@Api(value = "相关接口", tags = {"相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1//account-record-info")
public class AccountRecordInfoController {

    private final AccountRecordInfoService accountRecordInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取分页", notes = "获取分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "accountRecordInfoVo", dataTypeClass = AccountRecordInfoVo.class)}
    )
    public Result<Page<AccountRecordInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) AccountRecordInfoVo accountRecordInfoVo) {
        return Result.success(accountRecordInfoService.getPage(pageNum, pageSize, accountRecordInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取详情", notes = "获取详情", response = Result.class)
    @GetMapping
    public Result<AccountRecordInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(accountRecordInfoService.queryAccountRecordInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增", notes = "新增", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody AccountRecordInfoVo accountRecordInfoVo) {
        return Result.success(accountRecordInfoService.addAccountRecordInfo(accountRecordInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改", notes = "修改", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody AccountRecordInfoVo accountRecordInfoVo) {
        return Result.success(accountRecordInfoService.updateAccountRecordInfo(accountRecordInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除", notes = "刪除", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(accountRecordInfoService.deleteAccountRecordInfo(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "获取消息提醒信息", notes = "获取消息提醒信息", response = Result.class)
    @GetMapping(value = "queryRemindRecordInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "间距日期", name = "difDays", dataTypeClass = Integer.class)}
    )
    public Result<List<AccountCountInfoVo>> queryRemindRecordInfo(@RequestParam(value = "difDays", required = false) Integer difDays) throws WxErrorException {
        return Result.success(accountRecordInfoService.queryRemindRecordInfo(difDays));
    }
}
