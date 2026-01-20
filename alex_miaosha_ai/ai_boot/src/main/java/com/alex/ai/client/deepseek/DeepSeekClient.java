package com.alex.ai.client.deepseek;

import com.alex.ai.client.deepseek.dto.DeepSeekChatCompletionRequest;
import com.alex.ai.client.deepseek.dto.DeepSeekChatCompletionResponse;
import com.alex.ai.config.AiProperties;
import com.alex.ai.config.DeepSeekProperties;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI Agent：
 * DeepSeek 客户端（HTTP 调用）
 *
 * 设计点：
 * - 采用 OpenAI Chat Completions 兼容格式，便于未来替换模型/代理服务
 * - 不做流式（stream=false），保持与现有 Result<AiAnalyzeResp> 一致
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DeepSeekClient {

    @Qualifier("aiRestTemplate")
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public String chat(AiAnalyzeReq req, AiProperties aiProperties) {
        DeepSeekProperties props = aiProperties == null ? null : aiProperties.getDeepseek();
        if (props == null) {
            throw new IllegalStateException("DeepSeek 配置缺失");
        }
        if (props.getApiKey() == null || props.getApiKey().trim().isEmpty()) {
            throw new IllegalStateException("DeepSeek apiKey 未配置");
        }

        String url = buildUrl(props);
        DeepSeekChatCompletionRequest body = buildRequestBody(req, props);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // AI Agent：DeepSeek 通常使用 Bearer Token 方式鉴权（与 OpenAI 兼容）
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey().trim());

        HttpEntity<DeepSeekChatCompletionRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<DeepSeekChatCompletionResponse> resp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DeepSeekChatCompletionResponse.class
        );

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("DeepSeek 请求失败，httpStatus=" + resp.getStatusCodeValue());
        }

        DeepSeekChatCompletionResponse data = resp.getBody();
        if (data == null || data.getChoices() == null || data.getChoices().isEmpty()) {
            throw new IllegalStateException("DeepSeek 返回为空 choices");
        }
        DeepSeekChatCompletionResponse.Choice choice0 = data.getChoices().get(0);
        if (choice0 == null || choice0.getMessage() == null || choice0.getMessage().getContent() == null) {
            throw new IllegalStateException("DeepSeek 返回为空 message.content");
        }
        return choice0.getMessage().getContent();
    }

    private String buildUrl(DeepSeekProperties props) {
        String base = props.getBaseUrl() == null ? "" : props.getBaseUrl().trim();
        String path = props.getChatCompletionsPath() == null ? "" : props.getChatCompletionsPath().trim();
        if (base.endsWith("/") && path.startsWith("/")) {
            return base.substring(0, base.length() - 1) + path;
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }

    private DeepSeekChatCompletionRequest buildRequestBody(AiAnalyzeReq req, DeepSeekProperties props) {
        DeepSeekChatCompletionRequest request = new DeepSeekChatCompletionRequest();
        request.setModel(pickFirstNotBlank(req == null ? null : req.getModel(), props.getModel()));
        request.setTemperature(req != null && req.getTemperature() != null ? req.getTemperature() : props.getTemperature());
        request.setMaxTokens(req != null && req.getMaxTokens() != null ? req.getMaxTokens() : props.getMaxTokens());

        List<DeepSeekChatCompletionRequest.Message> messages = new ArrayList<>();

        // system prompt：约束输出为 JSON，便于服务端稳定解析
        DeepSeekChatCompletionRequest.Message system = new DeepSeekChatCompletionRequest.Message();
        system.setRole("system");
        system.setContent(buildSystemPrompt(req));
        messages.add(system);

        DeepSeekChatCompletionRequest.Message user = new DeepSeekChatCompletionRequest.Message();
        user.setRole("user");
        user.setContent(buildUserPrompt(req));
        messages.add(user);

        request.setMessages(messages);
        request.setStream(false);
        return request;
    }

    private String buildSystemPrompt(AiAnalyzeReq req) {
        // AI Agent：要求模型严格输出 JSON，避免 Markdown/解释性文本导致解析失败
        return "你是一个后端 AI 分析服务。请严格只输出 JSON，不要输出 Markdown，不要输出多余解释。\n"
                + "输出格式：{\"summary\":\"...\",\"keyPoints\":[\"...\",\"...\"]}\n"
                + "keyPoints 数量建议 3-8 条，中文输出。";
    }

    private String buildUserPrompt(AiAnalyzeReq req) {
        String bizType = req == null ? null : req.getBizType();
        Integer depth = req == null ? null : req.getDepth();
        Map<String, Object> context = req == null ? null : req.getContext();
        String content = req == null ? null : req.getContent();

        StringBuilder sb = new StringBuilder();
        sb.append("bizType=").append(bizType == null ? "default" : bizType).append("\n");
        sb.append("depth=").append(depth == null ? 1 : depth).append("\n");
        if (context != null && !context.isEmpty()) {
            try {
                sb.append("context=").append(objectMapper.writeValueAsString(context)).append("\n");
            } catch (Exception e) {
                // AI Agent：context 序列化失败不阻断主流程
                log.warn("DeepSeek context 序列化失败，将忽略 context。");
            }
        }
        sb.append("content=").append(content == null ? "" : content);
        return sb.toString();
    }

    private String pickFirstNotBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b;
    }
}


