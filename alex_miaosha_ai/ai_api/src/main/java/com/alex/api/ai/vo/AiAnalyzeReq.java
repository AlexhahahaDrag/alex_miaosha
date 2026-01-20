package com.alex.api.ai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * AI Agent：
 * AI 分析请求
 * - 作为跨模块的“契约对象”（Feign/Controller 入参）
 */
@Data
@ApiModel(value = "AiAnalyzeReq", description = "AI 分析请求")
public class AiAnalyzeReq implements Serializable {

    @ApiModelProperty(value = "业务类型（可选，如 finance/coupon/order 等）")
    private String bizType;

    @ApiModelProperty(value = "待分析内容（必填）")
    private String content;

    @ApiModelProperty(value = "上下文（可选，结构化参数）")
    private Map<String, Object> context;

    @ApiModelProperty(value = "分析深度（可选：1-3，默认 1）")
    private Integer depth;

    /**
     * AI Agent：
     * 以下字段用于在“调用方”按需覆盖服务端默认配置（向后兼容：不传则走服务端默认）
     */
    @ApiModelProperty(value = "指定引擎（可选：deepseek / rule-based），优先级高于服务端默认配置")
    private String engine;

    @ApiModelProperty(value = "指定模型（可选，例如：deepseek-chat / deepseek-reasoner）")
    private String model;

    @ApiModelProperty(value = "温度（可选）")
    private Double temperature;

    @ApiModelProperty(value = "最大 tokens（可选）")
    private Integer maxTokens;
}


