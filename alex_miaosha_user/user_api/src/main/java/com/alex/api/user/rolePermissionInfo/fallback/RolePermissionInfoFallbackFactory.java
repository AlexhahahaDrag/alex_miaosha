package com.alex.api.user.rolePermissionInfo.fallback;

import com.alex.api.user.rolePermissionInfo.api.RolePermissionInfoApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RolePermissionInfoFallbackFactory implements FallbackFactory<RolePermissionInfoApi> {

    @Override
    public RolePermissionInfoApi create(Throwable cause) {
        throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "rolePermissionInfo");
    }
}
