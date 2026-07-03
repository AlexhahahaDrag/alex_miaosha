package com.alex.api.finance.gift.event.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftEventTypeItemVo", description = "事由类型选项")
public class GiftEventTypeItemVo {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("选项ID")
    private Long id;

    @ApiModelProperty("展示名称")
    private String name;
}
