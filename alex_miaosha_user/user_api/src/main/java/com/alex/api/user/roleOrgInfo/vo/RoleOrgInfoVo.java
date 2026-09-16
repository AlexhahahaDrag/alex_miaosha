package com.alex.api.user.roleOrgInfo.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  角色机构信息表Vo
 * author:       majf
 * createDate:   2026-08-19
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "RoleOrgInfoVo", description = "角色机构信息表Vo")
public class RoleOrgInfoVo extends BaseVo<RoleOrgInfoVo> {

    @ApiModelProperty(value = "角色id")
    private String roleId;

    @ApiModelProperty(value = "机构id")
    private String orgId;

    @ApiModelProperty(value = "描述")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;
}
