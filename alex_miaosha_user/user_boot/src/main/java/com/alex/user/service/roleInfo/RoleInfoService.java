package com.alex.user.service.roleInfo;

import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.user.entity.roleInfo.RoleInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 角色信息表 服务类
 * @author: majf
 * @createDate: 2024-01-14 21:56:18
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface RoleInfoService extends IService<RoleInfo> {

    Page<RoleInfoVo> getPage(Long pageNum, Long pageSize, RoleInfoVo roleInfoVo);

    RoleInfoVo queryRoleInfo(String id);

    Boolean addRoleInfo(RoleInfoVo roleInfoVo);

    Boolean updateRoleInfo(RoleInfoVo roleInfoVo);

    Boolean deleteRoleInfo(String ids);
}
