package com.alex.finance.gift.eventoption.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@TableName("gift_event_type_option_t")
@ApiModel(value = "GiftEventTypeOption", description = "事由类型词典")
public class GiftEventTypeOption extends BaseEntity<GiftEventTypeOption> {

    @ApiModelProperty("组织ID，系统预设为0")
    @TableField("org_id")
    private Long orgId;

    @ApiModelProperty("创建用户ID，系统预设为0")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("类型：SYSTEM/CUSTOM")
    @TableField("option_type")
    private String optionType;

    @ApiModelProperty("预设 code，自定义为空")
    @TableField("event_code")
    private String eventCode;

    @ApiModelProperty("类型展示文案")
    @TableField("event_label")
    private String eventLabel;

    @ApiModelProperty("排序，系统预设使用")
    @TableField("sort_order")
    private Integer sortOrder;

    @ApiModelProperty("最近使用时间")
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    @ApiModelProperty("事由分类")
    @TableField("category")
    private String category;

    @ApiModelProperty("图标/Emoji")
    @TableField("icon")
    private String icon;

    @ApiModelProperty("状态: 0禁用, 1启用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("累计使用次数")
    @TableField("use_count")
    private Integer useCount;

    @ApiModelProperty("默认推荐金额")
    @TableField("default_amount")
    private java.math.BigDecimal defaultAmount;
}
