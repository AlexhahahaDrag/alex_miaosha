package com.alex.ai.engine.impl;

import com.alex.ai.client.openai.OpenAiCompatibleClient;
import com.alex.ai.config.AiProperties;
import com.alex.ai.config.OpenAiCompatibleProperties;
import com.alex.ai.engine.AiEngine;
import com.alex.ai.engine.AiEngineType;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * OpenAI Chat Completions 兼容引擎抽象基类。
 * DeepSeek / SenseNova 等子类只需声明引擎类型与配置解析。
 */
@Slf4j
public abstract class AbstractOpenAiCompatibleAiEngine implements AiEngine {

    protected final OpenAiCompatibleClient openAiCompatibleClient;
    protected final ObjectMapper objectMapper;
    protected final AiProperties aiProperties;

    protected AbstractOpenAiCompatibleAiEngine(OpenAiCompatibleClient openAiCompatibleClient,
                                               ObjectMapper objectMapper,
                                               AiProperties aiProperties) {
        this.openAiCompatibleClient = openAiCompatibleClient;
        this.objectMapper = objectMapper;
        this.aiProperties = aiProperties;
    }

    protected abstract AiEngineType engineType();

    protected abstract OpenAiCompatibleProperties resolveProps(AiProperties aiProperties);

    @Override
    public String key() {
        return engineType().getKey();
    }

    @Override
    public AiAnalyzeResp analyze(AiAnalyzeReq req, String requestId, long start) {
        OpenAiCompatibleProperties props = resolveProps(aiProperties);
        String defaultModel = props == null ? null : props.getModel();
        String model = pickFirstNotBlank(req == null ? null : req.getModel(), defaultModel);
        String engineTag = key() + ":" + (model == null ? "default" : model);

        String llmContent = openAiCompatibleClient.chat(req, props, engineType().getDisplayName());
        return toAnalyzeRespFromLlm(requestId, llmContent, engineTag, start);
    }

    private AiAnalyzeResp toAnalyzeRespFromLlm(String requestId, String llmContent, String engine, long start) {
        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setRequestId(requestId);
        resp.setEngine(engine);

        if (llmContent == null || llmContent.trim().isEmpty()) {
            resp.setSummary(engineType().getDisplayName() + " 返回为空。");
            resp.setKeyPoints(List.of());
            resp.setCostMs(System.currentTimeMillis() - start);
            return resp;
        }

        try {
            AiStructuredResult structured = objectMapper.readValue(llmContent, AiStructuredResult.class);
            resp.setSummary(structured.getSummary() == null ? "" : structured.getSummary());
            resp.setKeyPoints(structured.getKeyPoints() == null ? List.of() : structured.getKeyPoints());
        } catch (Exception e) {
            log.warn("LLM JSON parse failed, engine={}, err={}", engine, e.toString());
            resp.setSummary(llmContent.trim());
            resp.setKeyPoints(List.of());
        }

        resp.setCostMs(System.currentTimeMillis() - start);
        return resp;
    }

    private static String pickFirstNotBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AiStructuredResult {
        private String summary;
        private List<String> keyPoints;
    }
}
