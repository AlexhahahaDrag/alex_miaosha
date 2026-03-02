package com.alex.api.user.orgInfo.fallback;

import com.alex.api.user.orgInfo.api.OrgInfoApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrgInfoFallbackFactory implements FallbackFactory<OrgInfoApi> {

    @Override
    public OrgInfoApi create(Throwable cause) {
        throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "orgInfo");
    }
}
