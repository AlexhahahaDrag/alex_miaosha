package com.alex.user.rolePermissionInfo.mapper;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.vo.rolePermissionInfo.RolePermissionInfoVo;
import com.alex.user.rolePermissionInfo.entity.RolePermissionInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  角色权限信息表mapper
 * author:       majf
 * createDate:   2024-01-19 14:52:21
 * version:      1.0.0
 */
@Mapper
public interface RolePermissionInfoMapper extends BaseMapper<RolePermissionInfo> {

    @DataPermission(table = "t_role_permission_info")
    Page<RolePermissionInfoVo> getPage(Page<RolePermissionInfoVo> page, @Param("rolePermissionInfoVo") RolePermissionInfoVo rolePermissionInfoVo);

    List<RolePermissionInfoVo> getList(@Param("rolePermissionInfoVo") RolePermissionInfoVo rolePermissionInfoVo);

    RolePermissionInfoVo queryRolePermissionInfo(@Param("id") Long id);
}
