package com.alex.user.roleInfo.service.impl;

import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.permissionInfo.service.PermissionInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.alex.user.roleInfo.entity.RoleInfo;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleInfo.service.RoleInfoService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * description:  角色信息表服务实现类
 * author:       majf
 * createDate:   2024-01-14 21:56:18
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleInfoServiceImp extends ServiceImpl<RoleInfoMapper, RoleInfo> implements RoleInfoService {

    private final RoleInfoMapper roleInfoMapper;

    private final PermissionInfoService permissionInfoService;

    private final RolePermissionInfoService rolePermissionInfoService;

    private final RoleUserInfoService roleUserInfoService;

    // RBAC-BE-RELATION-002: 角色权限/绑定变更后失效受影响用户的 permission_context 缓存
    private final PermissionContextCacheService permissionContextCacheService;

    private final UserUtils userUtils;

    @Override
    public Page<RoleInfoVo> getPage(Long pageNum, Long pageSize, RoleInfoVo roleInfoVo) {
        Page<RoleInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return roleInfoMapper.getPage(page, roleInfoVo);
    }

    /**
     * param: id
     * description: 查询角色信息
     * author:      majf
     * return:      com.alex.api.user.roleInfo.vo.RoleInfoVo
    */
    @Override
    public RoleInfoVo queryRoleInfo(String id) {
        RoleInfoVo roleInfoVo = roleInfoMapper.queryRoleInfo(id);
        // C1 修复：挂了 @DataPermission 后，越权或不存在的角色 id 查询结果为 null。
        // 之前没有判空会在下面直接 NPE（500），现在与 org/user 详情查询保持一致的空安全语义：
        // 越权/不存在统一返回 null，由 Controller 层当作「查无此角色」处理，不抛异常。
        if (roleInfoVo == null) {
            return null;
        }
        // 权限列表
        List<PermissionInfoVo> list = permissionInfoService.getList(null);
        roleInfoVo.setPermissionList(list);
        // 角色权限列表
        RolePermissionInfoVo rolePermissionInfoVo = new RolePermissionInfoVo();
        rolePermissionInfoVo.setRoleId(id);
        List<RolePermissionInfoVo> rolePermissionInfoVoList = rolePermissionInfoService.getList(rolePermissionInfoVo);
        roleInfoVo.setRolePermissionInfoVoList(rolePermissionInfoVoList);
        List<RoleUserInfoVo> roleUserInfoVoList = roleUserInfoService.list(Wrappers.<RoleUserInfo>lambdaQuery()
                        .eq(RoleUserInfo::getRoleId, id)
                        .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS))
                .stream()
                .map(item -> {
                    RoleUserInfoVo vo = new RoleUserInfoVo();
                    BeanUtils.copyProperties(item, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        roleInfoVo.setRoleUserInfoVoList(roleUserInfoVoList);
        return roleInfoVo;
    }

    @Override
    public String addRoleInfo(RoleInfoVo roleInfoVo) {
        assertRoleCodeUnique(roleInfoVo == null ? null : roleInfoVo.getRoleCode(), null);
        RoleInfo roleInfo = new RoleInfo();
        BeanUtils.copyProperties(roleInfoVo, roleInfo);
        roleInfoMapper.insert(roleInfo);
        // Return created id as String to avoid frontend page-lookup race / LIKE mismatch
        return String.valueOf(roleInfo.getId());
    }

    @Override
    public Boolean updateRoleInfo(RoleInfoVo roleInfoVo) {
        assertRoleAccessible(roleInfoVo == null ? null : roleInfoVo.getId());
        assertRoleCodeUnique(roleInfoVo == null ? null : roleInfoVo.getRoleCode(),
                roleInfoVo == null ? null : roleInfoVo.getId());
        RoleInfo roleInfo = new RoleInfo();
        BeanUtils.copyProperties(roleInfoVo, roleInfo);
        roleInfoMapper.updateById(roleInfo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRoleInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !StringUtils.isEmpty(id))
                .collect(Collectors.toList());
        if (idArr.isEmpty()) {
            return true;
        }
        // Ownership first: deny out-of-scope roles before bound-user guards.
        for (String roleId : idArr) {
            assertRoleAccessible(Long.valueOf(roleId));
        }
        long boundUserCount = roleUserInfoService.count(Wrappers.<RoleUserInfo>lambdaQuery()
                .in(RoleUserInfo::getRoleId, idArr)
                .eq(RoleUserInfo::getIsDelete, 0)
                .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
        if (boundUserCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色仍绑定用户，不能删除:");
        }
        // RBAC-BE-ROLE-002: cascade-invalidate role-permission in the same TX as delete.
        // Mirror RolePermissionInfoServiceImp.assignPermissions invalidate loop.
        List<RolePermissionInfo> activePermissions = rolePermissionInfoService.list(
                Wrappers.<RolePermissionInfo>lambdaQuery()
                        .in(RolePermissionInfo::getRoleId, idArr)
                        .eq(RolePermissionInfo::getStatus, SysConf.VALID_STATUS));
        for (RolePermissionInfo row : activePermissions) {
            row.setStatus(SysConf.INVALID_STATUS);
            if (!rolePermissionInfoService.updateById(row)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色权限旧关系失效失败:");
            }
        }
        // Optional harden: invalidate leftover valid role_user (should be empty after guard).
        // Collect user ids for permission_context cache clear.
        Set<String> affectedUserIds = new HashSet<>();
        List<RoleUserInfo> leftoverRoleUsers = roleUserInfoService.list(
                Wrappers.<RoleUserInfo>lambdaQuery()
                        .in(RoleUserInfo::getRoleId, idArr)
                        .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
        for (RoleUserInfo ru : leftoverRoleUsers) {
            ru.setStatus(SysConf.INVALID_STATUS);
            if (!roleUserInfoService.updateById(ru)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色用户旧关系失效失败:");
            }
            if (ru.getUserId() != null) {
                affectedUserIds.add(ru.getUserId());
            }
        }
        // RBAC-BE-RELATION-002: 级联删除成功后统一走 helper 失效受影响用户的缓存
        permissionContextCacheService.invalidateAll(toLongUserIds(affectedUserIds));
        roleInfoMapper.deleteBatchIds(idArr);
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

    /**
     * RBAC-BE-ROLE-003: roleCode must be non-empty and unique table-wide (update excludes self).
     */
    private void assertRoleCodeUnique(String roleCode, Long excludeId) {
        if (StringUtils.isEmpty(roleCode)) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色编码不能为空");
        }
        long duplicateCount = roleInfoMapper.selectCount(Wrappers.<RoleInfo>lambdaQuery()
                .eq(RoleInfo::getRoleCode, roleCode)
                .ne(excludeId != null, RoleInfo::getId, excludeId));
        if (duplicateCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色编码已存在");
        }
    }

    /**
     * Write-path ownership guard: non-super users must pass a scoped queryRoleInfo
     * before update/delete. Null means outside data scope — never silent success.
     */
    private void assertRoleAccessible(Long id) {
        if (id == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色ID不能为空");
        }
        TUserVo loginUser = userUtils.getLoginUser();
        if (loginUser == null) {
            // I1 修复：登录上下文不可用（如 loginToken 缓存缺失且降级查询失败）时必须 fail-closed，
            // 不能因为拿不到 loginUser 就当作"非超管走 query"最终又意外放行。
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问：登录上下文不可用");
        }
        if (isSuperAdminLogin(loginUser)) {
            return;
        }
        RoleInfoVo visible = roleInfoMapper.queryRoleInfo(String.valueOf(id));
        if (visible == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问其他机构的角色");
        }
    }

    /**
     * C2 修复：授予角色（把用户加入某角色）比改角色名危害更大，必须同时满足两条：
     * 1) 归属校验——非超管只能对自己数据范围内可见的角色执行 assign（复用 assertRoleAccessible）；
     * 2) 硬性规则——非超管一律不得把任何用户授予 super_super 角色，即使该角色行恰好在
     *    其可见范围内（operator 归属正常不会落在机构管理员范围，这里是纵深防御的第二道闸）。
     */
    private void assertRoleGrantable(Long roleId) {
        assertRoleAccessible(roleId);
        TUserVo loginUser = userUtils.getLoginUser();
        if (isSuperAdminLogin(loginUser)) {
            return;
        }
        RoleInfo roleInfo = roleInfoMapper.selectById(roleId);
        if (roleInfo != null && RbacRoleCodes.SUPER.equals(roleInfo.getRoleCode())) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权授予超级管理员角色");
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

    @Override
    public Boolean assignUsers(Long roleId, List<Long> userIds) {
        // C2 修复：assign 系列是"授予超管"这类最高危写操作，必须先过归属校验 + 超管授予禁令。
        assertRoleGrantable(roleId);
        return roleUserInfoService.assignUsersToRole(roleId, userIds);
    }

    @Override
    public Boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色权限分配参数错误:");
        }
        // C2 修复：selectById 是 BaseMapper 方法，不受 @DataPermission 约束，
        // 必须先经 assertRoleAccessible（走已挂注解的 queryRoleInfo）才能确认调用者对该角色有权限。
        assertRoleAccessible(roleId);
        RoleInfo roleInfo = roleInfoMapper.selectById(roleId);
        if (roleInfo == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色不存在:");
        }
        Boolean ok = rolePermissionInfoService.assignPermissions(roleId, permissionIds);
        List<RoleUserInfo> users = roleUserInfoService.list(Wrappers.<RoleUserInfo>lambdaQuery()
                .eq(RoleUserInfo::getRoleId, String.valueOf(roleId))
                .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
        Set<String> boundUserIds = new HashSet<>();
        for (RoleUserInfo ru : users) {
            if (ru.getUserId() != null) {
                boundUserIds.add(ru.getUserId());
            }
        }
        // RBAC-BE-RELATION-002: 权限授权变更后失效该角色下所有绑定用户的缓存
        permissionContextCacheService.invalidateAll(toLongUserIds(boundUserIds));
        return ok;
    }
}
