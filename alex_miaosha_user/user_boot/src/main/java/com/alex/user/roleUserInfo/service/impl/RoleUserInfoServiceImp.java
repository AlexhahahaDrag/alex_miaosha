package com.alex.user.roleUserInfo.service.impl;

import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
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
 * description:  用户角色信息表服务实现类
 * author:       majf
 * createDate:   2024-01-15 15:12:07
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleUserInfoServiceImp extends ServiceImpl<RoleUserInfoMapper, RoleUserInfo> implements RoleUserInfoService {

    private final RoleUserInfoMapper roleUserInfoMapper;

    // RBAC-BE-RELATION-002: assign 成功后主动失效受影响用户的 permission_context 缓存
    private final PermissionContextCacheService permissionContextCacheService;

    @Override
    public Page<RoleUserInfoVo> getPage(Long pageNum, Long pageSize, RoleUserInfoVo roleUserInfoVo) {
        Page<RoleUserInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return roleUserInfoMapper.getPage(page, roleUserInfoVo);
    }

    @Override
    public RoleUserInfoVo queryRoleUserInfo(String id) {
        return roleUserInfoMapper.queryRoleUserInfo(id);
    }

    @Override
    public Boolean addRoleUserInfo(RoleUserInfoVo roleUserInfoVo) {
        RoleUserInfo roleUserInfo = new RoleUserInfo();
        BeanUtils.copyProperties(roleUserInfoVo, roleUserInfo);
        roleUserInfoMapper.insert(roleUserInfo);
        return true;
    }

    @Override
    public Boolean updateRoleUserInfo(RoleUserInfoVo roleUserInfoVo) {
        RoleUserInfo roleUserInfo = new RoleUserInfo();
        BeanUtils.copyProperties(roleUserInfoVo, roleUserInfo);
        roleUserInfoMapper.updateById(roleUserInfo);
        return true;
    }

    @Override
    public Boolean deleteRoleUserInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        roleUserInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "用户角色分配参数错误:");
        }
        List<RoleUserInfo> activeAssignments = list(Wrappers.<RoleUserInfo>lambdaQuery()
                .eq(RoleUserInfo::getUserId, String.valueOf(userId))
                .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
        for (RoleUserInfo roleUserInfo : activeAssignments) {
            roleUserInfo.setStatus(SysConf.INVALID_STATUS);
            if (!updateById(roleUserInfo)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "用户角色旧关系失效失败:");
            }
        }
        if (roleIds == null || roleIds.isEmpty()) {
            // RBAC-BE-RELATION-002: 角色被清空也是一种变更，同样要失效缓存
            permissionContextCacheService.invalidate(userId);
            return true;
        }
        Set<Long> uniqueRoleIds = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            if (roleId != null) {
                uniqueRoleIds.add(roleId);
            }
        }
        if (uniqueRoleIds.isEmpty()) {
            permissionContextCacheService.invalidate(userId);
            return true;
        }
        List<RoleUserInfo> newAssignments = new ArrayList<>();
        for (Long roleId : uniqueRoleIds) {
            RoleUserInfo roleUserInfo = new RoleUserInfo();
            roleUserInfo.setUserId(String.valueOf(userId));
            roleUserInfo.setRoleId(String.valueOf(roleId));
            roleUserInfo.setStatus(SysConf.VALID_STATUS);
            newAssignments.add(roleUserInfo);
        }
        if (!saveBatch(newAssignments)) {
            throw new SystemException(ResultEnum.SYSTEM_ERROR, "用户角色新关系保存失败:");
        }
        // RBAC-BE-RELATION-002: 改角色成功后主动失效该用户的 permission_context 缓存
        permissionContextCacheService.invalidate(userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignUsersToRole(Long roleId, List<Long> userIds) {
        if (roleId == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色用户分配参数错误:");
        }
        List<RoleUserInfo> activeAssignments = list(Wrappers.<RoleUserInfo>lambdaQuery()
                .eq(RoleUserInfo::getRoleId, String.valueOf(roleId))
                .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
        Set<Long> previousUserIds = new LinkedHashSet<>();
        for (RoleUserInfo roleUserInfo : activeAssignments) {
            String previousUserId = roleUserInfo.getUserId();
            if (previousUserId != null && !previousUserId.isEmpty()) {
                previousUserIds.add(Long.valueOf(previousUserId));
            }
            roleUserInfo.setStatus(SysConf.INVALID_STATUS);
            if (!updateById(roleUserInfo)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色用户旧关系失效失败:");
            }
        }
        if (userIds == null || userIds.isEmpty()) {
            if (!previousUserIds.isEmpty()) {
                permissionContextCacheService.invalidateAll(previousUserIds);
            }
            return true;
        }
        Set<Long> uniqueUserIds = new LinkedHashSet<>();
        for (Long userId : userIds) {
            if (userId != null) {
                uniqueUserIds.add(userId);
            }
        }
        if (uniqueUserIds.isEmpty()) {
            if (!previousUserIds.isEmpty()) {
                permissionContextCacheService.invalidateAll(previousUserIds);
            }
            return true;
        }
        List<RoleUserInfo> newAssignments = new ArrayList<>();
        for (Long userId : uniqueUserIds) {
            RoleUserInfo roleUserInfo = new RoleUserInfo();
            roleUserInfo.setUserId(String.valueOf(userId));
            roleUserInfo.setRoleId(String.valueOf(roleId));
            roleUserInfo.setStatus(SysConf.VALID_STATUS);
            newAssignments.add(roleUserInfo);
        }
        if (!saveBatch(newAssignments)) {
            throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色用户新关系保存失败:");
        }
        // RBAC-BE-RELATION-002/SCOPE-004: 失效被移除与被分配用户的 permission_context 缓存
        Set<Long> affectedUserIds = new LinkedHashSet<>(previousUserIds);
        affectedUserIds.addAll(uniqueUserIds);
        permissionContextCacheService.invalidateAll(affectedUserIds);
        return true;
    }

    @Override
    public List<RoleInfoVo> getRoleInfoList(Long userId, boolean hasPermission) {
        return roleUserInfoMapper.getRoleInfoList(userId, hasPermission);
    }
}
