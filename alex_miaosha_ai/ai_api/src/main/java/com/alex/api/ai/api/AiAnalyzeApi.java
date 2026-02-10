package com.alex.api.ai.api;

import com.alex.api.ai.api.fallback.AiAnalyzeFallbackFactory;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI Agent：
 * AI 分析服务 Feign 接口（供其它模块调用）
 *
 * 服务名约定：alex-ai-${spring.profiles.active}
 * - 由 Nacos 注册中心按 profile 区分服务实例
 */
@Component
@FeignClient(
        name = "alex-ai-${spring.profiles.active:dev}",
        // AI Agent：修正 Feign 降级配置：当前实现类为 FallbackFactory，因此这里必须用 fallbackFactory
        fallbackFactory = AiAnalyzeFallbackFactory.class,
        configuration = FeignConfig.class
)
public interface AiAnalyzeApi {

    @PostMapping("${api.version:/api/v1}/ai/analyze")
    Result<AiAnalyzeResp> analyze(@RequestBody AiAnalyzeReq req);
}


