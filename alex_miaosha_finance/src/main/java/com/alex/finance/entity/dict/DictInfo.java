package com.alex.finance.entity.dict;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  字典表类
 * @author:       alex
 * @createDate: 2022-10-13 17:47:15
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_info")
@ApiModel(value = "DictInfo对象", description = "字典表")
public class DictInfo extends BaseEntity<DictInfo>{

    @ApiModelProperty(value = "名称")
    @TableField("type_code")
    private String typeCode;

    @ApiModelProperty(value = "类别")
    @TableField("type_name")
    private String typeName;

    @ApiModelProperty(value = "属于")
    @TableField("belong_to")
    private String belongTo;

    @ApiModelProperty(value = "是否有效")
    @TableField("is_valid")
    private String isValid;

}
