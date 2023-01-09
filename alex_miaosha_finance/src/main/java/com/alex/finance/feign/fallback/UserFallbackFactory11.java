package com.alex.finance.feign.fallback;

import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.UserException;
import com.alex.finance.feign.UserClient11;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFallbackFactory11 implements FallbackFactory<UserClient11> {

    @Override
    public UserClient11 create(Throwable throwable) {
        log.error("异常原因:{}", throwable.getMessage(), throwable);
        return tUserVo -> {
           throw new UserException(ResultEnum.ERROR_USER_LIST);
        };
    }
}
