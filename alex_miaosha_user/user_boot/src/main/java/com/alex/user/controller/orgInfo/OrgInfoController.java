package com.alex.user.controller.orgInfo;

import com.alex.api.user.vo.orgInfo.OrgInfoVo;
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
import com.alex.user.entity.orgInfo.OrgInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.user.service.orgInfo.OrgInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:  机构表restApi
 * @author:       alex
 * @createDate:   2023-12-07 16:57:00
 * @version:      1.0.0
 */
@ApiSort(105)
@Api(value = "机构表相关接口", tags = {"机构表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/org-info")
public class OrgInfoController {

    private final OrgInfoService orgInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取机构表分页", notes = "获取机构表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "orgInfoVo")}
    )
    public Result<Page<OrgInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) OrgInfoVo orgInfoVo) {
        return Result.success(orgInfoService.getPage(pageNum, pageSize, orgInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取机构表详情", notes = "获取机构表详情", response = Result.class)
    @GetMapping
    public Result<OrgInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(orgInfoService.queryOrgInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增机构表", notes = "新增机构表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody OrgInfoVo orgInfoVo) {
        return Result.success(orgInfoService.addOrgInfo(orgInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改机构表", notes = "修改机构表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody OrgInfoVo orgInfoVo) {
        return Result.success(orgInfoService.updateOrgInfo(orgInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除机构表", notes = "刪除机构表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(orgInfoService.deleteOrgInfo(ids));
    }
}
