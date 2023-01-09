package com.alex.utils.feign.fallback;

import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.UserException;
import com.alex.utils.feign.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable throwable) {
        log.error("异常原因:{}", throwable.getMessage(), throwable);
        return tUserVo -> {
           throw new UserException(ResultEnum.ERROR_USER_LIST);
        };
    }
}
