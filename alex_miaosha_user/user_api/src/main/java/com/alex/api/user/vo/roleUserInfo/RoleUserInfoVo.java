package com.alex.api.user.vo.roleUserInfo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  用户角色信息表Vo
 * author:       majf
 * createDate:   2024-01-15 15:12:07
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "RoleUserInfoVo", description = "用户角色信息表Vo")
public class RoleUserInfoVo extends BaseVo<RoleUserInfoVo>{

    @ApiModelProperty(value = "角色id")
    private String roleId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "描述")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;
}
