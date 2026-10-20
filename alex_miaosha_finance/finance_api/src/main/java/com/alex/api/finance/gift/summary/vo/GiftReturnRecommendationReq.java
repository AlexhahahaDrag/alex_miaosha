package com.alex.api.finance.gift.summary.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftReturnRecommendationReq", description = "AI 智能回礼档位测算请求")
public class GiftReturnRecommendationReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "亲友ID", required = true)
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long personId;

    @ApiModelProperty(value = "待办事件类型 (WEDDING:结婚宴请, FULL_MONTH:满月百天, BIRTHDAY:寿宴生日, HOUSEWARMING:乔迁之喜, FUNERAL:白事慰问, GENERAL:通用礼往)", required = true)
    private String eventType;

    @ApiModelProperty(value = "城市层级 (TIER_1:一线城市, TIER_2:新一线/二线城市, TIER_3:三四线/县域，默认 TIER_2)")
    private String cityTier = "TIER_2";
}
