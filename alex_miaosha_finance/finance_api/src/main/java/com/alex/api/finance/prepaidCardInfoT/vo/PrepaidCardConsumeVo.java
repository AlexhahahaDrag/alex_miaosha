package com.alex.api.finance.prepaidCardInfoT.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description:  消费卡信息表Vo
 * author:       alex
 * createDate:   2025-04-30 08:21:48
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PrepaidCardConsumeVo", description = "消费卡消费Vo")
public class PrepaidCardConsumeVo extends BaseVo<PrepaidCardConsumeVo> {

    @ApiModelProperty(value = "卡编码")
    private String cardId;

    @ApiModelProperty(value = "卡名称")
    private String cardName;

    @ApiModelProperty(value = "当前操作人")
    private Long userId;

    @ApiModelProperty(value = "消费金额")
    private BigDecimal consumeAmount;

    @NotBlank(message = "操作类型不能为空")
    @ApiModelProperty(value = "操作类型", required = true)
    private String type;

    @ApiModelProperty(value = "消费日期")
    private LocalDateTime consumeTime;
}
