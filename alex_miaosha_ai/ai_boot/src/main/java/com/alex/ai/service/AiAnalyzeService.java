package com.alex.ai.service;

import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;

/**
 * AI Agent：
 * AI 分析服务（领域服务）
 */
public interface AiAnalyzeService {

    AiAnalyzeResp analyze(AiAnalyzeReq req);
}


