package com.alex.web.service;

import com.alex.web.pojo.dto.RoleDTO;
import com.alex.web.pojo.entity.Role;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @description:  service
 * @author:       majf
 * @createDate:   2022/8/9 11:56
 * @version:      1.0.0
 */
public interface RoleService extends IService<Role> {

    /**
     * @param page
     * @param pageSize
     * @param search
     * @description: 获取角色列表数据
     * @author:      majf
     * @return:      com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.alex.web.pojo.dto.RoleDTO>
    */
    Page<RoleDTO> findRoles(Long page, Long pageSize, String search);

    /**
     * @param roleDTO
     * @description: 创建角色
     * @author:      majf
     * @return:      boolean
    */
    void createRole(RoleDTO roleDTO);

    /**
     * @param roleDTO
     * @description: 更新角色信息
     * @author:      majf
     * @return:      boolean
    */
    void updateRole(RoleDTO roleDTO);
    /**
     * @param ids
     * @description: 批量删除
     * @author:      majf
     * @return:      void
    */
    void deletes(String ids);

    /**
     * @param search
     * @description: 查询所有
     * @author:      majf
     * @return:      java.util.List<com.alex.web.pojo.dto.RoleDTO>
     */
    List<RoleDTO> findAll(String search);
}
