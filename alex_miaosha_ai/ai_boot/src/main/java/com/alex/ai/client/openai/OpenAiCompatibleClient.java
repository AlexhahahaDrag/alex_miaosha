package com.alex.ai.client.openai;

import com.alex.ai.client.openai.dto.OpenAiChatCompletionRequest;
import com.alex.ai.client.openai.dto.OpenAiChatCompletionResponse;
import com.alex.ai.config.AiHttpConfig;
import com.alex.ai.config.OpenAiCompatibleProperties;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI Chat Completions 兼容 HTTP 客户端（WebClient）。
 * DeepSeek / SenseNova 等引擎均通过此客户端外呼。
 */
@Component
@Slf4j
public class OpenAiCompatibleClient {

    private final ObjectMapper objectMapper;

    public OpenAiCompatibleClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 调用兼容 Chat Completions 接口，返回 message.content。
     *
     * @param req           分析请求
     * @param props         提供商配置（baseUrl / apiKey / model 等）
     * @param providerLabel 错误信息中的提供商标签
     */
    public String chat(AiAnalyzeReq req, OpenAiCompatibleProperties props, String providerLabel) {
        String label = providerLabel == null || providerLabel.trim().isEmpty() ? "OpenAI" : providerLabel.trim();
        if (props == null) {
            throw new IllegalStateException(label + " 配置缺失");
        }
        if (props.getApiKey() == null || props.getApiKey().trim().isEmpty()) {
            throw new IllegalStateException(label + " apiKey 未配置");
        }

        String url = buildUrl(props);
        OpenAiChatCompletionRequest body = buildRequestBody(req, props);

        int timeoutMs = props.getTimeoutMs() == null || props.getTimeoutMs() <= 0
                ? 15000
                : props.getTimeoutMs();
        WebClient webClient = AiHttpConfig.buildAiWebClient(timeoutMs);

        OpenAiChatCompletionResponse data;
        try {
            data = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey().trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(OpenAiChatCompletionResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new IllegalStateException(
                    label + " 请求失败，httpStatus=" + e.getRawStatusCode(), e);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(label + " 请求失败：" + e.getMessage(), e);
        }

        if (data == null || data.getChoices() == null || data.getChoices().isEmpty()) {
            throw new IllegalStateException(label + " 返回为空 choices");
        }
        OpenAiChatCompletionResponse.Choice choice0 = data.getChoices().get(0);
        if (choice0 == null || choice0.getMessage() == null) {
            throw new IllegalStateException(label + " 返回为空 message.content");
        }
        String content = choice0.getMessage().getContent();
        // 2xx + 有 choices/message 但 content 为空：返回空串，由引擎层降级提示
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        return content;
    }

    private String buildUrl(OpenAiCompatibleProperties props) {
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

    private OpenAiChatCompletionRequest buildRequestBody(AiAnalyzeReq req, OpenAiCompatibleProperties props) {
        OpenAiChatCompletionRequest request = new OpenAiChatCompletionRequest();
        request.setModel(pickFirstNotBlank(req == null ? null : req.getModel(), props.getModel()));
        request.setTemperature(req != null && req.getTemperature() != null
                ? req.getTemperature()
                : props.getTemperature());
        request.setMaxTokens(req != null && req.getMaxTokens() != null
                ? req.getMaxTokens()
                : props.getMaxTokens());

        List<OpenAiChatCompletionRequest.Message> messages = new ArrayList<>();

        OpenAiChatCompletionRequest.Message system = new OpenAiChatCompletionRequest.Message();
        system.setRole("system");
        system.setContent(AiPromptBuilder.buildSystemPrompt(req));
        messages.add(system);

        OpenAiChatCompletionRequest.Message user = new OpenAiChatCompletionRequest.Message();
        user.setRole("user");
        user.setContent(AiPromptBuilder.buildUserPrompt(req, objectMapper));
        messages.add(user);

        request.setMessages(messages);
        request.setStream(false);
        return request;
    }

    private String pickFirstNotBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b;
    }
}
