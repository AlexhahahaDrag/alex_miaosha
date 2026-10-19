package com.alex.api.finance.vo.finance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * description: 通用智能记账文本解析请求 (支持日常收支、购物小票、预付卡流水)
 * author: alex
 * createDate: 2026-10-19
 * version: 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UniversalFinanceAiParseReq", description = "通用智能记账文本解析请求")
public class UniversalFinanceAiParseReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记账场景：AUTO_DETECT(自动检测), DAILY_EXPENSE(日常收支), SHOP_RECEIPT(购物小票), PREPAID_CARD(预付卡流水)", example = "AUTO_DETECT")
    @Builder.Default
    private String scene = "AUTO_DETECT";

    @NotBlank(message = "待解析文本内容不能为空")
    @ApiModelProperty(value = "自然语言流水、小票粘贴文本或转录文本", required = true, example = "昨天中午和同事聚餐微信付了128元，麦当劳汉堡")
    private String text;

    @ApiModelProperty(value = "指定的归属人ID(可选，默认为当前登录用户)")
    private Long belongTo;
}
