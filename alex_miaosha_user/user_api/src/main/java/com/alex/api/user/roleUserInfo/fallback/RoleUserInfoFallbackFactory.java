package com.alex.api.user.roleUserInfo.fallback;

import com.alex.api.user.roleUserInfo.api.RoleUserInfoApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleUserInfoFallbackFactory implements FallbackFactory<RoleUserInfoApi> {

    @Override
    public RoleUserInfoApi create(Throwable cause) {
        throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "roleUserInfo");
    }
}
