package com.alex.api.finance.gift.record.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftRecordAiParseReq", description = "自然语言智能记账解析请求")
public class GiftRecordAiParseReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "记账自然语言内容不能为空")
    @ApiModelProperty(value = "自然语言文本（例如：今天下午参加老同学王强儿子满月酒，在喜来登随了600元红包）", required = true)
    private String content;

    @ApiModelProperty(value = "默认流水方向（可选：GIVE 随礼 / RECEIVE 收礼，若自然语言中未提及则默认此值）")
    private String defaultDirection;
}
