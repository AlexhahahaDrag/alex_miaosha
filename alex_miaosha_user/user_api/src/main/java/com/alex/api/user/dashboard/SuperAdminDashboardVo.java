package com.alex.api.user.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 超级管理员工作台全景 KPI 与态势看板 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "超级管理员工作台全景看板数据")
public class SuperAdminDashboardVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "全平台注册用户总量", example = "12480")
    private Long totalUsers;

    @ApiModelProperty(value = "活跃组织机构/家庭总数", example = "86")
    private Long activeOrgs;

    @ApiModelProperty(value = "全平台累计流转总额 (元)", example = "842600.00")
    private BigDecimal totalTurnover;

    @ApiModelProperty(value = "微服务集群实时请求吞吐峰值 (QPS)", example = "3420")
    private Integer clusterQps;

    @ApiModelProperty(value = "微服务集群各节点健康状态")
    private List<MicroServiceHealthVo> serviceNodes;

    @ApiModelProperty(value = "近7天趋势横坐标日期列表", example = "[\"10-17\", \"10-18\", \"10-19\", \"10-20\", \"10-21\", \"10-22\", \"10-23\"]")
    private List<String> trendDates;

    @ApiModelProperty(value = "近7天 QPS 吞吐走势", example = "[2100, 2400, 2800, 3100, 2900, 3300, 3420]")
    private List<Integer> trendQps;

    @ApiModelProperty(value = "近7天业务记账与交易单量走势", example = "[120, 150, 180, 210, 190, 260, 280]")
    private List<Integer> trendOrders;
}
