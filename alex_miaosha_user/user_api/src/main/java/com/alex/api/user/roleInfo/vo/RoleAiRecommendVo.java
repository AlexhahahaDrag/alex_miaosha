package com.alex.api.user.roleInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 岗位角色权限 AI 推荐响应
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "RoleAiRecommendVo", description = "岗位角色权限 AI 推荐响应")
public class RoleAiRecommendVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "推荐勾选的菜单ID列表(字符串类型)")
    private List<String> recommendedMenuIds;

    @ApiModelProperty(value = "推荐勾选的菜单名称列表")
    private List<String> recommendedMenuNames;

    @ApiModelProperty(value = "推荐按钮权限编码清单")
    private List<String> recommendedPermissionCodes;

    @ApiModelProperty(value = "AI 岗位职责与权限匹配分析理由")
    private String reasoning;
}
