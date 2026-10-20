package com.alex.ai.stream;

import com.alex.ai.config.AiProperties;
import com.alex.ai.controller.AiAnalyzeController;
import com.alex.ai.service.AiAnalyzeService;
import com.alex.api.ai.vo.AiAnalyzeReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 集成测试套件 2:
 * AI 流式会话背压控制、并发限制与断开取消联动测试
 */
@ExtendWith(MockitoExtension.class)
class AiStreamBackpressureAndCancellationIT {

    @Mock
    private AiAnalyzeService aiAnalyzeService;

    private AiStreamBackpressureManager backpressureManager;
    private AiAnalyzeController controller;

    @BeforeEach
    void setUp() {
        backpressureManager = new AiStreamBackpressureManager(2); // 上限 2
        AiProperties properties = new AiProperties();
        AiProperties.Stream stream = new AiProperties.Stream();
        stream.setReadTimeoutMs(10000);
        properties.setStream(stream);

        controller = new AiAnalyzeController(aiAnalyzeService, properties, backpressureManager);
    }

    @Test
    @DisplayName("背压限流: 单用户达到最大并发数时拒绝新流，释放后可重新接入")
    void backpressure_limitsConcurrentStreamsPerUser() {
        String userId = "user-10086";

        assertTrue(backpressureManager.tryAcquire(userId), "第 1 个流应成功");
        assertEquals(1, backpressureManager.getActiveCount(userId));

        assertTrue(backpressureManager.tryAcquire(userId), "第 2 个流应成功");
        assertEquals(2, backpressureManager.getActiveCount(userId));

        assertFalse(backpressureManager.tryAcquire(userId), "超过上限 (2) 时应触发背压拒绝");

        // 释放 1 个
        backpressureManager.release(userId);
        assertEquals(1, backpressureManager.getActiveCount(userId));

        // 重新获取
        assertTrue(backpressureManager.tryAcquire(userId), "释放后应能重新获取");
        assertEquals(2, backpressureManager.getActiveCount(userId));
    }

    @Test
    @DisplayName("用户隔离: 不同用户之间的流并发配额相互独立")
    void backpressure_differentUsersHaveIndependentQuotas() {
        String userA = "user-A";
        String userB = "user-B";

        assertTrue(backpressureManager.tryAcquire(userA));
        assertTrue(backpressureManager.tryAcquire(userA));
        assertFalse(backpressureManager.tryAcquire(userA));

        // userB 依然可以正常获取
        assertTrue(backpressureManager.tryAcquire(userB));
        assertTrue(backpressureManager.tryAcquire(userB));
        assertFalse(backpressureManager.tryAcquire(userB));
    }

    @Test
    @DisplayName("取消感知状态: AiSseEventWriter 标记取消后 isCancelled 必须为 true")
    void writer_markCancelled_reflectsState() {
        SseEmitter emitter = new SseEmitter();
        AiSseEventWriter writer = new AiSseEventWriter(emitter);

        assertFalse(writer.isCancelled());

        writer.markCancelled();
        assertTrue(writer.isCancelled(), "markCancelled 后 isCancelled 必须返回 true");
    }

    @Test
    @DisplayName("控制器背压拦截: 并发超额时控制器直接返回 429 错误并不调用分析服务")
    void controller_chatStream_triggersBackpressure_whenQuotaFull() {
        String userId = "busy-user";
        // 占满该用户的配额
        assertTrue(backpressureManager.tryAcquire(userId));
        assertTrue(backpressureManager.tryAcquire(userId));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContext(Map.of("userId", userId));
        req.setContent("请继续分析");

        SseEmitter emitter = controller.chatStream(req);
        assertNotNull(emitter);

        // 验证没有向下游分析服务派发任务
        verify(aiAnalyzeService, never()).analyzeStream(any(), any());
    }
}
