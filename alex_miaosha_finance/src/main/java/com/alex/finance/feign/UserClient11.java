package com.alex.finance.feign;

import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import com.alex.common.pojo.vo.user.TUserVo;
import com.alex.finance.feign.fallback.UserFallbackFactory11;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * description:
 * author:       majf
 * createDate:   2022/12/28 11:24
 * version:      1.0.0
 */
@Component
@FeignClient(name = "alex-user", fallback = UserFallbackFactory11.class, configuration = FeignConfig.class)
public interface UserClient11 {

    @PostMapping(value = "/user/list")
    Result<List<TUserVo>> getList(@RequestBody(required = false) TUserVo tUserVo);
}
