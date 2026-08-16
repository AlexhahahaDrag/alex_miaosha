package com.alex.api.finance.gift.event.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "GiftEventInfoVo", description = "gift event vo")
public class GiftEventInfoVo extends BaseVo<GiftEventInfoVo> {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "org id")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "user id")
    private Long userId;

    @ApiModelProperty(value = "event name")
    private String eventName;

    @ApiModelProperty(value = "event type")
    private String eventType;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "event type option id")
    private Long eventTypeOptionId;

    @ApiModelProperty(value = "event time")
    private LocalDateTime eventTime;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "host person id")
    private Long hostPersonId;

    @ApiModelProperty(value = "host person name")
    private String hostPersonName;

    @ApiModelProperty(value = "participant count")
    private Long participantCount;

    @ApiModelProperty(value = "total amount")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "remark")
    private String remark;
}
