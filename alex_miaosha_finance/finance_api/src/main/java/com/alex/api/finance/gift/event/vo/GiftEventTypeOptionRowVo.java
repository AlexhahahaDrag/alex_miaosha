package com.alex.api.finance.gift.event.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "GiftEventTypeOptionRowVo", description = "事由类型词典查询行")
public class GiftEventTypeOptionRowVo {

    @JsonSerialize(using = Long2StringSerializer.class)
    private Long id;

    private String optionType;

    private String eventCode;

    private String eventLabel;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("家庭组 orgId，系统预设为0")
    private Long orgId;

    private String category;

    private String icon;

    private Integer status;

    private Integer useCount;

    private java.math.BigDecimal defaultAmount;

    private Integer sortOrder;
}
