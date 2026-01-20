package com.alex.api.ai.api.fallback;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * AI Agent：
 * AI 服务不可用时的降级处理
 */
@Component
@Slf4j
public class AiAnalyzeFallbackFactory implements FallbackFactory<AiAnalyzeApi> {

    @Override
    public AiAnalyzeApi create(Throwable throwable) {
        log.error("AI 服务调用异常: {}", throwable.getMessage(), throwable);
        return req -> {
            throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "ai");
        };
    }
}


