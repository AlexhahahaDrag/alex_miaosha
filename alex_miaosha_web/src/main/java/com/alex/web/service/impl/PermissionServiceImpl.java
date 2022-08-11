package com.alex.web.service.impl;

import com.alex.web.pojo.dto.PermissionDTO;
import com.alex.web.service.PermissionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:  权限服务实现类
 * @author:       majf
 * @createDate:   2022/8/11 15:58
 * @version:      1.0.0
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Override
    public Page<PermissionDTO> findPermissions(Long page, Long pageSize, String search) {
        return null;
    }

    @Override
    public void createPermission(PermissionDTO permissionDTO) {

    }

    @Override
    public void updatePermission(PermissionDTO permissionDTO) {

    }

    @Override
    public void deletes(String ids) {

    }

    @Override
    public List<PermissionDTO> findAll(String search) {
        return null;
    }
}
