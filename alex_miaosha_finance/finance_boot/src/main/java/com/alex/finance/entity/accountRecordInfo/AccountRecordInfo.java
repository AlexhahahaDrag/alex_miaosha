package com.alex.finance.entity.accountRecordInfo;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  类
 * @author:       alex
 * @createDate: 2023-04-08 16:27:39
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("account_record_info")
@ApiModel(value = "AccountRecordInfo对象", description = "")
public class AccountRecordInfo {

    @ApiModelProperty(value = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "有效期")
    @TableField("avli_date")
    private LocalDateTime avliDate;

    @ApiModelProperty(value = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "账号")
    @TableField("`account`")
    private String account;

    @ApiModelProperty(value = "是否发送提醒")
    @TableField("is_send")
    private Integer isSend;
}
