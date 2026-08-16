package com.alex.finance.gift.eventoption.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@TableName("gift_event_type_user_config_t")
@ApiModel(value = "GiftEventTypeUserConfig", description = "事由分类用户/机构个性化配置")
public class GiftEventTypeUserConfig extends BaseEntity<GiftEventTypeUserConfig> {

    @ApiModelProperty("关联的事由分类选项ID")
    @TableField("option_id")
    private Long optionId;

    @ApiModelProperty("机构/家庭组ID")
    @TableField("org_id")
    private Long orgId;

    @ApiModelProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("状态: 0禁用, 1启用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("个性化推荐金额")
    @TableField("custom_amount")
    private BigDecimal customAmount;
}
