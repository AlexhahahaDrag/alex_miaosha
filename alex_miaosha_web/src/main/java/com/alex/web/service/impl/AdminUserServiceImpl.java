package com.alex.web.service.impl;

import com.alex.web.pojo.dto.AdminUserDTO;
import com.alex.web.pojo.dto.AdminUserPermissionDTO;
import com.alex.web.pojo.dto.PermissionMenuDTO;
import com.alex.web.pojo.request.AdminUserPasswordReq;
import com.alex.web.service.AdminUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @description:  管理员服务实现类
 * @author:       majf
 * @createDate:   2022/8/11 15:57
 * @version:      1.0.0
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Override
    public Optional<AdminUserDTO> findByUserName(String username) {
        return Optional.empty();
    }

    @Override
    public String updatePassword(AdminUserPasswordReq adminUserPasswordReq) {
        return null;
    }

    @Override
    public Page<AdminUserDTO> findAdminUsers(Long page, Long pageSize, String search) {
        return null;
    }

    @Override
    public void switchIsBan(Long id) {

    }

    @Override
    public void createAdminUser(AdminUserDTO adminUserDTO) {

    }

    @Override
    public void updateAdminUser(AdminUserDTO adminUserDTO) {

    }

    @Override
    public void deletes(String ids) {

    }

    @Override
    public void patchAdminUserPermission(AdminUserPermissionDTO adminUserPermissionDTO) {

    }

    @Override
    public List<PermissionMenuDTO> findMenuByUsername(String username) {
        return null;
    }
}
