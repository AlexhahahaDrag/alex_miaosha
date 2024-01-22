package com.alex.user.service.permissionInfo;

import com.alex.api.user.vo.permissionInfo.PermissionInfoVo;
import com.alex.user.entity.permissionInfo.PermissionInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 权限信息表 服务类
 * author: majf
 * createDate: 2024-01-16 15:43:56
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PermissionInfoService extends IService<PermissionInfo> {

    Page<PermissionInfoVo> getPage(Long pageNum, Long pageSize, PermissionInfoVo permissionInfoVo);

    PermissionInfoVo queryPermissionInfo(Long id);

    PermissionInfoVo addPermissionInfo(PermissionInfoVo permissionInfoVo);

    PermissionInfoVo updatePermissionInfo(PermissionInfoVo permissionInfoVo);

    Boolean deletePermissionInfo(String ids);

    List<PermissionInfoVo> getList(PermissionInfoVo permissionInfoVo);
}
