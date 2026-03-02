package com.alex.api.oss.fileInfo.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.alex.api.oss.fileInfo.api.OssApi;

@Component
@Slf4j
public class OssFallbackFactory implements FallbackFactory<OssApi> {
    @Override
    public OssApi create(Throwable cause) {
        return null;
    }

    //    @Override
//    public OssApi create(Throwable throwable) {
//        log.error("异常原因:{}", throwable.getMessage(), throwable);
//
//        return ossApi -> {
//            throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
//        };
//    }
}
