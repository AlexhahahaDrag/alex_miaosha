package com.alex.api.finance.api;

import com.alex.api.finance.api.fallback.FinanceFallbackFactory;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 *@description:
 *@author:       alex
 *@createDate:   2023/1/28 11:39
 *@version:      1.0.0
 */
@Component
@FeignClient(name = "alex-user-${spring.profiles.active:dev}", fallback = FinanceFallbackFactory.class, configuration = FeignConfig.class)
// TODO: 2023/1/13 写配置，写博客 
//此处读取的配置文件为引用client的配置文件
public interface FinanceApi {

    @PostMapping(value = "/user/list")
    Result<List<FinanceInfoVo>> getList(FinanceInfoVo tUserVo);
}
