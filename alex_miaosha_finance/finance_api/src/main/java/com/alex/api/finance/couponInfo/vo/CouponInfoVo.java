package com.alex.api.finance.couponInfo.vo;

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
 * @description:  消费券信息表视图
 * @author:       alex
 * @createDate:   2025-12-17 11:56:28
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CouponInfoVo", description = "消费券信息表Vo")
public class CouponInfoVo extends BaseVo<CouponInfoVo>{

    @ApiModelProperty(value = "已消耗数量（核销数量汇总）")
    private Integer consumedQuantity;

    @ApiModelProperty(value = "剩余数量（totalQuantity - consumedQuantity）")
    private Integer remainingQuantity;

    @ApiModelProperty(value = "消费券名称")
    private String couponName;

    @ApiModelProperty(value = "消费券总发行数量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "有效期开始时间")
    private LocalDateTime startDate;

    @ApiModelProperty(value = "有效期结束时间")
    private LocalDateTime endDate;

    @ApiModelProperty(value = "消费券单张面值")
    private BigDecimal unitValue;

    @ApiModelProperty(value = "最低消费门槛")
    private BigDecimal minSpend;

}
