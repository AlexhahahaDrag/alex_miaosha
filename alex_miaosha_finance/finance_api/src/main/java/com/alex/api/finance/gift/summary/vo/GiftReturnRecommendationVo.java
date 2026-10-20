package com.alex.api.finance.gift.summary.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftReturnRecommendationVo", description = "AI 智能回礼档位测算响应")
public class GiftReturnRecommendationVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "亲友姓名")
    private String personName;

    @ApiModelProperty(value = "关系类型名称")
    private String relationTypeName;

    @ApiModelProperty(value = "历史互赠参考基准金额")
    private BigDecimal historicalBaseAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "最低体面档金额 (底线守礼)")
    private BigDecimal minDecentAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "推荐平衡档金额 (最体面合适)")
    private BigDecimal recommendedAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "热络加码档金额 (深化情谊)")
    private BigDecimal generousAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "民间吉利备选档位列表")
    private List<BigDecimal> auspiciousTierList;

    @ApiModelProperty(value = "AI 测算推导依据与规则摘要")
    private String ruleSummary;

    @ApiModelProperty(value = "随赠贺词与祝颂语锦囊")
    private String blessingWords;
}
