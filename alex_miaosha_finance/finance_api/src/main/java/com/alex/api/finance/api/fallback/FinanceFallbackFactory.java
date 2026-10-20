package com.alex.api.finance.api.fallback;

import com.alex.api.finance.api.FinanceApi;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description:  Finance 微服务 Feign 远程调用降级工厂
 * author:       alex
 * createDate:   2023/1/28 11:44
 * version:      2.0.0
 */
@Component
@Slf4j
public class FinanceFallbackFactory implements FallbackFactory<FinanceApi> {

    @Override
    public FinanceApi create(Throwable throwable) {
        log.error("FinanceApi feign fallback triggered. cause: {}", throwable != null ? throwable.getMessage() : "unknown", throwable);
        return new FinanceApi() {
            @Override
            public Result<List<FinanceInfoVo>> getList(FinanceInfoVo tUserVo) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "财务服务不可用 [FinanceApi#getList]");
            }
        };
    }
}
