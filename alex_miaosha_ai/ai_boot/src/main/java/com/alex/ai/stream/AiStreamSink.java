package com.alex.ai.stream;

import com.alex.api.ai.vo.AiAnalyzeResp;

/**
 * SSE 流式分析下游回调契约。
 */
public interface AiStreamSink {

    void meta(String requestId, String engine);

    void delta(String text);

    void done(AiAnalyzeResp resp);

    void error(String code, String message);
}
