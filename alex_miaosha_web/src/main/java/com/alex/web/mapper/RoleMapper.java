package com.alex.web.mapper;

import com.alex.web.pojo.dto.RoleDTO;
import com.alex.web.pojo.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface RoleMapper extends BaseMapper<Role> {

    Page<RoleDTO> page(Page page, @Param("search") String search);
}
