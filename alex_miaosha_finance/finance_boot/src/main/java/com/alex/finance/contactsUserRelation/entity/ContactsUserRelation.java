package com.alex.finance.contactsUserRelation.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  联系人关系分类字典表
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("contacts_user_relation_info_t")
@ApiModel(value = "ContactsUserRelation对象", description = "联系人关系分类字典表")
public class ContactsUserRelation extends BaseEntity<ContactsUserRelation> {

	@ApiModelProperty(value = "用户ID，为空表示公共字典，有值表示用户自定义分类")
	@TableField("user_id")
	private Long userId;

	@ApiModelProperty(value = "关系标签，如：重要客户、潜在客户等")
	@TableField("relationship_tag")
	private String relationshipTag;

	@ApiModelProperty(value = "重要程度，1-普通，2-重要，3-非常重要")
	@TableField("importance")
	private Integer importance;

	@ApiModelProperty(value = "描述信息")
	@TableField("description")
	private String description;

	@ApiModelProperty(value = "备注信息")
	@TableField("remarks")
	private String remarks;

	@ApiModelProperty(value = "是否启用，0-禁用，1-启用")
	@TableField("is_enabled")
	private Integer isEnabled;

}

