package com.alex.ai.client.openai;

import com.alex.ai.config.OpenAiCompatibleProperties;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompatibleClientStreamTest {

    private MockWebServer server;
    private OpenAiCompatibleClient client;
    private OpenAiCompatibleProperties props;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new OpenAiCompatibleClient(new ObjectMapper());

        props = new OpenAiCompatibleProperties();
        props.setBaseUrl(server.url("/").toString().replaceAll("/$", ""));
        props.setChatCompletionsPath("/v1/chat/completions");
        props.setApiKey("test-key");
        props.setModel("test-model");
        props.setTemperature(0.2);
        props.setMaxTokens(256);
        props.setTimeoutMs(5000);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void chatStream_twoDeltasAndDone_invokesOnDeltaTwiceAndOnComplete() throws Exception {
        String sseBody = ""
                + "data: {\"choices\":[{\"delta\":{\"content\":\"Hello\"}}]}\n\n"
                + "data: {\"choices\":[{\"delta\":{\"content\":\" world\"}}]}\n\n"
                + "data: [DONE]\n\n";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody(sseBody));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("finance");
        req.setContent("analyze me");

        List<String> deltas = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);

        client.chatStream(req, props, "DeepSeek", 120000,
                deltas::add,
                completeCount::incrementAndGet);

        assertEquals(List.of("Hello", " world"), deltas);
        assertEquals(1, completeCount.get());

        RecordedRequest recorded = server.takeRequest();
        assertNotNull(recorded);
        assertEquals("POST", recorded.getMethod());
        assertEquals("/v1/chat/completions", recorded.getPath());
        assertEquals("Bearer test-key", recorded.getHeader("Authorization"));
        assertTrue(recorded.getHeader("Accept").contains("text/event-stream"));
        String body = recorded.getBody().readUtf8();
        assertTrue(body.contains("\"stream\":true") || body.contains("\"stream\": true"));
    }

    @Test
    void chatStream_http500_throwsAndDoesNotCallOnComplete() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("err"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("x");

        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicInteger deltaCount = new AtomicInteger(0);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.chatStream(req, props, "SenseNova", 120000,
                        t -> deltaCount.incrementAndGet(),
                        () -> completed.set(true)));

        assertTrue(ex.getMessage().contains("SenseNova"));
        assertTrue(ex.getMessage().contains("500") || ex.getMessage().contains("请求失败"));
        assertFalse(completed.get());
        assertEquals(0, deltaCount.get());
    }

    @Test
    void chatStream_dataLineSplitAcrossChunks_stillParsesDelta() throws Exception {
        // 单行 data 被拆成多个 chunk（无中间 \n），依赖 lineCarry 拼接
        String dataLine = "data: {\"choices\":[{\"delta\":{\"content\":\"Hi\"}}]}\n";
        String doneLine = "data: [DONE]\n\n";
        String full = dataLine + doneLine;
        int chunkSize = Math.max(8, dataLine.length() / 3);
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setChunkedBody(full, chunkSize));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("split-line");

        List<String> deltas = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);

        client.chatStream(req, props, "DeepSeek", 120000,
                deltas::add,
                completeCount::incrementAndGet);

        assertEquals(List.of("Hi"), deltas);
        assertEquals(1, completeCount.get());
    }
}
