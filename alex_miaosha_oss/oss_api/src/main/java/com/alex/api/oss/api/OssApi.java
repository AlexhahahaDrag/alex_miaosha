package com.alex.api.oss.api;

import com.alex.api.oss.api.fallback.OssFallbackFactory;
import com.alex.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/13 13:58
 * version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", fallback = OssFallbackFactory.class, configuration = FeignConfig.class)
public interface OssApi {

}
