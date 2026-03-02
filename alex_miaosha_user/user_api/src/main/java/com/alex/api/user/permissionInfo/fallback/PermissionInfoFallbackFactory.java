package com.alex.api.user.permissionInfo.fallback;

import com.alex.api.user.permissionInfo.api.PermissionInfoApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PermissionInfoFallbackFactory implements FallbackFactory<PermissionInfoApi> {

    @Override
    public PermissionInfoApi create(Throwable cause) {
        throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "permissionInfo");
    }
}
