package com.alex.user.roleUserInfo.mapper;

import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  用户角色信息�?mapper
 * author:       majf
 * createDate:   2024-01-15 15:12:07
 * version:      1.0.0
 */
@Mapper
public interface RoleUserInfoMapper extends BaseMapper<RoleUserInfo> {

    Page<RoleUserInfoVo> getPage(Page<RoleUserInfoVo> page, @Param("roleUserInfoVo") RoleUserInfoVo roleUserInfoVo);

    RoleUserInfoVo queryRoleUserInfo(@Param("id") String id);

    List<RoleInfoVo> getRoleInfoList(@Param("userId") Long userId,
                                     @Param("hasPermission")boolean hasPermission);
}
