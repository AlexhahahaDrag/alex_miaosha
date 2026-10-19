package com.alex.finance.message.controller;

import com.alex.api.finance.message.vo.SysMessageInfoVo;
import com.alex.api.finance.message.vo.SysMessageSummaryVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.finance.message.service.SysMessageInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统消息与业务到期提醒控制器
 */
@ApiSort(140)
@Api(value = "系统消息与到期提醒管理", tags = {"系统消息与到期提醒管理"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/sys-message-info")
public class SysMessageInfoController {

    private final SysMessageInfoService sysMessageInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "消息与提醒分页查询", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<SysMessageInfoVo>> getPage(
            @RequestParam(value = "pageNum", required = false) Long pageNum,
            @RequestParam(value = "pageSize", required = false) Long pageSize,
            @RequestBody(required = false) SysMessageInfoVo query
    ) {
        return Result.success(sysMessageInfoService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取未读消息分类汇总统计", response = Result.class)
    @GetMapping(value = "/summary")
    public Result<SysMessageSummaryVo> getSummary() {
        return Result.success(sysMessageInfoService.getSummary());
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "标记单条消息已读", response = Result.class)
    @PutMapping(value = "/read")
    public Result<Boolean> markRead(@RequestParam(value = "id") Long id) {
        return Result.success(sysMessageInfoService.markRead(id));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "一键全部标记已读（可指定消息类型）", response = Result.class)
    @PutMapping(value = "/read-all")
    public Result<Integer> markAllRead(@RequestParam(value = "msgType", required = false) String msgType) {
        return Result.success(sysMessageInfoService.markAllRead(msgType));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "发布系统通知或自定义提醒", response = Result.class)
    @PostMapping
    public Result<SysMessageInfoVo> publish(@RequestBody SysMessageInfoVo vo) {
        return Result.success(sysMessageInfoService.publishSystemMessage(vo));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "删除消息（支持逗号分隔批量删除）", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam(value = "ids") String ids) {
        return Result.success(sysMessageInfoService.deleteMessages(ids));
    }
}
