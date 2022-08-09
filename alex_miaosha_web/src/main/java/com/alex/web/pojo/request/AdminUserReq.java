package com.alex.web.pojo.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import javax.validation.constraints.NotBlank;

/**
 * @description:  用户表类
 * @author:       majf
 * @createDate: 2022-08-09 14:12:41
 * @version:      1.0.0
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "AdminUserReq", description = "管理员req")
public class AdminUserReq {

    @NotBlank(message = "账号不能为空", groups = {Update.class})
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @NotBlank(message = "账号不能为空", groups = {Insert.class, Update.class})
    @ApiModelProperty(value = "账号")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String name;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @NotBlank(message = "角色不能为空", groups = {Insert.class, Update.class})
    @ApiModelProperty(value = "角色多个角色可以使用夺标关联或者本字段用逗号分隔")
    private String role;

    @NotBlank(message = "密码不能为空", groups = {Insert.class, Update.class})
    @ApiModelProperty(value = "用户密码")
    private String password;

    @NotBlank(message = "是否禁用不能为空", groups = {Insert.class, Update.class})
    @ApiModelProperty(value = "是否禁用")
    private Boolean isBan;
}
