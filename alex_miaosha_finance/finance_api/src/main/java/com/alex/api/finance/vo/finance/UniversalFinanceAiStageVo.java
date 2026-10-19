package com.alex.api.finance.vo.finance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * description: 通用智能记账流式解析阶段进度 VO (SSE 分阶段推送事件)
 * author: alex
 * createDate: 2026-10-19
 * version: 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UniversalFinanceAiStageVo", description = "通用智能记账流式解析阶段进度")
public class UniversalFinanceAiStageVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "解析阶段编码：SCENE_ANALYSIS, EXTRACTING_AMOUNT, STRUCTURING_ITEMS, MATCHING_ENTITIES, COMPLETE, ERROR", example = "EXTRACTING_AMOUNT")
    private String stage;

    @ApiModelProperty(value = "解析进度百分比(0-100)", example = "40")
    private Integer progress;

    @ApiModelProperty(value = "阶段状态提示文案", example = "正在提取收支金额与关键语义要素...")
    private String message;

    @ApiModelProperty(value = "阶段性快照或最终完成载荷")
    private UniversalFinanceAiParseVo partialData;
}
