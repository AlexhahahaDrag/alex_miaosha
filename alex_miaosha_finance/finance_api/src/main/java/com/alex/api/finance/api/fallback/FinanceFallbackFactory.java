package com.alex.api.finance.api.fallback;

import com.alex.api.finance.api.FinanceApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 *description:
 *author:       alex
 *createDate:   2023/1/28 11:44
 *version:      1.0.0
 */
@Component
@Slf4j
public class FinanceFallbackFactory implements FallbackFactory<FinanceApi> {

    @Override
    public FinanceApi create(Throwable throwable) {
        log.error("异常原因:{}", throwable.getMessage(), throwable);
        return finance -> {
           throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "finance");
        };
    }
}
