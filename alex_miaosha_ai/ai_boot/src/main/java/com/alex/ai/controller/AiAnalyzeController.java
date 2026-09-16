package com.alex.ai.controller;

import com.alex.ai.config.AiProperties;
import com.alex.ai.service.AiAnalyzeService;
import com.alex.ai.stream.AiSseEventWriter;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.common.Result;
import com.alex.common.annotations.LogRestRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * AI Agent：
 * AI 对话接口（对外路径 /chat；内部仍复用 AiAnalyze* 契约与实现）
 */
@Api(value = "AI 对话相关接口", tags = {"AI 对话相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/ai")
@Slf4j
public class AiAnalyzeController {

    private static final long DEFAULT_STREAM_TIMEOUT_MS = 120_000L;

    private final AiAnalyzeService aiAnalyzeService;
    private final AiProperties aiProperties;

    @LogRestRequest(apiName = "AI 对话")
    @ApiOperation(
            value = "AI 对话",
            notes = "对话入口；响应为结构化摘要（summary/keyPoints）。请求体与流式接口相同。",
            response = Result.class)
    @PostMapping("/chat")
    public Result<AiAnalyzeResp> chat(@RequestBody AiAnalyzeReq req) {
        return Result.success(aiAnalyzeService.analyze(req));
    }

    @LogRestRequest(apiName = "AI 流式对话")
    @ApiOperation(
            value = "AI 流式对话",
            notes = "SSE 推送 meta/delta/done/error；与 /chat 请求体相同。done 为结构化摘要。")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiAnalyzeReq req) {
        long timeout = resolveStreamTimeoutMs();
        SseEmitter emitter = new SseEmitter(timeout);
        AiSseEventWriter writer = new AiSseEventWriter(emitter);

        emitter.onTimeout(() -> {
            log.warn("AI chat stream timed out after {} ms", timeout);
            writer.completeWithErrorQuietly(new IllegalStateException("SSE timeout"));
        });
        emitter.onError(ex -> log.warn("AI chat stream connection error: {}",
                ex == null ? "" : ex.getMessage()));

        CompletableFuture.runAsync(() -> {
            try {
                aiAnalyzeService.analyzeStream(req, writer);
            } catch (Exception e) {
                log.error("AI chat stream failed: {}", e.getMessage(), e);
                try {
                    writer.error("500702", e.getMessage() == null ? "AI 流式对话失败" : e.getMessage());
                } catch (Exception sendEx) {
                    writer.completeWithErrorQuietly(e);
                }
            }
        });

        return emitter;
    }

    private long resolveStreamTimeoutMs() {
        if (aiProperties == null || aiProperties.getStream() == null) {
            return DEFAULT_STREAM_TIMEOUT_MS;
        }
        int configured = aiProperties.getStream().getReadTimeoutMs();
        return configured <= 0 ? DEFAULT_STREAM_TIMEOUT_MS : configured;
    }
}
