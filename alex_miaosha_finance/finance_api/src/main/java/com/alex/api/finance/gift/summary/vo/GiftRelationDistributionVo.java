package com.alex.api.finance.gift.summary.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftRelationDistributionVo", description = "gift relation distribution")
public class GiftRelationDistributionVo {

    private String relationType;

    private Long count = 0L;
}
