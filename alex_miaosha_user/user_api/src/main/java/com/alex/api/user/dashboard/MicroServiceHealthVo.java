package com.alex.api.user.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 微服务集群节点健康与探针指标 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "微服务集群节点健康与探针指标")
public class MicroServiceHealthVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "服务唯一标识", example = "alex-gateway")
    private String serviceId;

    @ApiModelProperty(value = "微服务显示名称", example = "Spring Cloud 统一网关")
    private String name;

    @ApiModelProperty(value = "微服务监听端口", example = "30001")
    private Integer port;

    @ApiModelProperty(value = "核心职责描述", example = "路由转发、统一鉴权、分布式限流")
    private String desc;

    @ApiModelProperty(value = "在线状态 (UP / DOWN / DEGRADED)", example = "UP")
    private String status;

    @ApiModelProperty(value = "网络探测时延 (毫秒)", example = "15")
    private Long latency;

    @ApiModelProperty(value = "最近探测时间", example = "2026-10-23 16:30:00")
    private String lastCheckTime;
}
