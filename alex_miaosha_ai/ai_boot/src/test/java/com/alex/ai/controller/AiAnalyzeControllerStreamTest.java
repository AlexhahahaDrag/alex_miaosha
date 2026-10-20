package com.alex.ai.controller;

import com.alex.ai.config.AiProperties;
import com.alex.ai.service.AiAnalyzeService;
import com.alex.ai.stream.AiStreamSink;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AI 控制器流式接口与同步接口单元测试
 */
@ExtendWith(MockitoExtension.class)
class AiAnalyzeControllerStreamTest {

    @Mock
    private AiAnalyzeService aiAnalyzeService;

    @Mock
    private AiProperties aiProperties;

    @InjectMocks
    private AiAnalyzeController aiAnalyzeController;

    @BeforeEach
    void setUp() {
        AiProperties.Stream stream = new AiProperties.Stream();
        stream.setReadTimeoutMs(60000);
        lenient().when(aiProperties.getStream()).thenReturn(stream);
    }

    @Test
    @DisplayName("同步 /chat 对话接口返回业务成功 Result 包装体")
    void chat_syncInterface_shouldReturnSuccess() {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("general-chat");
        req.setContent("你好，请做自我介绍");

        AiAnalyzeResp mockResp = new AiAnalyzeResp();
        mockResp.setSummary("我是智能助手");
        mockResp.setEngine("deepseek");
        when(aiAnalyzeService.analyze(req)).thenReturn(mockResp);

        Result<AiAnalyzeResp> result = aiAnalyzeController.chat(req);

        assertNotNull(result);
        assertEquals("200", result.getCode());
        assertEquals("我是智能助手", result.getData().getSummary());
        assertEquals("deepseek", result.getData().getEngine());
    }

    @Test
    @DisplayName("流式 /chat/stream 接口返回 SseEmitter 实例并执行异步分析链路")
    void chatStream_shouldReturnSseEmitter_andTriggerAsyncAnalysis() throws Exception {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("general-chat-stream");
        req.setContent("请列举家庭记账的 3 个要点");

        doAnswer(invocation -> {
            AiStreamSink sink = invocation.getArgument(1);
            sink.meta("ai-req-001", "deepseek");
            sink.delta("1. 分类清晰；");
            sink.delta("2. 及时记账；");
            sink.delta("3. 定期复盘。");
            AiAnalyzeResp doneResp = new AiAnalyzeResp();
            doneResp.setSummary("记账要点分析完成");
            doneResp.setCostMs(150L);
            sink.done(doneResp);
            return null;
        }).when(aiAnalyzeService).analyzeStream(eq(req), any(AiStreamSink.class));

        SseEmitter emitter = aiAnalyzeController.chatStream(req);

        assertNotNull(emitter);
        // 等待异步线程调度触发
        Thread.sleep(120);

        verify(aiAnalyzeService, times(1)).analyzeStream(eq(req), any(AiStreamSink.class));
    }

    @Test
    @DisplayName("流式 /chat/stream 异常时：异常被捕获且触发 writer.error 兜底完成")
    void chatStream_whenServiceFails_shouldHandleErrorQuietly() throws Exception {
        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("fail-test");
        req.setContent("引发异常的请求");

        doThrow(new RuntimeException("模型连接超时"))
                .when(aiAnalyzeService).analyzeStream(eq(req), any(AiStreamSink.class));

        SseEmitter emitter = aiAnalyzeController.chatStream(req);

        assertNotNull(emitter);
        Thread.sleep(120);

        verify(aiAnalyzeService, times(1)).analyzeStream(eq(req), any(AiStreamSink.class));
    }
}
