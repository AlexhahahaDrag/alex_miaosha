package com.alex.api.finance.vo.finance;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * description: AI 智能预算测算推荐请求参数
 * author: alex
 * createDate: 2026-10-12
 * version: 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "FinanceBudgetAiRecommendReq", description = "AI 智能预算测算推荐请求参数")
public class FinanceBudgetAiRecommendReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预算月份 (YYYY-MM，如 2026-10，为空默认当月)")
    private String budgetMonth;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "家庭组/机构ID(为空自动获取当前登录用户的家庭组)")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "归属用户ID(可选)")
    private Long belongTo;

    @ApiModelProperty(value = "期望偏好目标(如: 节约省钱, 适度平衡, 品质生活)")
    private String targetGoal;
}
