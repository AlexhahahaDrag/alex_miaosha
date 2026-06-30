package com.alex.api.finance.gift.person.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftPersonRelationOptionsVo", description = "联系人关系下拉选项")
public class GiftPersonRelationOptionsVo {

    @ApiModelProperty("系统预设关系")
    private List<GiftPersonRelationItemVo> presets;

    @ApiModelProperty("当前用户私有的自定义关系")
    private List<GiftPersonRelationItemVo> customs;
}
