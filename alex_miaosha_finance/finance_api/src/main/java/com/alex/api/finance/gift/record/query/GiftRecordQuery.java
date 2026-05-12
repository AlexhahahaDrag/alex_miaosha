package com.alex.api.finance.gift.record.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftRecordQuery", description = "gift record query")
public class GiftRecordQuery implements Serializable {

    @ApiModelProperty(value = "keyword")
    private String keyword;

    @ApiModelProperty(value = "event id")
    private Long eventId;

    @ApiModelProperty(value = "giver person id")
    private Long giverPersonId;

    @ApiModelProperty(value = "receiver person id")
    private Long receiverPersonId;

    @ApiModelProperty(value = "direction")
    private String direction;

    @ApiModelProperty(value = "return status")
    private String returnStatus;

    @ApiModelProperty(value = "pay time start")
    private LocalDateTime payTimeStart;

    @ApiModelProperty(value = "pay time end")
    private LocalDateTime payTimeEnd;

    @ApiModelProperty(value = "amount min")
    private BigDecimal amountMin;

    @ApiModelProperty(value = "amount max")
    private BigDecimal amountMax;
}
