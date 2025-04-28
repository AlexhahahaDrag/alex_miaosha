package com.alex.finance.prepaidConsumeRecordT.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * description:  消费卡交易记录表类
 * author:       alex
 * createDate: 2025-04-28 20:58:14
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("prepaid_consume_record_t")
@ApiModel(value = "PrepaidConsumeRecordT对象", description = "消费卡交易记录表")
public class PrepaidConsumeRecordT extends BaseEntity<PrepaidConsumeRecordT>{

    @ApiModelProperty(value = "卡号（关联prepaid_card_info_t.card_id）")
    @TableField("card_id")
    private Long cardId;

    @ApiModelProperty(value = "交易流水号（业务唯一）")
    @TableField("order_no")
    private String orderNo;

    @ApiModelProperty(value = "交易金额（正消费，负充值）")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易后余额")
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    @ApiModelProperty(value = "商户名称")
    @TableField("merchant_name")
    private String merchantName;

    @ApiModelProperty(value = "交易时间")
    @TableField("consume_time")
    private LocalDateTime consumeTime;

    @ApiModelProperty(value = "交易描述")
    @TableField("`description`")
    private String description;

}
