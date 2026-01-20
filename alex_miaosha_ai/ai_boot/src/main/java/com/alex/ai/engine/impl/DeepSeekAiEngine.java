package com.alex.ai.engine.impl;

import com.alex.ai.client.deepseek.DeepSeekClient;
import com.alex.ai.config.AiProperties;
import com.alex.ai.engine.AiEngine;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI Agent：
 * DeepSeek 引擎（Strategy）
 */
@Component
@RequiredArgsConstructor
public class DeepSeekAiEngine implements AiEngine {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;

    @Override
    public String key() {
        return "deepseek";
    }

    @Override
    public boolean isEnabled(AiProperties aiProperties) {
        if (aiProperties == null || aiProperties.getDeepseek() == null) {
            return false;
        }
        String apiKey = aiProperties.getDeepseek().getApiKey();
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public AiAnalyzeResp analyze(AiAnalyzeReq req, String requestId, long start) {
        // AI Agent：优先级：请求指定 model > 服务端默认 model
        String defaultModel = aiProperties != null && aiProperties.getDeepseek() != null ? aiProperties.getDeepseek().getModel() : null;
        String model = pickFirstNotBlank(req == null ? null : req.getModel(), defaultModel);

        String llmContent = deepSeekClient.chat(req, aiProperties);
        return toAnalyzeRespFromLlm(requestId, llmContent, "deepseek:" + (model == null ? "default" : model), start);
    }

    /**
     * AI Agent：
     * 将 LLM 输出转换为统一响应结构
     */
    private AiAnalyzeResp toAnalyzeRespFromLlm(String requestId, String llmContent, String engine, long start) {
        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setRequestId(requestId);
        resp.setEngine(engine);

        if (llmContent == null || llmContent.trim().isEmpty()) {
            resp.setSummary("DeepSeek 返回为空。");
            resp.setKeyPoints(List.of());
            resp.setCostMs(System.currentTimeMillis() - start);
            return resp;
        }

        // AI Agent：优先按 JSON 解析（system prompt 已约束输出），解析失败则降级为纯摘要
        try {
            AiStructuredResult structured = objectMapper.readValue(llmContent, AiStructuredResult.class);
            resp.setSummary(structured.getSummary() == null ? "" : structured.getSummary());
            resp.setKeyPoints(structured.getKeyPoints() == null ? List.of() : structured.getKeyPoints());
        } catch (Exception ignored) {
            resp.setSummary(llmContent.trim());
            resp.setKeyPoints(List.of());
        }

        resp.setCostMs(System.currentTimeMillis() - start);
        return resp;
    }

    private String pickFirstNotBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b;
    }

    /**
     * AI Agent：
     * LLM 输出的结构化结果（system prompt 约束 JSON 格式）
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiStructuredResult {
        private String summary;
        private List<String> keyPoints;
    }
}


