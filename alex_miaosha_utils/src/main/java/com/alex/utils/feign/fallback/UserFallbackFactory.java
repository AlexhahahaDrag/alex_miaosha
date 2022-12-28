package com.alex.utils.feign.fallback;

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
        return new UserClient(){

//            @Override
//            public Object bigDataTest(Object o) {
//                //出现异常，自定义返回内容，保证接口安全
//                return throwable.getMessage();
//            }
        };
    }
}
