package com.alex.api.finance.gift.person.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftPersonRelationOptionRowVo", description = "关系词典查询行")
public class GiftPersonRelationOptionRowVo {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("SYSTEM|CUSTOM")
    private String optionType;

    @ApiModelProperty("预设 code")
    private String relationCode;

    @ApiModelProperty("展示文案")
    private String relationLabel;
}
