package com.alex.web.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @description:
 * @author:       majf
 * @createDate:   2022/8/9 15:07
 * @version:      1.0.0
 */
@Data
@ApiModel(value = "管理员对象DTO", description = "管理员表DTO")
public class AdminUserDTO implements UserDetails {

    private final long serialId = 1l;

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "账号")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String name;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "角色多个角色可以使用夺标关联或者本字段用逗号分隔")
    private String role;

    @JsonIgnore
    @ApiModelProperty(value = "用户密码")
    private String password;

    @ApiModelProperty(value = "是否禁用")
    private Boolean isBan;

    @ApiModelProperty(value = "角色列表")
    private List<RoleDTO> roles;

    @ApiModelProperty(value = "权限列表")
    private List<PermissionDTO> permissions = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        permissions.forEach(permissionDTO -> authorities.add(new SimpleGrantedAuthority(permissionDTO.getPermission())));
        return authorities;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return !this.isBan;
    }
}
