package com.alex.user.rolePermissionInfo.service.impl;

import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.rolePermissionInfo.entity.RolePermissionInfo;
import com.alex.user.rolePermissionInfo.mapper.RolePermissionInfoMapper;
import com.alex.user.rolePermissionInfo.service.RolePermissionInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        rolePermissionInfoMapper.deleteByIds(idArr);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色权限分配参数错误:");
        }
        List<RolePermissionInfo> active = list(Wrappers.<RolePermissionInfo>lambdaQuery()
                .eq(RolePermissionInfo::getRoleId, String.valueOf(roleId))
                .eq(RolePermissionInfo::getStatus, SysConf.VALID_STATUS));
        for (RolePermissionInfo row : active) {
            row.setStatus(SysConf.INVALID_STATUS);
            if (!updateById(row)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色权限旧关系失效失败:");
            }
        }
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long pid : permissionIds) {
            if (pid != null) {
                unique.add(pid);
            }
        }
        if (unique.isEmpty()) {
            return true;
        }
        List<RolePermissionInfo> batch = new ArrayList<>();
        for (Long pid : unique) {
            RolePermissionInfo row = new RolePermissionInfo();
            row.setRoleId(String.valueOf(roleId));
            row.setPermissionId(String.valueOf(pid));
            row.setStatus(SysConf.VALID_STATUS);
            batch.add(row);
        }
        if (!saveBatch(batch)) {
            throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色权限新关系保存失败:");
        }
        return true;
    }
}
