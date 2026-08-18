package com.alex.api.finance.gift.event.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftEventQuery", description = "gift event query")
public class GiftEventQuery implements Serializable {

    @ApiModelProperty(value = "keyword")
    private String keyword;

    @ApiModelProperty(value = "event type")
    private String eventType;

    @ApiModelProperty(value = "event time start")
    private LocalDateTime eventTimeStart;

    @ApiModelProperty(value = "event time end")
    private LocalDateTime eventTimeEnd;
}
