package com.alex.finance.vo.finance;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @description:  财务信息表Vo
 * @author:       majf
 * @createDate:   2022-10-10 16:56:00
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "FinanceInfoVo", description = "财务信息表Vo")
public class FinanceInfoVo extends Model<FinanceInfoVo> {

    @ApiModelProperty(value = "名称")
    @JsonProperty("NAME")
    private String name;

    @ApiModelProperty(value = "类别")
    @JsonProperty("TYPE_CODE")
    private String typeCode;

    @ApiModelProperty(value = "钱数")
    @JsonProperty("AMOUNT")
    private BigDecimal amount;

    @ApiModelProperty(value = "来源")
    @JsonProperty("FROM_SOURCE")
    private String fromSource;

}
