package com.alex.web.service.impl;

import com.alex.web.mapper.RoleMapper;
import com.alex.web.pojo.dto.RoleDTO;
import com.alex.web.pojo.entity.Role;
import com.alex.web.service.RoleService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author:       majf
 * @createDate:   2022/8/11 16:07
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public Page<RoleDTO> findRoles(Long page, Long pageSize, String search) {
        Page<RoleDTO> pageInfo = new Page<>();
        return roleMapper.page(pageInfo, search);
    }

    @Override
    public void createRole(RoleDTO roleDTO) {
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        roleMapper.insert(role);
    }

    @Override
    public void updateRole(RoleDTO roleDTO) {
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        this.updateById(role);
    }

    @Override
    public void deletes(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return;
        }
        String[] idList = ids.split(",");
//        Arrays.stream(idList).map()
//        roleMapper.deleteBatchIds(idList);
    }

    @Override
    public List<RoleDTO> findAll(String search) {
        return null;
    }
}
