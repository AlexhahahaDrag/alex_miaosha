package com.alex.ai.service.impl;

import com.alex.ai.engine.AiEngineRouter;
import com.alex.ai.service.AiAnalyzeService;
import com.alex.ai.stream.AiStreamSink;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

/**
 * AI Agent：
 * AI 分析服务实现（当前为轻量规则引擎示例）
 *
 * 说明：
 * - 这里先提供一个“可用”的默认实现，确保其它模块能稳定调用，不依赖外部 LLM Key
 * - 后续你可以在此处接入 OpenAI / 本地大模型 / 向量检索等能力
 */
@Service
@RequiredArgsConstructor
public class AiAnalyzeServiceImpl implements AiAnalyzeService {

    private final AiEngineRouter aiEngineRouter;

    @Override
    public AiAnalyzeResp analyze(AiAnalyzeReq req) {
        long start = System.currentTimeMillis();

        String content = req == null ? null : req.getContent();
        String bizType = req == null ? null : req.getBizType();

        String requestId = buildRequestId(bizType, content);

        // AI Agent：策略模式（Strategy）+ 路由器（Router）
        // - 由 AiEngineRouter 选择具体引擎策略并统一处理回退
        return aiEngineRouter.analyze(req, requestId, start);
    }

    @Override
    public void analyzeStream(AiAnalyzeReq req, AiStreamSink sink) {
        long start = System.currentTimeMillis();
        String content = req == null ? null : req.getContent();
        String bizType = req == null ? null : req.getBizType();
        String requestId = buildRequestId(bizType, content);
        aiEngineRouter.analyzeStream(req, requestId, start, sink);
    }

    private String buildRequestId(String bizType, String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String raw = (bizType == null ? "" : bizType) + "|" + (content == null ? "" : content) + "|" + Instant.now().toEpochMilli();
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            // 取前 12 bytes 足够做追踪（避免超长）
            StringBuilder sb = new StringBuilder("ai-");
            for (int i = 0; i < 12 && i < digest.length; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "ai-" + Instant.now().toEpochMilli();
        }
    }

}


