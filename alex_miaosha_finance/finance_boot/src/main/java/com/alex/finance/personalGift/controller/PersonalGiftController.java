package com.alex.finance.personalGift.controller;

import com.alex.api.finance.personalGift.vo.ContactsGiftRecordVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftOccasionDistributionVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftStatisticVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftTrendVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.personalGift.service.PersonalGiftService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * description:  个人随礼信息表restApi
 * author:       alex
 * createDate:   2024-07-10 10:01:28
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "个人随礼信息表相关接口", tags = {"个人随礼信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/personal-gift")
public class PersonalGiftController {

    private final PersonalGiftService personalGiftService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取个人随礼信息表分页", notes = "获取个人随礼信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "personalGiftVo", dataTypeClass = PersonalGiftVo.class)}
    )
    public Result<Page<PersonalGiftVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                @RequestBody(required = false) PersonalGiftVo personalGiftVo) {
        return Result.success(personalGiftService.getPage(pageNum, pageSize, personalGiftVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取个人随礼信息表详情", notes = "获取个人随礼信息表详情", response = Result.class)
    @GetMapping
    public Result<PersonalGiftVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(personalGiftService.queryPersonalGift(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增个人随礼信息表", notes = "新增个人随礼信息表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PersonalGiftVo personalGiftVo) {
        return Result.success(personalGiftService.addPersonalGift(personalGiftVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改个人随礼信息表", notes = "修改个人随礼信息表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PersonalGiftVo personalGiftVo) {
        return Result.success(personalGiftService.updatePersonalGift(personalGiftVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除个人随礼信息表", notes = "刪除个人随礼信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(personalGiftService.deletePersonalGift(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "通知个人", notes = "通知个人", response = Result.class)
    @GetMapping(value = "/notice")
    public Result<Boolean> noticePersonalGift(@RequestParam(value = "id") Long id) {
        return Result.success(personalGiftService.noticePersonalGift(id));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "导入个人随礼信息", notes = "导入个人随礼信息", response = Result.class)
    @PostMapping(value = "/import")
    public Result<Boolean> importPersonalGift(@RequestPart("file") MultipartFile file) throws Exception {
        return Result.success(personalGiftService.importPersonalGift(file));
    }

    @ApiOperationSupport(order = 80, author = "alex")
    @ApiOperation(value = "获取联系人随礼记录列表", notes = "获取联系人随礼记录列表，包括每个联系人的随礼总额、收礼总额、净差额等统计信息。用于前端展示联系人记录页面", response = Result.class)
    @PostMapping(value = "/contacts-gift-record-list")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "contactsGiftRecordVo", dataTypeClass = String.class)}
    )
    public Result<Page<ContactsGiftRecordVo>> getContactsGiftRecordList(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                                        @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                                        @RequestBody(required = false) ContactsGiftRecordVo queryCondition) {
        return Result.success(personalGiftService.getContactsGiftRecordList(pageNum, pageSize, queryCondition));
    }

    @ApiOperationSupport(order = 90, author = "alex")
    @ApiOperation(value = "获取个人随礼统计概览", notes = "获取统计概览页面的数据，包括本月、年度的随礼收礼数据、环比、同比、联系人统计等。支持按时间范围过滤统计数据", response = Result.class)
    @GetMapping(value = "/statistic")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "开始日期（格式：yyyy-MM-dd），不提供则使用系统当前月份开始日期", name = "startTime", dataTypeClass = String.class),
            @ApiImplicitParam(value = "结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期", name = "endTime", dataTypeClass = String.class)
    })
    public Result<PersonalGiftStatisticVo> getPersonalGiftStatistic(@RequestParam(value = "startTime", required = false) String startTime,
                                               @RequestParam(value = "endTime", required = false) String endTime) {
        return Result.success(personalGiftService.getPersonalGiftStatistic(startTime, endTime));
    }

    @ApiOperationSupport(order = 100, author = "alex")
    @ApiOperation(value = "获取个人随礼近12个月趋势", notes = "获取近12个月的随礼收礼趋势数据，用于前端展示趋势图表", response = Result.class)
    @GetMapping(value = "/trend")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "开始日期（格式：yyyy-MM-dd），不提供则使用系统近12个月开始日期", name = "startTime", dataTypeClass = String.class),
            @ApiImplicitParam(value = "结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期", name = "endTime", dataTypeClass = String.class)
    })
    public Result<List<PersonalGiftTrendVo>> getPersonalGiftTrend(@RequestParam(value = "startTime", required = false) String startTime,
                                          @RequestParam(value = "endTime", required = false) String endTime) {
        return Result.success(personalGiftService.getPersonalGiftTrend(startTime, endTime));
    }

    @ApiOperationSupport(order = 110, author = "alex")
    @ApiOperation(value = "获取个人随礼场合分布", notes = "获取不同场合的随礼收礼分布数据，用于前端展示饼图", response = Result.class)
    @GetMapping(value = "/occasion-distribution")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "开始日期（格式：yyyy-MM-dd），不提供则使用系统当前月份开始日期", name = "startTime", dataTypeClass = String.class),
            @ApiImplicitParam(value = "结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期", name = "endTime", dataTypeClass = String.class)
    })
    public Result<List<PersonalGiftOccasionDistributionVo>> getPersonalGiftOccasionDistribution(@RequestParam(value = "startTime", required = false) String startTime,
                                                         @RequestParam(value = "endTime", required = false) String endTime) {
        return Result.success(personalGiftService.getPersonalGiftOccasionDistribution(startTime, endTime));
    }
}
