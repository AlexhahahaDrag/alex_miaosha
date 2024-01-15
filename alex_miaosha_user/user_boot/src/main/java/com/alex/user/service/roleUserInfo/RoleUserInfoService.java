package com.alex.user.service.roleUserInfo;

import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.api.user.vo.roleUserInfo.RoleUserInfoVo;
import com.alex.user.entity.roleUserInfo.RoleUserInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户角色信息表 服务类
 * author: majf
 * createDate: 2024-01-15 15:12:07
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface RoleUserInfoService extends IService<RoleUserInfo> {

    Page<RoleUserInfoVo> getPage(Long pageNum, Long pageSize, RoleUserInfoVo roleUserInfoVo);

    RoleUserInfoVo queryRoleUserInfo(String id);

    Boolean addRoleUserInfo(RoleUserInfoVo roleUserInfoVo);

    Boolean updateRoleUserInfo(RoleUserInfoVo roleUserInfoVo);

    Boolean deleteRoleUserInfo(String ids);

    List<RoleInfoVo> getRoleInfoList(Long userId);
}
