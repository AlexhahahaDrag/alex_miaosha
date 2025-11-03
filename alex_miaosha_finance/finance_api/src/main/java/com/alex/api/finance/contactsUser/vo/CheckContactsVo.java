package com.alex.api.finance.contactsUser.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CheckContactsVo", description = "联系人校验Vo")
public class CheckContactsVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer nameCount;

	private Integer phoneCount;
}
