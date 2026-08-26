package com.alex.ai.client.openai;

import com.alex.api.ai.vo.AiAnalyzeReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * OpenAI 兼容客户端的 prompt 组装（system JSON 约束 + user 业务字段）。
 */
@Slf4j
public final class AiPromptBuilder {

    private AiPromptBuilder() {
    }

    /**
     * 系统提示：约束模型严格输出 JSON。
     */
    public static String buildSystemPrompt(AiAnalyzeReq req) {
        return "你是一个后端 AI 分析服务。请严格只输出 JSON，不要输出 Markdown，不要输出多余解释。\n"
                + "输出格式：{\"summary\":\"...\",\"keyPoints\":[\"...\",\"...\"]}\n"
                + "keyPoints 数量建议 3-8 条，中文输出。";
    }

    /**
     * 用户提示：bizType / depth / context / content。
     * context 序列化失败时忽略，不抛异常。
     */
    public static String buildUserPrompt(AiAnalyzeReq req, ObjectMapper objectMapper) {
        String bizType = req == null ? null : req.getBizType();
        Integer depth = req == null ? null : req.getDepth();
        Map<String, Object> context = req == null ? null : req.getContext();
        String content = req == null ? null : req.getContent();

        StringBuilder sb = new StringBuilder();
        sb.append("bizType=").append(bizType == null ? "default" : bizType).append("\n");
        sb.append("depth=").append(depth == null ? 1 : depth).append("\n");
        if (context != null && !context.isEmpty() && objectMapper != null) {
            try {
                String contextJson = objectMapper.writeValueAsString(context);
                sb.append("context=").append(contextJson).append("\n");
            } catch (Exception e) {
                log.warn("OpenAI context 序列化失败，将忽略 context。");
            }
        }
        sb.append("content=").append(content == null ? "" : content);
        return sb.toString();
    }
}
