package com.alex.user.roleOrgInfo.service;

import com.alex.user.roleOrgInfo.entity.RoleOrgInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * description:  角色机构信息表服务接口
 * author:       majf
 * createDate:   2026-08-19
 * version:      1.0.0
 */
public interface RoleOrgInfoService extends IService<RoleOrgInfo> {

    Boolean assignOrgs(Long roleId, List<Long> orgIds);

    List<RoleOrgInfo> listValidByRoleId(Long roleId);
}
