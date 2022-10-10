package com.alex.finance.entity.finance;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  财务信息表类
 * @author:       majf
 * @createDate: 2022-10-10 16:55:59
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("finance_info")
@ApiModel(value = "FinanceInfo对象", description = "财务信息表")
public class FinanceInfo extends BaseEntity<FinanceInfo>{

    @ApiModelProperty(value = "名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "类别")
    @TableField("type_code")
    private String typeCode;

    @ApiModelProperty(value = "钱数")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "来源")
    @TableField("from_source")
    private String fromSource;

}
