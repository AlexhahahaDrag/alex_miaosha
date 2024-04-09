package com.alex.api.user.api.orgUserInfo;

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
import com.alex.api.user.vo.orgUserInfo.OrgUserInfoVo;

/**
 * description:  用户公司信息表controller
 * author:       majf
 * createDate:   2024-01-15 15:12:05
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface OrgUserInfoApi {

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取用户公司信息表分页", notes = "获取用户公司信息表分页", response = Result.class)
    @PostMapping(value = "/api/v1//org-user-info/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "orgUserInfoVo")}
    )
    Result<Page<OrgUserInfoVo>> getOrgUserInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) OrgUserInfoVo orgUserInfoVo);

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取用户公司信息表详情", notes = "获取用户公司信息表详情", response = Result.class)
    @GetMapping(value = "/api/v1//org-user-info")
    Result<OrgUserInfoVo> queryOrgUserInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增用户公司信息表", notes = "新增用户公司信息表", response = Result.class)
    @PostMapping("/api/v1//org-user-info")
    Result<Boolean> addOrgUserInfo(@RequestBody OrgUserInfoVo orgUserInfoVo);

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改用户公司信息表", notes = "修改用户公司信息表", response = Result.class)
    @PutMapping("/api/v1//org-user-info")
    Result<Boolean> updateOrgUserInfo(@RequestBody OrgUserInfoVo orgUserInfoVo);

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除用户公司信息表", notes = "刪除用户公司信息表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteOrgUserInfo(@RequestParam("ids") String ids);
}