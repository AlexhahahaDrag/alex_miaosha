package com.alex.api.user.vo.orgUserInfo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  用户公司信息表Vo
 * author:       majf
 * createDate:   2024-01-15 15:12:05
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "OrgUserInfoVo", description = "用户公司信息表Vo")
public class OrgUserInfoVo extends BaseVo<OrgUserInfoVo>{

    @ApiModelProperty(value = "公司角色id")
    private String orgId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "描述")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;

}
