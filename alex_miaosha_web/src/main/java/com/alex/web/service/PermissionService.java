package com.alex.web.service;

import com.alex.web.pojo.dto.PermissionDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * description:  权限服务类
 * author:       majf
 * createDate:   2022/8/11 15:41
 * version:      1.0.0
 */
public interface PermissionService {

    /**
     * @param page
     * @param pageSize
     * @param search
     * description: 查询权限列表
     * author:      majf
     * @return:      com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.alex.web.pojo.dto.PermissionDTO>
    */
    Page<PermissionDTO> findPermissions(Long page, Long pageSize, String search);

    /**
     * @param permissionDTO
     * description: 创建权限
     * author:      majf
     * @return:      void
    */
    void createPermission(PermissionDTO permissionDTO);

    /**
     * @param permissionDTO
     * description: 更新权限
     * author:      majf
     * @return:      void
    */
    void updatePermission(PermissionDTO permissionDTO);

    /**
     * @param ids
     * description: 批量删除
     * author:      majf
     * @return:      void
    */
    void deletes(String ids);

    /**
     * @param search
     * description: 查询所有
     * author:      majf
     * @return:      java.util.List<com.alex.web.pojo.dto.PermissionDTO>
    */
    List<PermissionDTO> findAll(String search);
}
