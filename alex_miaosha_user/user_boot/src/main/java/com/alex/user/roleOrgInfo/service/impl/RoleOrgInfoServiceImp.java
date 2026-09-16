package com.alex.user.roleOrgInfo.service.impl;

import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.user.roleOrgInfo.entity.RoleOrgInfo;
import com.alex.user.roleOrgInfo.mapper.RoleOrgInfoMapper;
import com.alex.user.roleOrgInfo.service.RoleOrgInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * description:  角色机构信息表服务实现类
 * author:       majf
 * createDate:   2026-08-19
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleOrgInfoServiceImp extends ServiceImpl<RoleOrgInfoMapper, RoleOrgInfo> implements RoleOrgInfoService {

    private final RoleOrgInfoMapper roleOrgInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignOrgs(Long roleId, List<Long> orgIds) {
        if (roleId == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色机构分配参数错误:");
        }
        List<RoleOrgInfo> active = list(Wrappers.<RoleOrgInfo>lambdaQuery()
                .eq(RoleOrgInfo::getRoleId, String.valueOf(roleId))
                .eq(RoleOrgInfo::getStatus, SysConf.VALID_STATUS));
        for (RoleOrgInfo row : active) {
            row.setStatus(SysConf.INVALID_STATUS);
            if (!updateById(row)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色机构旧关系失效失败:");
            }
        }
        if (orgIds == null || orgIds.isEmpty()) {
            return true;
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long orgId : orgIds) {
            if (orgId != null) {
                unique.add(orgId);
            }
        }
        if (unique.isEmpty()) {
            return true;
        }
        List<RoleOrgInfo> batch = new ArrayList<>();
        for (Long orgId : unique) {
            RoleOrgInfo row = new RoleOrgInfo();
            row.setRoleId(String.valueOf(roleId));
            row.setOrgId(String.valueOf(orgId));
            row.setStatus(SysConf.VALID_STATUS);
            batch.add(row);
        }
        if (!saveBatch(batch)) {
            throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色机构新关系保存失败:");
        }
        return true;
    }

    @Override
    public List<RoleOrgInfo> listValidByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        List<RoleOrgInfo> rows = roleOrgInfoMapper.listValidByRoleId(String.valueOf(roleId));
        return rows == null ? Collections.emptyList() : rows;
    }
}
