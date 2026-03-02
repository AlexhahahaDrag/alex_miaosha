package com.alex.user.permissionInfo.mapper;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.user.permissionInfo.entity.PermissionInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  权限信息表mapper
 * author:       majf
 * createDate:   2024-01-16 15:43:56
 * version:      1.0.0
 */
@Mapper
public interface PermissionInfoMapper extends BaseMapper<PermissionInfo> {

    @DataPermission(table = "t_permission_info")
    Page<PermissionInfoVo> getPage(Page<PermissionInfoVo> page, @Param("permissionInfoVo") PermissionInfoVo permissionInfoVo);

    PermissionInfoVo queryPermissionInfo(@Param("id") Long id);

    List<PermissionInfoVo> getList(@Param("permissionInfoVo") PermissionInfoVo permissionInfoVo);
}
