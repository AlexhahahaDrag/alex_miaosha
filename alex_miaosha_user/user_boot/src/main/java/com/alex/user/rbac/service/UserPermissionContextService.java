package com.alex.user.rbac.service;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;

import java.util.List;

public interface UserPermissionContextService {

    /**
     * Build permission context. When {@code includeMenus} is false, skip menu tree load
     * (login slim path); org/roles/permission codes are still populated.
     */
    UserPermissionContextVo buildContext(Long userId, boolean includeMenus);

    default UserPermissionContextVo buildContext(Long userId) {
        return buildContext(userId, true);
    }

    /**
     * Visible menu tree for the user (full tree + permission filter). Requires auth at call site.
     */
    List<MenuInfoVo> listVisibleMenus(Long userId);
}
