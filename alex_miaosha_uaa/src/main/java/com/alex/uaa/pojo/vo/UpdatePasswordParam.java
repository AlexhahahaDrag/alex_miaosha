package com.alex.uaa.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 修改密码参数
 * @author:       majf
 * @createDate:   2022/8/8 15:54
 * @version:      1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "UpdatePasswordParam", description = "修改密码参数")
public class UpdatePasswordParam {

    @ApiModelProperty(value = "旧密码", name = "oldPassword")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", name = "newPassword")
    private String newPassword;
}
