package com.alex.ai.engine;

import com.alex.ai.config.AiProperties;
import com.alex.ai.stream.AiStreamSink;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;

/**
 * AI Agent：
 * AI 引擎策略接口（策略模式 Strategy）
 *
 * 说明：
 * - 每一种引擎（DeepSeek / rule-based / 未来的其它 LLM）都实现该接口
 * - Router 负责选择策略并处理回退
 */
public interface AiEngine {

    /**
     * AI Agent：引擎标识（用于 req.engine / 配置 ai.engine）
     */
    String key();

    /**
     * AI Agent：引擎是否可用（例如 DeepSeek 必须配置 apiKey）
     */
    default boolean isEnabled(AiProperties aiProperties) {
        return true;
    }

    /**
     * AI Agent：执行分析
     *
     * @param req 请求
     * @param requestId 请求ID（上层生成，便于链路追踪）
     * @param start 开始时间（ms）
     */
    AiAnalyzeResp analyze(AiAnalyzeReq req, String requestId, long start);

    /**
     * AI Agent：流式分析（默认：整包 analyze 后拆成 meta → 可选 delta → done）
     */
    default void analyzeStream(AiAnalyzeReq req, String requestId, long start, AiStreamSink sink) {
        try {
            sink.meta(requestId, key());
            AiAnalyzeResp resp = analyze(req, requestId, start);
            if (resp.getSummary() != null && !resp.getSummary().isEmpty()) {
                sink.delta(resp.getSummary());
            }
            sink.done(resp);
        } catch (Exception e) {
            sink.error("500702", e.getMessage() == null ? "AI 引擎调用失败" : e.getMessage());
        }
    }
}


