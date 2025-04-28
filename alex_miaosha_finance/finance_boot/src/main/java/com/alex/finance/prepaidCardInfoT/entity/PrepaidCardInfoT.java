package com.alex.finance.prepaidCardInfoT.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * description:  消费卡信息表类
 * author:       alex
 * createDate: 2025-04-28 20:58:16
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("prepaid_card_info_t")
@ApiModel(value = "PrepaidCardInfoT对象", description = "消费卡信息表")
public class PrepaidCardInfoT extends BaseEntity<PrepaidCardInfoT>{

    @ApiModelProperty(value = "卡号（业务唯一标识）")
    @TableField("card_id")
    private Long cardId;

    @ApiModelProperty(value = "卡名称")
    @TableField("card_name")
    private String cardName;

    @ApiModelProperty(value = "持卡人ID")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "初始金额")
    @TableField("initial_balance")
    private BigDecimal initialBalance;

    @ApiModelProperty(value = "当前余额")
    @TableField("current_balance")
    private BigDecimal currentBalance;

    @ApiModelProperty(value = "过期日期")
    @TableField("expire_date")
    private LocalDate expireDate;

    @ApiModelProperty(value = "状态（正常/冻结/挂失/过期）")
    @TableField("card_status")
    private String cardStatus;

    @ApiModelProperty(value = "版本")
    @TableField("version")
    private Integer version;

}
