package com.alex.ai.stream;

import com.alex.api.ai.vo.AiAnalyzeResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 阶段三十：AI SSE 流式事件生命周期与断连回收集成测试
 */
@ExtendWith(MockitoExtension.class)
public class AiSseStreamLifecycleIT {

    @Mock
    private SseEmitter sseEmitter;

    private AiSseEventWriter writer;

    @BeforeEach
    void setUp() {
        writer = new AiSseEventWriter(sseEmitter);
    }

    @Test
    @DisplayName("流式正常生命周期：依次推送 meta -> 增量 delta -> done，并最终触发 complete")
    void testNormalLifecycleSendsMetaDeltaDoneAndCompletes() throws Exception {
        writer.meta("req-stream-001", "deepseek:deepseek-chat");
        writer.delta("你好，");
        writer.delta("这是第一份要点分析。");

        AiAnalyzeResp doneResp = new AiAnalyzeResp();
        doneResp.setRequestId("req-stream-001");
        doneResp.setSummary("这是第一份要点分析。");
        doneResp.setKeyPoints(List.of("要点一"));
        doneResp.setEngine("deepseek:deepseek-chat");

        writer.done(doneResp);

        // 验证发送了 4 次事件 (1 meta + 2 delta + 1 done)
        verify(sseEmitter, times(4)).send(any(SseEmitter.SseEventBuilder.class));
        // 验证最终完成关闭流
        verify(sseEmitter, times(1)).complete();
    }

    @Test
    @DisplayName("流式错误中断生命周期：推送 error 事件后关闭流，后续 delta 被安全忽略")
    void testErrorLifecycleSendsErrorAndIgnoresSubsequentDeltas() throws Exception {
        writer.meta("req-stream-002", "sensenova:Nova-1.0");
        writer.error("500702", "上游大模型连接超时");

        // 后续迟到的 delta 应被拦截不再发送
        writer.delta("迟到的数据片段");

        // 验证只发送了 2 次 (1 meta + 1 error)
        verify(sseEmitter, times(2)).send(any(SseEmitter.SseEventBuilder.class));
        verify(sseEmitter, times(1)).complete();
    }

    @Test
    @DisplayName("客户端提前断开连接容错：底层抛出 IOException 时安全处理，不向外抛异常")
    void testClientDisconnectIOExceptionHandledGracefully() throws Exception {
        doThrow(new IOException("Broken pipe or client closed connection"))
                .when(sseEmitter).send(any(SseEmitter.SseEventBuilder.class));

        assertDoesNotThrow(() -> {
            writer.meta("req-stream-003", "deepseek:chat");
            writer.delta("测试断开");
        });

        // 验证通过 completeWithErrorQuietly 触发了错误收尾
        verify(sseEmitter, times(1)).completeWithError(any(IOException.class));
    }

    @Test
    @DisplayName("防重复关闭安全锁：done 之后再次调用 completeQuietly 或 completeWithError 不重复抛错")
    void testDoneAlreadyCompletedDoesNotDuplicateEmitterCall() throws Exception {
        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary("完成");

        writer.done(resp);
        writer.completeWithErrorQuietly(new RuntimeException("额外异常"));

        verify(sseEmitter, times(1)).complete();
        verify(sseEmitter, never()).completeWithError(any());
    }
}
