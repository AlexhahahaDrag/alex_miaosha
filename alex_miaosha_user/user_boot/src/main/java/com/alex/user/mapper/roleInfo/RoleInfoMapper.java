package com.alex.user.mapper.roleInfo;

import com.alex.user.entity.roleInfo.RoleInfo;
import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  角色信息表 mapper
 * @author:       majf
 * @createDate:   2024-01-14 21:56:18
 * @version:      1.0.0
 */
@Mapper
public interface RoleInfoMapper extends BaseMapper<RoleInfo> {

    Page<RoleInfoVo> getPage(Page<RoleInfoVo> page, @Param("roleInfoVo") RoleInfoVo roleInfoVo);

    RoleInfoVo queryRoleInfo(@Param("id") String id);
}
