package com.alex.api.finance.cpnCouponInfo.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * description:  消费券信息导入VO（用于 Excel 导入，支持中文列名）
 * author:       alex
 * createDate:   2025-12-17 17:54:42
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnCouponInfoImportVo", description = "消费券信息导入VO")
public class CpnCouponInfoImportVo {

    @Excel(name = "消费券名称")
    @ApiModelProperty(value = "消费券名称")
    private String couponName;

    @Excel(name = "总发行数量")
    @ApiModelProperty(value = "消费券总发行数量")
    private Integer totalQuantity;

    @Excel(name = "有效期开始时间", format = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "有效期开始时间")
    private String startDate;

    @Excel(name = "有效期结束时间", format = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "有效期结束时间")
    private String endDate;

    @Excel(name = "单张面值")
    @ApiModelProperty(value = "消费券单张面值")
    private BigDecimal unitValue;

    @Excel(name = "最低消费门槛")
    @ApiModelProperty(value = "最低消费门槛")
    private BigDecimal minSpend;
}

