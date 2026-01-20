package com.alex.api.ai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI Agent：
 * AI 分析响应
 * - 作为跨模块的“契约对象”（Feign/Controller 出参）
 */
@Data
@ApiModel(value = "AiAnalyzeResp", description = "AI 分析响应")
public class AiAnalyzeResp implements Serializable {

    @ApiModelProperty(value = "请求ID（便于链路追踪）")
    private String requestId;

    @ApiModelProperty(value = "摘要")
    private String summary;

    @ApiModelProperty(value = "关键要点")
    private List<String> keyPoints;

    @ApiModelProperty(value = "使用的模型/策略标识（例如 rule-based / llm-openai / llm-local）")
    private String engine;

    @ApiModelProperty(value = "耗时（毫秒）")
    private Long costMs;
}


