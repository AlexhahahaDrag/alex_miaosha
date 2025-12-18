package com.alex.api.finance.cpnRedemptionRecordInfo.vo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @description:  消费券核销记录表 (按数量核销)视图
 * @author:       alex
 * @createDate:   2025-12-17 17:54:00
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnRedemptionRecordInfoVo", description = "消费券核销记录表 (按数量核销)Vo")
public class CpnRedemptionRecordInfoVo extends BaseVo<CpnRedemptionRecordInfoVo>{

    @ApiModelProperty(value = "被核销的券实例ID (外键关联 cpn_user_coupon_info_t.id)")
    private Long userCouponId;

    @ApiModelProperty(value = "核销用户ID")
    // 统一 userId 为 Long，避免前后端/DB bigint 对齐问题
    private Long userId;

    @ApiModelProperty(value = "关联的订单ID")
    private Long orderId;

    @ApiModelProperty(value = "本次核销数量 (固定为1)")
    private Integer redemptionQuantity;

    @ApiModelProperty(value = "核销券的面值（参考）")
    private BigDecimal redemptionValue;

    @ApiModelProperty(value = "核销时间")
    private LocalDateTime redemptionTime;

    @ApiModelProperty(value = "核销商家ID")
    private Integer merchantId;

    @ApiModelProperty(value = "备注")
    private String remarks;

}
