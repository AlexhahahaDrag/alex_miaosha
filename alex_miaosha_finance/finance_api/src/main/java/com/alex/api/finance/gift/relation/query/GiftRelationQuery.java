package com.alex.api.finance.gift.relation.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftRelationQuery", description = "gift relation query")
public class GiftRelationQuery implements Serializable {

    @ApiModelProperty(value = "person id")
    private Long personId;

    @ApiModelProperty(value = "relation person id")
    private Long relationPersonId;

    @ApiModelProperty(value = "relation type")
    private String relationType;
}
