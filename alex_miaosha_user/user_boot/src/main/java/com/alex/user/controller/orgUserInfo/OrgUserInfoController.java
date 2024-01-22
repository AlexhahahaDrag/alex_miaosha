package com.alex.user.controller.orgUserInfo;

import com.alex.api.user.vo.orgUserInfo.OrgUserInfoVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.user.service.orgUserInfo.OrgUserInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  用户公司信息表restApi
 * author:       majf
 * createDate:   2024-01-15 15:12:05
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "用户公司信息表相关接口", tags = {"用户公司信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/org-user-info")
public class OrgUserInfoController {

    private final OrgUserInfoService orgUserInfoService;

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取用户公司信息表分页", notes = "获取用户公司信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "orgUserInfoVo")}
    )
    public Result<Page<OrgUserInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) OrgUserInfoVo orgUserInfoVo) {
        return Result.success(orgUserInfoService.getPage(pageNum, pageSize, orgUserInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取用户公司信息表详情", notes = "获取用户公司信息表详情", response = Result.class)
    @GetMapping
    public Result<OrgUserInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(orgUserInfoService.queryOrgUserInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增用户公司信息表", notes = "新增用户公司信息表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody OrgUserInfoVo orgUserInfoVo) {
        return Result.success(orgUserInfoService.addOrgUserInfo(orgUserInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改用户公司信息表", notes = "修改用户公司信息表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody OrgUserInfoVo orgUserInfoVo) {
        return Result.success(orgUserInfoService.updateOrgUserInfo(orgUserInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除用户公司信息表", notes = "刪除用户公司信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(orgUserInfoService.deleteOrgUserInfo(ids));
    }
}
