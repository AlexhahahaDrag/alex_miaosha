package com.alex.api.finance.message.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统消息与业务到期提醒 VO
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "SysMessageInfoVo", description = "系统消息与业务到期提醒视图对象")
public class SysMessageInfoVo extends BaseVo<SysMessageInfoVo> {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "所属机构ID")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "接收用户ID")
    private Long userId;

    @ApiModelProperty(value = "消息大类：SYSTEM / GIFT_EVENT / COUPON_EXPIRE / CARD_EXPIRE")
    private String msgType;

    @ApiModelProperty(value = "子类型：WELCOME / ANNOUNCE / D_DAY / D_MINUS_1 / D_MINUS_3")
    private String subType;

    @ApiModelProperty(value = "优先级：HIGH / MEDIUM / NORMAL")
    private String priority;

    @ApiModelProperty(value = "消息标题")
    private String title;

    @ApiModelProperty(value = "消息正文")
    private String content;

    @ApiModelProperty(value = "关联业务主键ID（字符串防精度丢失）")
    private String bizId;

    @ApiModelProperty(value = "关联业务类型：GIFT_EVENT / COUPON / ACCOUNT_RECORD / PREPAID_CARD / SYSTEM")
    private String bizType;

    @ApiModelProperty(value = "跳转路由路径")
    private String jumpUrl;

    @ApiModelProperty(value = "阅读状态：0-未读，1-已读")
    private Integer readStatus;

    @ApiModelProperty(value = "阅读时间")
    private LocalDateTime readTime;

    @ApiModelProperty(value = "幂等防重键")
    private String dedupKey;

    @ApiModelProperty(value = "提醒归属日期")
    private LocalDate remindDate;

    @ApiModelProperty(value = "关键词过滤（匹配标题或内容）")
    private String keyword;

    @ApiModelProperty(value = "仅查询未读消息")
    private Boolean onlyUnread;
}
