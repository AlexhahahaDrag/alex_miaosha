package com.alex.api.finance.vo.accountRecordInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  Vo
 * author:       alex
 * createDate:   2023-04-08 16:27:39
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "AccountCountInfoVo", description = "过期单据账号统计对象")
public class AccountCountInfoVo {

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "账号名称")
    private String accountName;

    @ApiModelProperty(value = "数量")
    private Long num;
}
