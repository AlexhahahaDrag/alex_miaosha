package com.alex.api.user.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 工作台系统审计与核心动态时间线 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "工作台系统审计与核心动态时间线")
public class SystemAuditLogVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "事件唯一 ID", example = "evt-101")
    private String id;

    @ApiModelProperty(value = "事件标题", example = "用户权限分配")
    private String title;

    @ApiModelProperty(value = "详细描述", example = "为用户 majf 分配机构管理员角色")
    private String description;

    @ApiModelProperty(value = "操作人", example = "super_super")
    private String operator;

    @ApiModelProperty(value = "操作时间", example = "2026-10-23 15:30:00")
    private String operateTime;

    @ApiModelProperty(value = "业务标签分类", example = "安全鉴权")
    private String tag;

    @ApiModelProperty(value = "标签颜色", example = "purple")
    private String tagColor;
}
