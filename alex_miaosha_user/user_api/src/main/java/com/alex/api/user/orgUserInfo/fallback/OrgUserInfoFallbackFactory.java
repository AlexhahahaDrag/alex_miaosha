package com.alex.api.user.orgUserInfo.fallback;

import com.alex.api.user.orgUserInfo.api.OrgUserInfoApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrgUserInfoFallbackFactory implements FallbackFactory<OrgUserInfoApi> {

    @Override
    public OrgUserInfoApi create(Throwable cause) {
        throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "orgUserInfo");
    }
}
