package com.alex.ai.engine.impl;

import com.alex.ai.engine.AiEngine;
import com.alex.ai.engine.AiEngineType;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Agent：
 * 规则引擎（Strategy）
 * - 用作默认引擎 & DeepSeek 失败时的兜底
 */
@Component
public class RuleBasedAiEngine implements AiEngine {

    @Override
    public String key() {
        return AiEngineType.RULE_BASED.getKey();
    }

    @Override
    public AiAnalyzeResp analyze(AiAnalyzeReq req, String requestId, long start) {
        String content = req == null ? null : req.getContent();
        String bizType = req == null ? null : req.getBizType();
        int depth = req == null || req.getDepth() == null ? 1 : req.getDepth();

        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setRequestId(requestId);
        resp.setEngine(key());

        if (content == null || content.trim().isEmpty()) {
            resp.setSummary("内容为空，无法进行分析。");
            resp.setKeyPoints(List.of("请提供 content 字段"));
            resp.setCostMs(System.currentTimeMillis() - start);
            return resp;
        }

        String trimmed = content.trim();
        List<String> points = new ArrayList<>();
        points.add("bizType=" + (bizType == null ? "default" : bizType));
        points.add("contentLength=" + trimmed.length());
        points.add("depth=" + depth);

        if (depth >= 2) {
            points.add("包含数字：" + (trimmed.matches(".*\\d+.*") ? "是" : "否"));
            points.add("包含链接：" + (trimmed.contains("http://") || trimmed.contains("https://") ? "是" : "否"));
        }

        if (depth >= 3) {
            // 简单句号/换行切句
            String[] sentences = trimmed.split("[。.!?\\n\\r]+");
            points.add("句子数(粗略)=" + Math.max(1, sentences.length));
        }

        resp.setSummary("AI Agent：已完成基础分析（规则引擎示例）。");
        resp.setKeyPoints(points);
        resp.setCostMs(System.currentTimeMillis() - start);
        return resp;
    }
}


