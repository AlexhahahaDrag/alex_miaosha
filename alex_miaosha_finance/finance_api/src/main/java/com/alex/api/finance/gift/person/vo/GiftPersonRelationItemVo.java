package com.alex.api.finance.gift.person.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftPersonRelationItemVo", description = "联系人关系选项")
public class GiftPersonRelationItemVo {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("词典主键")
    private Long id;

    @ApiModelProperty("关系名称")
    private String name;
}
