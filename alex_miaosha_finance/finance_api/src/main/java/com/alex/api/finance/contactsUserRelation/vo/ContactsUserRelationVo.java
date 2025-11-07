package com.alex.api.finance.contactsUserRelation.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  联系人关系分类字典Vo
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 * AI Agent
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ContactsUserRelationVo", description = "联系人关系分类字典Vo")
public class ContactsUserRelationVo extends BaseVo<ContactsUserRelationVo> {

	@ApiModelProperty(value = "用户ID，为空表示公共字典，有值表示用户自定义分类")
	private Long userId;

	@ApiModelProperty(value = "关系标签，如：重要客户、潜在客户等")
	private String relationshipTag;

	@ApiModelProperty(value = "重要程度，1-普通，2-重要，3-非常重要")
	private Integer importance;

	@ApiModelProperty(value = "描述信息")
	private String description;

	@ApiModelProperty(value = "备注信息")
	private String remarks;

	@ApiModelProperty(value = "是否启用，0-禁用，1-启用")
	private Integer isEnabled;

	@ApiModelProperty(value = "关键字（用于模糊搜索）")
	private String keyword;

}

