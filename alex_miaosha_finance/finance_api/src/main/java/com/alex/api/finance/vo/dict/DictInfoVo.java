package com.alex.api.finance.vo.dict;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  字典表Vo
 * @author:       alex
 * @createDate:   2022-10-13 17:47:15
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "DictInfoVo", description = "字典表Vo")
public class DictInfoVo extends BaseVo<DictInfoVo>{

    @ApiModelProperty(value = "名称")
    private String typeCode;

    @ApiModelProperty(value = "类别")
    private String typeName;

    @ApiModelProperty(value = "分类编码")
    private String belongTo;

    @ApiModelProperty(value = "分类")
    private String belongToName;

    @ApiModelProperty(value = "排序")
    private Integer orderBy;

    @ApiModelProperty(value = "是否有效")
    private String isValid;

    @ApiModelProperty("是否删除")
    private Integer isDelete;
}
