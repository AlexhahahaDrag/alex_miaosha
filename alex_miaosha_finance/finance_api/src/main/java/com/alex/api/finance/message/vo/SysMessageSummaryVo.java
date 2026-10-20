package com.alex.api.finance.message.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 消息中心未读汇总统计 VO
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "SysMessageSummaryVo", description = "消息中心未读汇总统计")
public class SysMessageSummaryVo {

    @ApiModelProperty(value = "总未读消息数")
    private Long unreadTotal = 0L;

    @ApiModelProperty(value = "系统消息未读数")
    private Long systemUnread = 0L;

    @ApiModelProperty(value = "礼尚往来事件提醒未读数")
    private Long giftEventUnread = 0L;

    @ApiModelProperty(value = "人情回礼备忘未读数")
    private Long giftReturnUnread = 0L;

    @ApiModelProperty(value = "消费券到期提醒未读数")
    private Long couponExpireUnread = 0L;

    @ApiModelProperty(value = "猫超卡/预付卡到期提醒未读数")
    private Long cardExpireUnread = 0L;
}
