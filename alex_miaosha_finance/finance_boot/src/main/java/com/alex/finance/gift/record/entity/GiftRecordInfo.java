package com.alex.finance.gift.record.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description: 礼尚往来礼金记录表
 * author: alex
 * version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("gift_record_info_t")
@ApiModel(value = "GiftRecordInfo对象", description = "礼尚往来礼金记录表")
public class GiftRecordInfo extends BaseEntity<GiftRecordInfo> {

    @ApiModelProperty(value = "组织ID")
    @TableField("org_id")
    private Long orgId;

    @ApiModelProperty(value = "归属用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "事件ID")
    @TableField("event_id")
    private Long eventId;

    @ApiModelProperty(value = "送礼人员ID")
    @TableField("giver_person_id")
    private Long giverPersonId;

    @ApiModelProperty(value = "收礼人员ID")
    @TableField("receiver_person_id")
    private Long receiverPersonId;

    @ApiModelProperty(value = "关联原始收礼记录ID")
    @TableField("related_record_id")
    private Long relatedRecordId;

    @ApiModelProperty(value = "礼金方向：GIVE/RECEIVE/RETURN")
    @TableField("direction")
    private String direction;

    @ApiModelProperty(value = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "礼金时间")
    @TableField("pay_time")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "是否已回礼")
    @TableField("returned_flag")
    private Integer returnedFlag;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;
}
