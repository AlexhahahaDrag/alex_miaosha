package com.alex.user.permissionInfo.service.impl;

import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.permissionInfo.entity.PermissionInfo;
import com.alex.user.permissionInfo.mapper.PermissionInfoMapper;
import com.alex.user.permissionInfo.service.PermissionInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.rolePermissionInfo.entity.RolePermissionInfo;
import com.alex.user.rolePermissionInfo.service.RolePermissionInfoService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * description: 权限信息表服务实现类
 * author: majf
 * createDate: 2024-01-16 15:43:56
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PermissionInfoServiceImp extends ServiceImpl<PermissionInfoMapper, PermissionInfo>
        implements PermissionInfoService {

    private final PermissionInfoMapper permissionInfoMapper;

    private final UserUtils userUtils;

    private final RolePermissionInfoService rolePermissionInfoService;

    private final RoleUserInfoService roleUserInfoService;

    // RBAC-BE-PERM-002: 删除权限点级联清理关系后，失效受影响用户的 permission_context 缓存
    private final PermissionContextCacheService permissionContextCacheService;

    @Override
    public Page<PermissionInfoVo> getPage(Long pageNum, Long pageSize, PermissionInfoVo permissionInfoVo) {
        Page<PermissionInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return permissionInfoMapper.getPage(page, permissionInfoVo);
    }

    @Override
    public PermissionInfoVo queryPermissionInfo(Long id) {
        PermissionInfoVo permissionInfoVo = permissionInfoMapper.queryPermissionInfo(id);
        // 挂了 @DataPermission 后，越权或不存在的权限 id 查询结果为 null。
        // 与 role/org/user 详情查询保持一致的空安全语义：直接返回 null，不 NPE。
        if (permissionInfoVo == null) {
            return null;
        }
        return permissionInfoVo;
    }

    @Override
    public PermissionInfoVo addPermissionInfo(PermissionInfoVo permissionInfoVo) {
        assertPermissionCodeUnique(permissionInfoVo == null ? null : permissionInfoVo.getPermissionCode(), null);
        PermissionInfo permissionInfo = new PermissionInfo();
        BeanUtils.copyProperties(permissionInfoVo, permissionInfo);
        permissionInfoMapper.insert(permissionInfo);
        permissionInfoVo.setId(permissionInfo.getId());
        return permissionInfoVo;
    }

    @Override
    public PermissionInfoVo updatePermissionInfo(PermissionInfoVo permissionInfoVo) {
        // Ownership first, then uniqueness (mirrors RoleInfoServiceImp).
        assertPermissionAccessible(permissionInfoVo == null ? null : permissionInfoVo.getId());
        assertPermissionCodeUnique(permissionInfoVo == null ? null : permissionInfoVo.getPermissionCode(),
                permissionInfoVo == null ? null : permissionInfoVo.getId());
        PermissionInfo permissionInfo = new PermissionInfo();
        BeanUtils.copyProperties(permissionInfoVo, permissionInfo);
        permissionInfoMapper.updateById(permissionInfo);
        permissionInfoVo.setId(permissionInfo.getId());
        return permissionInfoVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePermissionInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !StringUtils.isEmpty(id))
                .collect(Collectors.toList());
        if (idArr.isEmpty()) {
            return true;
        }
        // Ownership first: deny out-of-scope permissions before cascade.
        for (String permissionId : idArr) {
            assertPermissionAccessible(Long.valueOf(permissionId));
        }
        // RBAC-BE-PERM-002: cascade-invalidate role-permission in the same TX as delete.
        List<RolePermissionInfo> activeBindings = rolePermissionInfoService.list(
                Wrappers.<RolePermissionInfo>lambdaQuery()
                        .in(RolePermissionInfo::getPermissionId, idArr)
                        .eq(RolePermissionInfo::getStatus, SysConf.VALID_STATUS));
        Set<String> affectedRoleIds = new HashSet<>();
        for (RolePermissionInfo row : activeBindings) {
            row.setStatus(SysConf.INVALID_STATUS);
            if (!rolePermissionInfoService.updateById(row)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色权限旧关系失效失败:");
            }
            if (row.getRoleId() != null) {
                affectedRoleIds.add(row.getRoleId());
            }
        }
        // permission_context clear for users on affected roles: route through shared helper.
        Set<String> affectedUserIds = new HashSet<>();
        if (!affectedRoleIds.isEmpty()) {
            List<RoleUserInfo> boundUsers = roleUserInfoService.list(
                    Wrappers.<RoleUserInfo>lambdaQuery()
                            .in(RoleUserInfo::getRoleId, affectedRoleIds)
                            .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
            for (RoleUserInfo ru : boundUsers) {
                if (ru.getUserId() != null) {
                    affectedUserIds.add(ru.getUserId());
                }
            }
        }
        permissionContextCacheService.invalidateAll(toLongUserIds(affectedUserIds));
        permissionInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    /**
     * userId 字段在关系表里以 String 存储（来自 Long 主键序列化），转换失败按理不应发生；
     * 这里容错跳过而不是抛异常，避免缓存清理这一辅助逻辑影响主删除流程。
     */
    private static Set<Long> toLongUserIds(Collection<String> userIds) {
        Set<Long> result = new HashSet<>();
        for (String userId : userIds) {
            if (userId == null) {
                continue;
            }
            try {
                result.add(Long.valueOf(userId));
            } catch (NumberFormatException ignored) {
                // 不应出现：userId 应始终来自 Long 主键序列化
            }
        }
        return result;
    }

    @Override
    public List<PermissionInfoVo> getList(PermissionInfoVo permissionInfoVo) {
        List<PermissionInfoVo> list = permissionInfoMapper.getList(permissionInfoVo);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<PermissionInfoVo>> menuMap = list.stream()
                .filter(item -> item.getParentId() != null)
                .collect(Collectors.groupingBy(PermissionInfoVo::getParentId));
        return list.stream().filter(item -> item.getParentId() == null)
                .peek(item -> item.setChildren(getChildren(item.getId(), menuMap)))
                .toList();
    }

    /**
     * RBAC-BE-PERM-003: permissionCode must be non-empty and unique table-wide (update excludes self).
     */
    private void assertPermissionCodeUnique(String permissionCode, Long excludeId) {
        if (StringUtils.isEmpty(permissionCode)) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "权限编码不能为空");
        }
        long duplicateCount = permissionInfoMapper.selectCount(Wrappers.<PermissionInfo>lambdaQuery()
                .eq(PermissionInfo::getPermissionCode, permissionCode)
                .ne(excludeId != null, PermissionInfo::getId, excludeId));
        if (duplicateCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "权限编码已存在");
        }
    }

    /**
     * Write-path ownership guard: non-super users must pass a scoped queryPermissionInfo
     * before update/delete. Null means outside data scope — never silent success.
     */
    private void assertPermissionAccessible(Long id) {
        if (id == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "权限ID不能为空");
        }
        TUserVo loginUser = userUtils.getLoginUser();
        if (loginUser == null) {
            // 登录上下文不可用时必须 fail-closed，不能默认放行。
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问：登录上下文不可用");
        }
        if (isSuperAdminLogin(loginUser)) {
            return;
        }
        PermissionInfoVo visible = permissionInfoMapper.queryPermissionInfo(id);
        if (visible == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问其他机构的权限");
        }
    }

    private static boolean isSuperAdminLogin(TUserVo loginUser) {
        if (loginUser == null) {
            return false;
        }
        UserPermissionContextVo context = loginUser.getPermissionContext();
        if (context != null && Boolean.TRUE.equals(context.getSuperAdmin())) {
            return true;
        }
        List<RoleInfoVo> roles = loginUser.getRoleInfoVoList();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (RoleInfoVo role : roles) {
            if (role != null && RbacRoleCodes.SUPER.equals(role.getRoleCode())) {
                return true;
            }
        }
        return false;
    }

    public List<PermissionInfoVo> getChildren(Long pId, Map<Long, List<PermissionInfoVo>> menuMap) {
        if (pId == null || menuMap == null || menuMap.get(pId) == null || menuMap.get(pId).isEmpty()) {
            return null;
        }
        List<PermissionInfoVo> children = menuMap.get(pId);
        children.forEach(item -> item.setChildren(getChildren(item.getId(), menuMap)));
        return children;
    }
}
