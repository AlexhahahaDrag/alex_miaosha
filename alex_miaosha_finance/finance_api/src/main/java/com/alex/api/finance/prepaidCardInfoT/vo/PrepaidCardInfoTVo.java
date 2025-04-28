package com.alex.api.finance.prepaidCardInfoT.vo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  消费卡信息表Vo
 * author:       alex
 * createDate:   2025-04-28 20:58:16
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PrepaidCardInfoTVo", description = "消费卡信息表Vo")
public class PrepaidCardInfoTVo extends BaseVo<PrepaidCardInfoTVo>{

    @ApiModelProperty(value = "卡号（业务唯一标识）")
    private Long cardId;

    @ApiModelProperty(value = "卡名称")
    private String cardName;

    @ApiModelProperty(value = "持卡人ID")
    private String userId;

    @ApiModelProperty(value = "初始金额")
    private BigDecimal initialBalance;

    @ApiModelProperty(value = "当前余额")
    private BigDecimal currentBalance;

    @ApiModelProperty(value = "过期日期")
    private LocalDate expireDate;

    @ApiModelProperty(value = "状态（正常/冻结/挂失/过期）")
    private String cardStatus;

    @ApiModelProperty(value = "版本")
    private Integer version;

}
