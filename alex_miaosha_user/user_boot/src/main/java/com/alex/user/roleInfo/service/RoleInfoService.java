package com.alex.user.roleInfo.service;

import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.user.roleInfo.entity.RoleInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色信息表服务接口
 * author: majf
 * createDate: 2024-01-14 21:56:18
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface RoleInfoService extends IService<RoleInfo> {

    Page<RoleInfoVo> getPage(Long pageNum, Long pageSize, RoleInfoVo roleInfoVo);

    RoleInfoVo queryRoleInfo(String id);

    String addRoleInfo(RoleInfoVo roleInfoVo);

    Boolean updateRoleInfo(RoleInfoVo roleInfoVo);

    Boolean deleteRoleInfo(String ids);

    Boolean assignUsers(Long roleId, List<Long> userIds);

    Boolean assignPermissions(Long roleId, List<Long> permissionIds);
}
