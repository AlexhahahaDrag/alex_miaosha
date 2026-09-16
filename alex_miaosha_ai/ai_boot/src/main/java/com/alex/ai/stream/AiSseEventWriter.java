package com.alex.ai.stream;

import com.alex.api.ai.vo.AiAnalyzeResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 将 {@link AiStreamSink} 回调写入 {@link SseEmitter}（event: meta/delta/done/error）。
 */
@Slf4j
public class AiSseEventWriter implements AiStreamSink {

    private final SseEmitter emitter;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public AiSseEventWriter(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void meta(String requestId, String engine) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("requestId", requestId);
        payload.put("engine", engine);
        send("meta", payload);
    }

    @Override
    public void delta(String text) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", text == null ? "" : text);
        send("delta", payload);
    }

    @Override
    public void done(AiAnalyzeResp resp) {
        send("done", resp);
        completeQuietly();
    }

    @Override
    public void error(String code, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        send("error", payload);
        completeQuietly();
    }

    private void send(String name, Object data) {
        if (completed.get()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            log.warn("SSE send failed. event={}, err={}", name, e.getMessage());
            completeWithErrorQuietly(e);
        } catch (IllegalStateException e) {
            log.debug("SSE already completed. event={}", name);
        }
    }

    private void completeQuietly() {
        if (completed.compareAndSet(false, true)) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // already completed / disconnected
            }
        }
    }

    /**
     * 异步任务未走到 sink.done/error 时的兜底。
     */
    public void completeWithErrorQuietly(Throwable t) {
        if (completed.compareAndSet(false, true)) {
            try {
                emitter.completeWithError(t);
            } catch (Exception ignored) {
                // already completed / disconnected
            }
        }
    }
}
