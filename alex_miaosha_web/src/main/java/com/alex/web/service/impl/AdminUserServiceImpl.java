package com.alex.web.service.impl;

import com.alex.web.pojo.dto.AdminUserDTO;
import com.alex.web.pojo.dto.AdminUserPermissionDTO;
import com.alex.web.pojo.dto.PermissionMenuDTO;
import com.alex.web.pojo.request.AdminUserPasswordReq;
import com.alex.web.pojo.request.AdminUserReq;
import com.alex.web.service.AdminUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Optional;

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
    public Page<AdminUserDTO> findByAdminUsers(Integer page, Integer pageSize, String search) {
        return null;
    }

    @Override
    public void switchIsBan(Integer id) {

    }

    @Override
    public void createAdminUser(AdminUserReq adminUserReq) {

    }

    @Override
    public void updateAdminUser(AdminUserReq adminUserReq) {

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
