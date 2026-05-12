package com.alex.api.finance.gift.person.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftPersonQuery", description = "gift person query")
public class GiftPersonQuery implements Serializable {

    @ApiModelProperty(value = "keyword")
    private String keyword;

    @ApiModelProperty(value = "relation type")
    private String relationType;
}
