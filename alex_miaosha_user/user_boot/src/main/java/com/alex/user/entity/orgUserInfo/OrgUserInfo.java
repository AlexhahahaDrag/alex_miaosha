package com.alex.user.entity.orgUserInfo;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * description:  用户公司信息表类
 * author:       majf
 * createDate: 2024-01-15 15:12:05
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_org_user_info")
@ApiModel(value = "OrgUserInfo对象", description = "用户公司信息表")
public class OrgUserInfo extends BaseEntity<OrgUserInfo>{

    @ApiModelProperty(value = "公司角色id")
    @TableField("org_id")
    private String orgId;

    @ApiModelProperty(value = "用户id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "描述")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("`status`")
    private String status;

}
