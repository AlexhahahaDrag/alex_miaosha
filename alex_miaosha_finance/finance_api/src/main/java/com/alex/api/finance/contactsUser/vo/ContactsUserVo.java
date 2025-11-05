package com.alex.api.finance.contactsUser.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  联系人信息表Vo
 * author:       alex
 * createDate:   2025-11-03 10:01:28
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ContactsUserVo", description = "联系人信息表Vo")
public class ContactsUserVo extends BaseVo<ContactsUserVo> {

	@ApiModelProperty(value = "联系人姓名")
	private String name;

	@ApiModelProperty(value = "联系电话")
	private String phone;

	@ApiModelProperty(value = "关系类型")
	private String relationship;

	@ApiModelProperty(value = "电子邮箱")
	private String email;

	@ApiModelProperty(value = "联系地址")
	private String address;

	@ApiModelProperty(value = "备注信息")
	private String remarks;

	@ApiModelProperty(value = "是否是常用联系人")
	private Integer isFavorite;

	@ApiModelProperty(value = "事件类型，如：生日、婚礼、养老等")
	private String eventType;

	@ApiModelProperty(value = "关键字（用于模糊搜索）")
	private String keyword;

}
