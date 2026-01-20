package com.alex.ai.engine;

import com.alex.ai.config.AiProperties;
import com.alex.ai.engine.impl.RuleBasedAiEngine;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI Agent：
 * AI 引擎路由器（简单工厂/路由）
 *
 * 职责：
 * - 选择合适的策略（AiEngine）
 * - 统一处理异常与回退（fallback 到 rule-based）
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiEngineRouter {

    private final AiProperties aiProperties;
    private final RuleBasedAiEngine ruleBasedAiEngine;
    private final List<AiEngine> engines;

    public AiAnalyzeResp analyze(AiAnalyzeReq req, String requestId, long start) {
        String desiredKey = pickFirstNotBlank(req == null ? null : req.getEngine(), aiProperties == null ? null : aiProperties.getEngine());
        AiEngine desiredEngine = findEngineOrNull(desiredKey);

        // AI Agent：找不到/不可用则走默认兜底
        if (desiredEngine == null || !desiredEngine.isEnabled(aiProperties)) {
            return ruleBasedAiEngine.analyze(req, requestId, start);
        }

        // AI Agent：如果目标本身就是 rule-based，直接执行
        if (ruleBasedAiEngine.key().equalsIgnoreCase(desiredEngine.key())) {
            return ruleBasedAiEngine.analyze(req, requestId, start);
        }

        // AI Agent：非 rule-based 引擎执行失败 -> 回退 rule-based（保持原行为）
        try {
            return desiredEngine.analyze(req, requestId, start);
        } catch (Exception e) {
            log.error("AI 引擎调用失败，将回退规则引擎。engine={}, requestId={}, err={}",
                    desiredEngine.key(), requestId, e.getMessage(), e);
            AiAnalyzeResp fallback = ruleBasedAiEngine.analyze(req, requestId, start);
            fallback.setEngine("rule-based(fallback)");
            // AI Agent：保持原有语义（DeepSeek 失败时提示 DeepSeek）
            if ("deepseek".equalsIgnoreCase(desiredEngine.key())) {
                fallback.setSummary("DeepSeek 调用失败，已回退规则引擎。");
            } else {
                fallback.setSummary("AI 引擎调用失败，已回退规则引擎。");
            }
            return fallback;
        }
    }

    private AiEngine findEngineOrNull(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        if (engines == null || engines.isEmpty()) {
            return null;
        }
        for (AiEngine engine : engines) {
            if (engine != null && engine.key() != null && engine.key().equalsIgnoreCase(key.trim())) {
                return engine;
            }
        }
        return null;
    }

    private String pickFirstNotBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b;
    }
}


