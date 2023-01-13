package com.alex.api.oss.api.fallback;

import com.alex.api.oss.api.OssApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OssFallbackFactory implements FallbackFactory<OssApi> {

    @Override
    public OssApi create(Throwable throwable) {
        log.error("异常原因:{}", throwable.getMessage(), throwable);

        return ossApi -> {
            throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
        };
    }
}
