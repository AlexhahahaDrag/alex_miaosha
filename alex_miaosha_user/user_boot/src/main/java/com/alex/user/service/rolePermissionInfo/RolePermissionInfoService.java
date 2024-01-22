package com.alex.user.service.rolePermissionInfo;

import com.alex.api.user.vo.rolePermissionInfo.RolePermissionInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.user.entity.rolePermissionInfo.RolePermissionInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 角色权限信息表 服务类
 * author: majf
 * createDate: 2024-01-19 14:52:21
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface RolePermissionInfoService extends IService<RolePermissionInfo> {

    Page<RolePermissionInfoVo> getPage(Long pageNum, Long pageSize, RolePermissionInfoVo rolePermissionInfoVo);

    RolePermissionInfoVo queryRolePermissionInfo(Long id);

    Boolean addRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo);

    Boolean updateRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo);

    Boolean deleteRolePermissionInfo(String ids);
}
