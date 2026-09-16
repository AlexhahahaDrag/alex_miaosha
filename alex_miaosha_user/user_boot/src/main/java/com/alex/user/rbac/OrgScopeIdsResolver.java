package com.alex.user.rbac;

import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolve caller org scope S for role-org binding guards.
 * Mirrors DataPermissionHandlerImpl admin subtree + login-org resolution.
 */
public class OrgScopeIdsResolver {

    private final OrgSubtreeLookup orgSubtreeLookup;

    public OrgScopeIdsResolver(OrgSubtreeLookup orgSubtreeLookup) {
        this.orgSubtreeLookup = orgSubtreeLookup == null ? OrgSubtreeLookup.NOOP : orgSubtreeLookup;
    }

    public static Long resolveLoginOrgId(TUserVo loginUser) {
        if (loginUser == null) {
            return null;
        }
        if (loginUser.getOrgInfoVo() != null && loginUser.getOrgInfoVo().getId() != null) {
            return loginUser.getOrgInfoVo().getId();
        }
        return loginUser.getOrgId();
    }

    public static boolean isSuperAdmin(TUserVo loginUser) {
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

    public static boolean isAdmin(TUserVo loginUser) {
        if (loginUser == null) {
            return false;
        }
        List<RoleInfoVo> roles = loginUser.getRoleInfoVoList();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (RoleInfoVo role : roles) {
            if (role != null && RbacRoleCodes.ADMIN.equals(role.getRoleCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return null when super (unrestricted); otherwise selfOrg ∪ descendants (admin) or singleton selfOrg (user).
     *         Empty set when login org cannot be resolved.
     */
    public Set<Long> resolveOrgScopeIds(TUserVo loginUser) {
        if (isSuperAdmin(loginUser)) {
            return null;
        }
        Long selfOrgId = resolveLoginOrgId(loginUser);
        if (selfOrgId == null) {
            return Collections.emptySet();
        }
        if (!isAdmin(loginUser)) {
            return Collections.singleton(selfOrgId);
        }
        Set<Long> scopeIds = new LinkedHashSet<>();
        scopeIds.add(selfOrgId);
        List<Long> descendants = orgSubtreeLookup.findDescendantOrgIds(selfOrgId);
        if (descendants != null) {
            for (Long id : descendants) {
                if (id != null) {
                    scopeIds.add(id);
                }
            }
        }
        return scopeIds;
    }
}
