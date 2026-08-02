package com.alex.api.finance.gift.record.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
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
@ApiModel(value = "GiftRecordInfoVo", description = "gift record vo")
public class GiftRecordInfoVo extends BaseVo<GiftRecordInfoVo> {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "org id")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "user id")
    private Long userId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "event id")
    private Long eventId;

    @ApiModelProperty(value = "event type code or custom label")
    private String eventType;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "event type option id")
    private Long eventOptionId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "giver person id")
    private Long giverPersonId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "receiver person id")
    private Long receiverPersonId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "related source record id")
    private Long relatedRecordId;

    @ApiModelProperty(value = "gift direction: GIVE/RECEIVE/RETURN")
    private String direction;

    @ApiModelProperty(value = "amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "pay time")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "returned flag")
    private Integer returnedFlag;

    @ApiModelProperty(value = "remark")
    private String remark;

    @ApiModelProperty(value = "event name")
    private String eventName;

    @ApiModelProperty(value = "giver person name")
    private String giverPersonName;

    @ApiModelProperty(value = "receiver person name")
    private String receiverPersonName;

    @ApiModelProperty(value = "display person name")
    private String personName;

    @ApiModelProperty(value = "payment method")
    private String paymentMethod;

    @ApiModelProperty(value = "handler name")
    private String handlerName;
}
