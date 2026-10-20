package com.alex.api.finance.gift.summary.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftPersonBalanceVo", description = "亲友互赠收支平衡矩阵实体")
public class GiftPersonBalanceVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "亲友ID")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long personId;

    @ApiModelProperty(value = "亲友姓名")
    private String personName;

    @ApiModelProperty(value = "关系类型编码 (如 relatives/friends/colleagues)")
    private String relationType;

    @ApiModelProperty(value = "关系类型名称")
    private String relationTypeName;

    @ApiModelProperty(value = "TA送我(我收礼)总额")
    private BigDecimal totalReceiveAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "我送TA(我随礼)总额")
    private BigDecimal totalGiveAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "人情净差 (Receive - Give，正数表示我方盈余，负数表示我方借出)")
    private BigDecimal netBalance = BigDecimal.ZERO;

    @ApiModelProperty(value = "平衡状态 (SURPLUS:礼金盈余/多收未回, DEFICIT:礼金借出/多送待收, BALANCED:基本持平)")
    private String balanceStatus = "BALANCED";

    @ApiModelProperty(value = "往来记录总笔数")
    private Integer recordCount = 0;

    @ApiModelProperty(value = "最近一次往来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestRecordTime;

    @ApiModelProperty(value = "是否触发失衡预警 (如大额单向未回礼)")
    private Boolean isImbalancedAlert = false;

    @ApiModelProperty(value = "预警原因说明")
    private String alertReason;
}
