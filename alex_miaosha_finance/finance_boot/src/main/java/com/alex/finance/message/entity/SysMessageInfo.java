package com.alex.finance.message.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统消息与业务到期提醒实体
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_message_info_t")
@ApiModel(value = "SysMessageInfo对象", description = "系统消息与业务到期提醒表")
public class SysMessageInfo extends BaseEntity<SysMessageInfo> {

    @ApiModelProperty(value = "所属机构ID")
    @TableField("org_id")
    private Long orgId;

    @ApiModelProperty(value = "接收用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "消息大类：SYSTEM / GIFT_EVENT / COUPON_EXPIRE / CARD_EXPIRE")
    @TableField("msg_type")
    private String msgType;

    @ApiModelProperty(value = "子类型：WELCOME / ANNOUNCE / D_DAY / D_MINUS_1 / D_MINUS_3")
    @TableField("sub_type")
    private String subType;

    @ApiModelProperty(value = "优先级：HIGH / MEDIUM / NORMAL")
    @TableField("priority")
    private String priority;

    @ApiModelProperty(value = "消息标题")
    @TableField("title")
    private String title;

    @ApiModelProperty(value = "消息正文")
    @TableField("content")
    private String content;

    @ApiModelProperty(value = "关联业务主键ID")
    @TableField("biz_id")
    private String bizId;

    @ApiModelProperty(value = "关联业务类型：GIFT_EVENT / COUPON / ACCOUNT_RECORD / PREPAID_CARD / SYSTEM")
    @TableField("biz_type")
    private String bizType;

    @ApiModelProperty(value = "跳转路由路径")
    @TableField("jump_url")
    private String jumpUrl;

    @ApiModelProperty(value = "阅读状态：0-未读，1-已读")
    @TableField("read_status")
    private Integer readStatus;

    @ApiModelProperty(value = "阅读时间")
    @TableField("read_time")
    private LocalDateTime readTime;

    @ApiModelProperty(value = "幂等防重键")
    @TableField("dedup_key")
    private String dedupKey;

    @ApiModelProperty(value = "提醒归属日期")
    @TableField("remind_date")
    private LocalDate remindDate;
}
