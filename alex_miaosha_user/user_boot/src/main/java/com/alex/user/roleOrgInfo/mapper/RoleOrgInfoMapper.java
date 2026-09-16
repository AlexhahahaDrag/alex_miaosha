package com.alex.user.roleOrgInfo.mapper;

import com.alex.user.roleOrgInfo.entity.RoleOrgInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  角色机构信息表 mapper
 * author:       majf
 * createDate:   2026-08-19
 * version:      1.0.0
 */
@Mapper
public interface RoleOrgInfoMapper extends BaseMapper<RoleOrgInfo> {

    List<RoleOrgInfo> listValidByRoleId(@Param("roleId") String roleId);

    List<RoleOrgInfo> listValidByOrgIds(@Param("orgIds") List<String> orgIds);

}
