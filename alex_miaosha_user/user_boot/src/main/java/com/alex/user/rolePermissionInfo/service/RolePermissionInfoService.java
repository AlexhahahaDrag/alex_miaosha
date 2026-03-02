package com.alex.user.rolePermissionInfo.service;

import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.user.rolePermissionInfo.entity.RolePermissionInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色权限信息表服务接口
 * author: majf
 * createDate: 2024-01-19 14:52:21
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface RolePermissionInfoService extends IService<RolePermissionInfo> {

    Page<RolePermissionInfoVo> getPage(Long pageNum, Long pageSize, RolePermissionInfoVo rolePermissionInfoVo);

    List<RolePermissionInfoVo> getList(RolePermissionInfoVo rolePermissionInfoVo);

    RolePermissionInfoVo queryRolePermissionInfo(Long id);

    Boolean addRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo);

    Boolean updateRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo);

    Boolean deleteRolePermissionInfo(String ids);
}
