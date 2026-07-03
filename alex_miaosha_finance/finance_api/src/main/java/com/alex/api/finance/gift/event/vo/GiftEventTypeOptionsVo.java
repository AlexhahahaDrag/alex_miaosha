package com.alex.api.finance.gift.event.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftEventTypeOptionsVo", description = "事由类型下拉选项")
public class GiftEventTypeOptionsVo {

    @ApiModelProperty("系统预设类型")
    private List<GiftEventTypeItemVo> presets;

    @ApiModelProperty("家庭组共享的自定义类型")
    private List<GiftEventTypeItemVo> customs;
}
