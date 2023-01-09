package com.alex.utils.feign;

import com.alex.common.config.FeignConfig;
import com.alex.utils.feign.fallback.UserFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * description:
 * author:       majf
 * createDate:   2022/12/28 11:24
 * version:      1.0.0
 */
@FeignClient(name = "alex-user", fallback = UserFallbackFactory.class, configuration = FeignConfig.class)
public interface UserClient {


}
