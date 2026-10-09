package com.alex.api.finance.vo.finance;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * description: 保存月度零花钱预算配置请求体
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "FinanceBudgetSaveReq", description = "保存月度零花钱预算配置请求体")
public class FinanceBudgetSaveReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "家庭组/机构ID(为空自动获取当前登录用户的家庭组)")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "归属用户ID(可选，预留家庭组下特定个人)")
    private Long belongTo;

    @NotBlank(message = "预算月份不能为空")
    @ApiModelProperty(value = "预算月份 (格式: YYYY-MM)", required = true, example = "2026-10")
    private String budgetMonth;

    public void setYearMonth(String yearMonth) {
        if (this.budgetMonth == null) {
            this.budgetMonth = yearMonth;
        }
    }

    public String getYearMonth() {
        return this.budgetMonth;
    }

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.00", message = "预算金额不能为负数")
    @ApiModelProperty(value = "月度零花钱预算金额", required = true)
    private BigDecimal budgetAmount;

    @ApiModelProperty(value = "收支类型(expense:支出, income:收入，为空默认expense)")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "纳入统计的类别编码列表(为空表示统计全部非转账支出)")
    private List<String> categoryCodes;
}
