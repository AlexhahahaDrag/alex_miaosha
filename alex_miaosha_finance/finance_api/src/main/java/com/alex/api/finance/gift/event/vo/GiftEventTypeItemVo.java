package com.alex.api.finance.gift.event.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftEventTypeItemVo", description = "事由类型选项")
public class GiftEventTypeItemVo {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("选项ID")
    private Long id;

    @ApiModelProperty("展示名称")
    private String name;

    @ApiModelProperty("编码 (预设使用)")
    private String eventCode;

    @ApiModelProperty("事由分类")
    private String category;

    @ApiModelProperty("图标/Emoji")
    private String icon;

    @ApiModelProperty("状态: 0禁用, 1启用")
    private Integer status;

    @ApiModelProperty("累计使用次数")
    private Integer useCount;

    @ApiModelProperty("默认推荐金额")
    private java.math.BigDecimal defaultAmount;

    @ApiModelProperty("排序值")
    private Integer sortOrder;
}
