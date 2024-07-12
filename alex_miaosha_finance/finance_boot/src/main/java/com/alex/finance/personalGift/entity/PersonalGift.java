package com.alex.finance.personalGift.entity;

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
 * description:  个人随礼信息表类
 * author:       alex
 * createDate: 2024-07-10 10:01:28
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_personal_gift")
@ApiModel(value = "PersonalGift对象", description = "个人随礼信息表")
public class PersonalGift extends BaseEntity<PersonalGift>{

    @ApiModelProperty(value = "事件名称")
    @TableField("event_name")
    private String eventName;

    @ApiModelProperty(value = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "其他人")
    @TableField("other_person")
    private String otherPerson;

    @ApiModelProperty(value = "随礼时间")
    @TableField("event_time")
    private LocalDateTime eventTime;

    @ApiModelProperty(value = "备注")
    @TableField("remarks")
    private String remarks;

    @ApiModelProperty(value = "动作,字典(gift_action) 1：给予,2:随礼)")
    @TableField("`action`")
    private String action;

    @ApiModelProperty(value = "通知次数")
    @TableField("notice_num")
    private Integer noticeNum;

}
