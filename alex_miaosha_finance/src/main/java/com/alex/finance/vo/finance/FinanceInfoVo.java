package com.alex.finance.vo.finance;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @description:  财务信息表Vo
 * @author:       majf
 * @createDate:   2022-10-10 18:02:03
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "FinanceInfoVo", description = "财务信息表Vo")
public class FinanceInfoVo extends BaseVo<FinanceInfoVo> {

    @ApiModelProperty(name = "name", value = "名称")
    private String name;

    @ApiModelProperty(name = "typeCode", value = "类别")
    private String typeCode;

    @ApiModelProperty(name = "amount", value = "钱数")
    private BigDecimal amount;

    @ApiModelProperty(name = "fromSource", value = "来源")
    private String fromSource;

    @ApiModelProperty(name = "isValid", value = "来源")
    private Integer isValid;
}
