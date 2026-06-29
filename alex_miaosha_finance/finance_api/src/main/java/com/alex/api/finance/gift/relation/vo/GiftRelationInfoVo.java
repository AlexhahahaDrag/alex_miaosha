package com.alex.api.finance.gift.relation.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftRelationInfoVo", description = "gift relation vo")
public class GiftRelationInfoVo extends BaseVo<GiftRelationInfoVo> {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "org id")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "user id")
    private Long userId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "person id")
    private Long personId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "relation person id")
    private Long relationPersonId;

    @ApiModelProperty(value = "relation type")
    private String relationType;

    @ApiModelProperty(value = "remark")
    private String remark;
}
