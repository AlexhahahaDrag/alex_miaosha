package com.alex.user.entity.orgInfo;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  机构表类
 * @author:       alex
 * @createDate: 2023-12-07 16:57:00
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_org_info")
@ApiModel(value = "OrgInfo对象", description = "机构表")
public class OrgInfo extends BaseEntity<OrgInfo>{

    @ApiModelProperty(value = "机构编码")
    @TableField("org_code")
    private String orgCode;

    @ApiModelProperty(value = "机构名称")
    @TableField("org_name")
    private String orgName;

    @ApiModelProperty(value = "机构简称")
    @TableField("org_short_name")
    private String orgShortName;

    @ApiModelProperty(value = "父级机构id")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty(value = "简介最多150字")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("`status`")
    private String status;

}
