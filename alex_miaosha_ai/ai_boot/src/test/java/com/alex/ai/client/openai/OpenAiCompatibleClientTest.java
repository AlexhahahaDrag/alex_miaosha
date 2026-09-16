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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompatibleClientTest {

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
    void chat_200_returnsContent() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"summary\\\":\\\"ok\\\"}\"}}]}"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("finance");
        req.setContent("analyze me");

        String content = client.chat(req, props, "DeepSeek");

        assertEquals("{\"summary\":\"ok\"}", content);

        RecordedRequest recorded = server.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(recorded);
        assertEquals("POST", recorded.getMethod());
        assertEquals("/v1/chat/completions", recorded.getPath());
        assertEquals("Bearer test-key", recorded.getHeader("Authorization"));
        assertTrue(recorded.getBody().readUtf8().contains("analyze me"));
    }

    @Test
    void chat_500_throwsWithProviderLabel() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("err"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("x");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.chat(req, props, "SenseNova"));
        assertTrue(ex.getMessage().contains("SenseNova"));
        assertTrue(ex.getMessage().contains("500") || ex.getMessage().contains("请求失败"));
    }

    @Test
    void chat_emptyChoices_throwsWithProviderLabel() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"choices\":[]}"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("x");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.chat(req, props, "DeepSeek"));
        assertTrue(ex.getMessage().contains("DeepSeek"));
        assertTrue(ex.getMessage().contains("choices"));
    }

    @Test
    void chat_blankContent_returnsEmptyString() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"   \"}}]}"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("x");

        String content = client.chat(req, props, "DeepSeek");
        assertEquals("", content);
    }

    @Test
    void chat_nullContent_returnsEmptyString() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null}}]}"));

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("x");

        String content = client.chat(req, props, "SenseNova");
        assertEquals("", content);
    }
}
