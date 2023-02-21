package com.alex.api.user.api;

import com.alex.api.user.api.fallback.UserFallbackFactory;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/13 10:45
 * version:      1.0.0
 */
@Component
@FeignClient(name = "alex-user-${spring.profiles.active:dev}", fallback = UserFallbackFactory.class, configuration = FeignConfig.class)
// TODO: 2023/1/13 写配置，写博客 
//此处读取的配置文件为引用client的配置文件
public interface UserApi {

    @PostMapping(value = "/api/v1/user/list")
    Result<List<TUserVo>> getList(@RequestBody TUserVo tUserVo);

    // TODO: 2023/2/21 测试token携带未生效问题 
    @GetMapping(value = "/api/v1/user/getUserInfo")
    TUserVo getUserByUsername(@RequestParam("username") String username);

    @GetMapping(value = "/api/v1/user/authToken")
    Result<Boolean> authToken(@RequestParam("token") String token);
}
