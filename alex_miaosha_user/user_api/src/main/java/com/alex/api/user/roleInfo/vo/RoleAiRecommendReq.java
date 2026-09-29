package com.alex.api.user.roleInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 岗位角色权限 AI 推荐请求
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "RoleAiRecommendReq", description = "岗位角色权限 AI 推荐请求")
public class RoleAiRecommendReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色名称", required = true)
    private String roleName;

    @ApiModelProperty(value = "角色编码")
    private String roleCode;

    @ApiModelProperty(value = "角色职责描述/工作内容", required = true)
    private String description;
}
