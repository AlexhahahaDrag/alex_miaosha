package com.alex.api.user.roleInfo.fallback;

import com.alex.api.user.roleInfo.api.RoleInfoApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleInfoFallbackFactory implements FallbackFactory<RoleInfoApi> {

    @Override
    public RoleInfoApi create(Throwable cause) {
        throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "roleInfo");
    }
}
