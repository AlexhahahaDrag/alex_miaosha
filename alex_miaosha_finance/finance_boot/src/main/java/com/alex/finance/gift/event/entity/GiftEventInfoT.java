package com.alex.finance.gift.event.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * description: 礼尚往来事件表
 * author: alex
 * version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("gift_event_info_t")
@ApiModel(value = "GiftEventInfoT对象", description = "礼尚往来事件表")
public class GiftEventInfoT extends BaseEntity<GiftEventInfoT> {

    @ApiModelProperty(value = "组织ID")
    @TableField("org_id")
    private Long orgId;

    @ApiModelProperty(value = "归属用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "事件名称")
    @TableField("event_name")
    private String eventName;

    @ApiModelProperty(value = "事件类型")
    @TableField("event_type")
    private String eventType;

    @ApiModelProperty(value = "事件时间")
    @TableField("event_time")
    private LocalDateTime eventTime;

    @ApiModelProperty(value = "主办人员ID")
    @TableField("host_person_id")
    private Long hostPersonId;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;
}
