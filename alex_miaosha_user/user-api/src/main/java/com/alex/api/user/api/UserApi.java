package com.alex.api.user.api;

import com.alex.api.user.api.fallback.UserFallbackFactory;
import com.alex.api.user.dto.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

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

    @PostMapping(value = "/user/list")
    Result<List<TUserVo>> getList(TUserVo tUserVo);
}
