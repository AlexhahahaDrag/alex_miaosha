package com.alex.finance.gift.person.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description: 礼尚往来人员表
 * author: alex
 * version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("gift_person_info_t")
@ApiModel(value = "GiftPersonInfo对象", description = "礼尚往来人员表")
public class GiftPersonInfo extends BaseEntity<GiftPersonInfo> {

    @ApiModelProperty(value = "组织ID")
    @TableField("org_id")
    private Long orgId;

    @ApiModelProperty(value = "归属用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "绑定系统用户ID")
    @TableField("bind_user_id")
    private Long bindUserId;

    @ApiModelProperty(value = "人员姓名")
    @TableField("person_name")
    private String personName;

    @ApiModelProperty(value = "手机号")
    @TableField("phone")
    private String phone;

    @ApiModelProperty(value = "头像 OSS 文件ID")
    @TableField(value = "avatar", updateStrategy = FieldStrategy.ALWAYS)
    private Long avatar;

    @ApiModelProperty(value = "关系类型")
    @TableField("relation_type")
    private String relationType;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty(value = "关系等级: CORE核心, IMPORTANT重要, NORMAL普通, WEAK弱关系")
    @TableField("relation_grade")
    private String relationGrade;
}
