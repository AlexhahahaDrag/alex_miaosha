package com.alex.user.dashboard.service;

import com.alex.api.user.dashboard.MicroServiceHealthVo;
import com.alex.api.user.dashboard.OrgAdminDashboardVo;
import com.alex.api.user.dashboard.SuperAdminDashboardVo;
import com.alex.api.user.dashboard.SystemAuditLogVo;

import java.util.List;

/**
 * 工作台多角色全景看板业务接口
 */
public interface DashboardService {

    /**
     * 获取超级管理员全景 KPI 与态势看板
     */
    SuperAdminDashboardVo getSuperAdminOverview();

    /**
     * 获取机构/家庭管理员工作台大盘
     */
    OrgAdminDashboardVo getOrgAdminOverview();

    /**
     * 实时探测微服务集群各节点在线状态与时延
     */
    List<MicroServiceHealthVo> getServicesHealth();

    /**
     * 获取系统最近审计与核心操作动态
     */
    List<SystemAuditLogVo> getAuditTimeline();
}
