package com.alex.finance.personalGift.controller;

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
@RequestMapping("/api/v1/personal-gift")
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
}
