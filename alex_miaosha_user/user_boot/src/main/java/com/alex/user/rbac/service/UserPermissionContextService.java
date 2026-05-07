package com.alex.user.rbac.service;

import com.alex.api.user.userInfo.vo.UserPermissionContextVo;

public interface UserPermissionContextService {

    UserPermissionContextVo buildContext(Long userId);
}
