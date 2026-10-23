package com.alex.user.dashboard.controller;

import com.alex.api.user.dashboard.MicroServiceHealthVo;
import com.alex.api.user.dashboard.OrgAdminDashboardVo;
import com.alex.api.user.dashboard.SuperAdminDashboardVo;
import com.alex.api.user.dashboard.SystemAuditLogVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.LogRestRequest;
import com.alex.user.dashboard.service.DashboardService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作台多角色全景看板相关接口
 */
@ApiSort(106)
@Api(value = "工作台多角色全景看板接口", tags = {"工作台多角色全景看板接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @LogRestRequest(apiName = "获取超级管理员大盘全景数据")
    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取超级管理员大盘全景数据", notes = "超级管理员专属：全平台用户、机构数、流水总额与集群QPS走势", response = Result.class)
    @GetMapping("/super/overview")
    public Result<SuperAdminDashboardVo> getSuperAdminOverview() {
        return Result.success(dashboardService.getSuperAdminOverview());
    }

    @LogRestRequest(apiName = "获取机构管理员大盘数据")
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取机构管理员大盘数据", notes = "机构/家庭管理员专属：管辖机构成员、角色与综合收支", response = Result.class)
    @GetMapping("/org/overview")
    public Result<OrgAdminDashboardVo> getOrgAdminOverview() {
        return Result.success(dashboardService.getOrgAdminOverview());
    }

    @LogRestRequest(apiName = "探测微服务集群节点在线状态")
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "探测微服务集群节点在线状态", notes = "实时探测各微服务节点网络时延与连通性", response = Result.class)
    @GetMapping("/services/health")
    public Result<List<MicroServiceHealthVo>> getServicesHealth() {
        return Result.success(dashboardService.getServicesHealth());
    }

    @LogRestRequest(apiName = "获取工作台系统审计动态时间线")
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "获取工作台系统审计动态时间线", notes = "获取系统安全、权限变更、多级缓存与定时巡检最近审计动态", response = Result.class)
    @GetMapping("/audit/timeline")
    public Result<List<SystemAuditLogVo>> getAuditTimeline() {
        return Result.success(dashboardService.getAuditTimeline());
    }
}
