package com.alex.web.service;

import com.alex.web.pojo.dto.AdminUserDTO;
import com.alex.web.pojo.dto.AdminUserPermissionDTO;
import com.alex.web.pojo.dto.PermissionMenuDTO;
import com.alex.web.pojo.request.AdminUserPasswordReq;
import com.alex.web.pojo.request.AdminUserReq;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Optional;

/**
 * @description:  管理员service
 * @author:       majf
 * @createDate:   2022/8/9 11:56
 * @version:      1.0.0
 */
public interface AdminUserService {

    /**
     * @param username
     * @description: 根据名称获取管理员信息
     * @author:      majf
     * @return:      java.util.Optional<com.alex.web.pojo.dto.AdminUserDTO>
    */
    Optional<AdminUserDTO> findByUserName(String username);

    /**
     * @param adminUserPasswordReq
     * @description: 更新密码
     * @author:      majf
     * @return:      java.lang.String
    */
    String updatePassword(AdminUserPasswordReq adminUserPasswordReq);

    /**
     * @param page
     * @param pageSize
     * @param search
     * @description: 获取管理员列表数据
     * @author:      majf
     * @return:      com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.alex.web.pojo.dto.AdminUserDTO>
    */
    Page<AdminUserDTO> findByAdminUsers(Integer page, Integer pageSize, String search);

    /**
     * @param id
     * @description: 禁用/启用
     * @author:      majf
     * @return:      void
    */
    void switchIsBan(Integer id);

    /**
     * @param adminUserReq
     * @description: 创建管理员
     * @author:      majf
     * @return:      boolean
    */
    void createAdminUser(AdminUserReq adminUserReq);

    /**
     * @param adminUserReq
     * @description: 更新管理员信息
     * @author:      majf
     * @return:      boolean
    */
    void updateAdminUser(AdminUserReq adminUserReq);
    /**
     * @param ids
     * @description: 批量删除
     * @author:      majf
     * @return:      void
    */
    void deletes(String ids);

    /**
     * @param adminUserPermissionDTO
     * @description: 修改管理员权限
     * @author:      majf
     * @return:      void
    */
    void patchAdminUserPermission(AdminUserPermissionDTO adminUserPermissionDTO);

    /**
     * @param username
     * @description: 根据用户查询菜单
     * @author:      majf
     * @return:      java.util.List<PermissionMenuDTO>
    */
    List<PermissionMenuDTO> findMenuByUsername(String username);
}
