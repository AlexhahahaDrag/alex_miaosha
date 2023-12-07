package com.alex.api.user.vo.tOrgInfo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  机构表Vo
 * @author:       alex
 * @createDate:   2023-12-06 17:53:15
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "OrgInfoVo", description = "机构表Vo")
public class OrgInfoVo extends BaseVo<OrgInfoVo>{

    @ApiModelProperty(value = "机构编码")
    private String orgCode;

    @ApiModelProperty(value = "机构名称")
    private String orgName;

    @ApiModelProperty(value = "机构简称")
    private String orgShortName;

    @ApiModelProperty(value = "父级机构id")
    private Long pId;

    @ApiModelProperty(value = "简介最多150字")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;

}
