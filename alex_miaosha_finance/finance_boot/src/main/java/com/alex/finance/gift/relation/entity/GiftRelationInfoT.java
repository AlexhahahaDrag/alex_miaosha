package com.alex.finance.gift.relation.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description: 礼尚往来关系表
 * author: alex
 * version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("gift_relation_info_t")
@ApiModel(value = "GiftRelationInfoT对象", description = "礼尚往来关系表")
public class GiftRelationInfoT extends BaseEntity<GiftRelationInfoT> {

    @ApiModelProperty(value = "组织ID")
    @TableField("org_id")
    private Long orgId;

    @ApiModelProperty(value = "归属用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "人员ID")
    @TableField("person_id")
    private Long personId;

    @ApiModelProperty(value = "关联人员ID")
    @TableField("relation_person_id")
    private Long relationPersonId;

    @ApiModelProperty(value = "关系类型")
    @TableField("relation_type")
    private String relationType;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;
}
