package com.alex.api.finance.vo.accountRecordInfo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  Vo
 * @author:       alex
 * @createDate:   2023-04-08 16:27:39
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "AccountRecordInfoVo", description = "Vo")
public class AccountRecordInfoVo {

    @ApiModelProperty(value = "id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "有效期")
    private LocalDateTime avliDate;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "是否发送提醒")
    private Integer isSend;

    @ApiModelProperty(value = "状态")
    private Integer status;
}
