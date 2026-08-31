package com.alex.ai.engine;

import com.alex.ai.config.AiProperties;
import com.alex.ai.engine.impl.RuleBasedAiEngine;
import com.alex.ai.stream.AiStreamSink;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.AiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    /**
     * 流式分析：引擎选择与 fallback 对齐 {@link #analyze}，失败时经 sink 回报。
     */
    public void analyzeStream(AiAnalyzeReq req, String requestId, long start, AiStreamSink sink) {
        String desiredKey = pickFirstNotBlank(req == null ? null : req.getEngine(),
                aiProperties == null ? null : aiProperties.getEngine());
        AiEngine desiredEngine = findEngineOrNull(desiredKey);

        if (desiredEngine == null || !desiredEngine.isEnabled(aiProperties)) {
            if (isFallbackEnabled()) {
                emitRuleFallback(req, requestId, start, sink, desiredKey);
                return;
            }
            String key = desiredKey == null ? "" : desiredKey;
            sink.error(ResultEnum.AI_ENGINE_UNAVAILABLE.getCode(),
                    ResultEnum.AI_ENGINE_UNAVAILABLE.getValue() + ": " + key);
            return;
        }

        if (ruleBasedAiEngine.key().equalsIgnoreCase(desiredEngine.key())) {
            desiredEngine.analyzeStream(req, requestId, start, sink);
            return;
        }

        String failedEngineKey = desiredEngine.key();
        AtomicReference<String> engineError = new AtomicReference<>();
        // 首个 LLM error 不转发给客户端：若将走 fallback，仅由 emitRuleFallback 收尾，避免 error+fallback 双通道污染
        AiStreamSink wrapped = new AiStreamSink() {
            @Override
            public void meta(String rid, String engine) {
                if (engineError.get() == null) {
                    sink.meta(rid, engine);
                }
            }

            @Override
            public void delta(String text) {
                if (engineError.get() == null) {
                    sink.delta(text);
                }
            }

            @Override
            public void done(AiAnalyzeResp resp) {
                if (engineError.get() == null) {
                    sink.done(resp);
                }
            }

            @Override
            public void error(String code, String message) {
                engineError.compareAndSet(null, message == null ? "" : message);
            }
        };

        try {
            desiredEngine.analyzeStream(req, requestId, start, wrapped);
            if (engineError.get() != null) {
                handleLlmStreamFailure(req, requestId, start, sink, failedEngineKey, engineError.get());
            }
        } catch (Exception e) {
            String engineKey = failedEngineKey == null ? "" : failedEngineKey;
            log.error("AI 引擎流式调用失败。engine={}, requestId={}, err={}",
                    engineKey, requestId, e.getMessage(), e);
            String detail = e.getMessage() == null || e.getMessage().isBlank()
                    ? ResultEnum.AI_ENGINE_CALL_FAILED.getValue()
                    : e.getMessage();
            handleLlmStreamFailure(req, requestId, start, sink, failedEngineKey, detail);
        }
    }

    private void handleLlmStreamFailure(AiAnalyzeReq req, String requestId, long start,
                                        AiStreamSink sink, String failedEngineKey, String detail) {
        if (isFallbackEnabled()) {
            emitRuleFallback(req, requestId, start, sink, failedEngineKey);
            return;
        }
        String msg = detail == null || detail.isBlank()
                ? ResultEnum.AI_ENGINE_CALL_FAILED.getValue()
                : detail;
        sink.error(ResultEnum.AI_ENGINE_CALL_FAILED.getCode(), msg);
    }

    private void emitRuleFallback(AiAnalyzeReq req, String requestId, long start,
                                  AiStreamSink sink, String failedEngineKey) {
        sink.meta(requestId, "rule-based(fallback)");
        AiAnalyzeResp resp = ruleBasedAiEngine.analyze(req, requestId, start);
        resp.setEngine("rule-based(fallback)");
        String displayName = AiEngineType.fromKey(failedEngineKey)
                .map(AiEngineType::getDisplayName)
                .orElse(null);
        resp.setSummary(displayName != null
                ? displayName + " 调用失败，已回退规则引擎。"
                : "AI 引擎调用失败，已回退规则引擎。");
        if (resp.getSummary() != null && !resp.getSummary().isEmpty()) {
            sink.delta(resp.getSummary());
        }
        sink.done(resp);
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
