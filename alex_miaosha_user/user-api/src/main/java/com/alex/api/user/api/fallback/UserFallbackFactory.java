package com.alex.api.user.api.fallback;

import com.alex.api.user.api.UserApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFallbackFactory implements FallbackFactory<UserApi> {

    @Override
    public UserApi create(Throwable throwable) {
        log.error("异常原因:{}", throwable.getMessage(), throwable);
        return tUserVo -> {
           throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "user");
        };
    }
}
