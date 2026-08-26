package com.alex.ai.engine;

import com.alex.ai.config.AiProperties;
import com.alex.ai.engine.impl.RuleBasedAiEngine;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.AiException;
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
        String desiredKey = pickFirstNotBlank(req == null ? null : req.getEngine(),
                aiProperties == null ? null : aiProperties.getEngine());
        AiEngine desiredEngine = findEngineOrNull(desiredKey);

        // 找不到/不可用
        if (desiredEngine == null || !desiredEngine.isEnabled(aiProperties)) {
            if (isFallbackEnabled()) {
                return ruleBasedAiEngine.analyze(req, requestId, start);
            }
            String key = desiredKey == null ? "" : desiredKey;
            throw new AiException(ResultEnum.AI_ENGINE_UNAVAILABLE.getCode(),
                    ResultEnum.AI_ENGINE_UNAVAILABLE.getValue() + ": " + key);
        }

        // 目标本身就是 rule-based，直接执行
        if (ruleBasedAiEngine.key().equalsIgnoreCase(desiredEngine.key())) {
            return ruleBasedAiEngine.analyze(req, requestId, start);
        }

        // 非 rule-based 引擎执行失败
        try {
            return desiredEngine.analyze(req, requestId, start);
        } catch (Exception e) {
            String engineKey = desiredEngine.key() == null ? "" : desiredEngine.key();
            log.error("AI 引擎调用失败。engine={}, requestId={}, err={}",
                    engineKey, requestId, e.getMessage(), e);
            if (!isFallbackEnabled()) {
                String detail = e.getMessage() == null || e.getMessage().isBlank()
                        ? ResultEnum.AI_ENGINE_CALL_FAILED.getValue()
                        : e.getMessage();
                throw new AiException(ResultEnum.AI_ENGINE_CALL_FAILED.getCode(), detail);
            }
            AiAnalyzeResp fallback = ruleBasedAiEngine.analyze(req, requestId, start);
            fallback.setEngine("rule-based(fallback)");
            String displayName = AiEngineType.fromKey(engineKey)
                    .map(AiEngineType::getDisplayName)
                    .orElse(null);
            fallback.setSummary(displayName != null
                    ? displayName + " 调用失败，已回退规则引擎。"
                    : "AI 引擎调用失败，已回退规则引擎。");
            return fallback;
        }
    }

    private boolean isFallbackEnabled() {
        // fallback 缺失时按规格默认 enabled=true
        if (aiProperties == null || aiProperties.getFallback() == null) {
            return true;
        }
        return aiProperties.getFallback().isEnabled();
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
