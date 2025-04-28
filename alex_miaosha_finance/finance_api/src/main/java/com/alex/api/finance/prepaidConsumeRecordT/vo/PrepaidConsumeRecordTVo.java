package com.alex.api.finance.prepaidConsumeRecordT.vo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  消费卡交易记录表Vo
 * author:       alex
 * createDate:   2025-04-28 20:58:14
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PrepaidConsumeRecordTVo", description = "消费卡交易记录表Vo")
public class PrepaidConsumeRecordTVo extends BaseVo<PrepaidConsumeRecordTVo>{

    @ApiModelProperty(value = "卡号（关联prepaid_card_info_t.card_id）")
    private Long cardId;

    @ApiModelProperty(value = "交易流水号（业务唯一）")
    private String orderNo;

    @ApiModelProperty(value = "交易金额（正消费，负充值）")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易后余额")
    private BigDecimal balanceAfter;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "交易时间")
    private LocalDateTime consumeTime;

    @ApiModelProperty(value = "交易描述")
    private String description;

}
