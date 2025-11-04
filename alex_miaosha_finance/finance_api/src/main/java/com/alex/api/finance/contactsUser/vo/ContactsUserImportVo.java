package com.alex.api.finance.contactsUser.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  联系人导入VO（用于 Excel 导入，支持中文列名）
 * author:       alex
 * createDate:   2025-11-04 10:01:28
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ContactsUserImportVo", description = "联系人导入VO")
public class ContactsUserImportVo {

	@Excel(name = "姓名")
	@ApiModelProperty(value = "联系人姓名")
	private String name;

	@Excel(name = "电话")
	@ApiModelProperty(value = "联系电话")
	private String phone;

	@Excel(name = "关系")
	@ApiModelProperty(value = "关系类型")
	private String relationship;

	@Excel(name = "邮箱")
	@ApiModelProperty(value = "电子邮箱")
	private String email;

	@Excel(name = "地址")
	@ApiModelProperty(value = "联系地址")
	private String address;

	@Excel(name = "备注")
	@ApiModelProperty(value = "备注信息")
	private String remarks;
}
