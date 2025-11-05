package com.alex.finance.contactsUser.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  联系人信息表
 * author:       alex
 * createDate:   2025-11-03 10:01:28
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_contacts_user")
@ApiModel(value = "ContactsUser对象", description = "联系人信息表")
public class ContactsUser extends BaseEntity<ContactsUser> {

	@ApiModelProperty(value = "联系人姓名")
	@TableField("name")
	private String name;

	@ApiModelProperty(value = "联系电话")
	@TableField("phone")
	private String phone;

	@ApiModelProperty(value = "关系类型，字典(contacts_relationship)")
	@TableField("relationship")
	private String relationship;

	@ApiModelProperty(value = "电子邮箱")
	@TableField("email")
	private String email;

	@ApiModelProperty(value = "联系地址")
	@TableField("address")
	private String address;

	@ApiModelProperty(value = "备注信息")
	@TableField("remarks")
	private String remarks;

	@ApiModelProperty(value = "是否是常用联系人，0-否，1-是")
	@TableField("is_favorite")
	private Integer isFavorite;

	@ApiModelProperty(value = "事件类型，字典(gift_event)，如：生日、婚礼、养老等")
	@TableField("event_type")
	private String eventType;

}
