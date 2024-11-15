package com.alex.user.service.rolePermissionInfo.impl;

import com.alex.api.user.vo.rolePermissionInfo.RolePermissionInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.rolePermissionInfo.RolePermissionInfo;
import com.alex.user.mapper.rolePermissionInfo.RolePermissionInfoMapper;
import com.alex.user.service.rolePermissionInfo.RolePermissionInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  角色权限信息表服务实现类
 * author:       majf
 * createDate:   2024-01-19 14:52:21
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RolePermissionInfoServiceImp extends ServiceImpl<RolePermissionInfoMapper, RolePermissionInfo> implements RolePermissionInfoService {

    private final RolePermissionInfoMapper rolePermissionInfoMapper;

    @Override
    public Page<RolePermissionInfoVo> getPage(Long pageNum, Long pageSize, RolePermissionInfoVo rolePermissionInfoVo) {
        Page<RolePermissionInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return rolePermissionInfoMapper.getPage(page, rolePermissionInfoVo);
    }

    @Override
    public List<RolePermissionInfoVo> getList(RolePermissionInfoVo rolePermissionInfoVo) {
        return rolePermissionInfoMapper.getList(rolePermissionInfoVo);
    }

    @Override
    public RolePermissionInfoVo queryRolePermissionInfo(Long id) {
        return rolePermissionInfoMapper.queryRolePermissionInfo(id);
    }

    @Override
    public Boolean addRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo) {
        RolePermissionInfo rolePermissionInfo = new RolePermissionInfo();
        BeanUtils.copyProperties(rolePermissionInfoVo, rolePermissionInfo);
        rolePermissionInfoMapper.insert(rolePermissionInfo);
        return true;
    }

    @Override
    public Boolean updateRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo) {
        RolePermissionInfo rolePermissionInfo = new RolePermissionInfo();
        BeanUtils.copyProperties(rolePermissionInfoVo, rolePermissionInfo);
        rolePermissionInfoMapper.updateById(rolePermissionInfo);
        return true;
    }

    @Override
    public Boolean deleteRolePermissionInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        rolePermissionInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
