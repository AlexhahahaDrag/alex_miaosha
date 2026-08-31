package com.alex.ai.service;

import com.alex.ai.stream.AiStreamSink;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;

/**
 * AI Agent：
 * AI 分析服务（领域服务）
 */
public interface AiAnalyzeService {

    AiAnalyzeResp analyze(AiAnalyzeReq req);

    /**
     * 流式分析：生成 requestId 后委托路由器，经 sink 推送 meta/delta/done/error。
     */
    void analyzeStream(AiAnalyzeReq req, AiStreamSink sink);
}


