package com.alex.api.finance.personalGift.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description:  个人随礼信息表Vo
 * author:       alex
 * createDate:   2024-07-10 10:01:28
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PersonalGiftVo", description = "个人随礼信息表Vo")
public class PersonalGiftVo extends BaseVo<PersonalGiftVo>{

    @ApiModelProperty(value = "事件名称")
    private String eventName;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "人员")
    private String otherPerson;

    @ApiModelProperty(value = "随礼时间")
    private LocalDateTime eventTime;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "动作,字典(gift_action) 1：给予,2:随礼)")
    private String action;

    @ApiModelProperty(value = "通知次数")
    private Integer noticeNum;

    @ApiModelProperty(value = "关键字")
    private String keyword;
}
