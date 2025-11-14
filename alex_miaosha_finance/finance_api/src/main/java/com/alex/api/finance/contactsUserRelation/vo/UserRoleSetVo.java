package com.alex.api.finance.contactsUserRelation.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@ApiModel(value = "UserRoleSetVo", description = "用户角色设置vo")
public class UserRoleSetVo {

    @ApiModelProperty(name = "userId", value = "用户id")
    private Long userId;

    @ApiModelProperty(name = "roleCode", value = "角色编码")
    private String roleCode;
}
