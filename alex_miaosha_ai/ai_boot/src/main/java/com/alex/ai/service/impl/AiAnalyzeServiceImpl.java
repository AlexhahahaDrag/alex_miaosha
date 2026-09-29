package com.alex.ai.service.impl;

import com.alex.ai.engine.AiEngineRouter;
import com.alex.ai.service.AiAnalyzeService;
import com.alex.ai.stream.AiStreamSink;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.common.utils.redis.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * AI Agent：
 * AI 分析服务实现（含 Redis 语义指纹缓存、令牌桶限流与配额管理）
 */
@Slf4j
@Service
public class AiAnalyzeServiceImpl implements AiAnalyzeService {

    public static final String CACHE_PREFIX = "AiCacheKey:analyze:";
    public static final int CACHE_EXPIRE_SECONDS = 7200; // 2 hours
    public static final int LIMIT_PER_MINUTE = 15;
    public static final int LIMIT_PER_DAY = 150;

    private final AiEngineRouter aiEngineRouter;
    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;

    public AiAnalyzeServiceImpl(AiEngineRouter aiEngineRouter,
                                @Autowired(required = false) RedisUtils redisUtils,
                                @Autowired(required = false) ObjectMapper objectMapper) {
        this.aiEngineRouter = aiEngineRouter;
        this.redisUtils = redisUtils;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public AiAnalyzeResp analyze(AiAnalyzeReq req) {
        long start = System.currentTimeMillis();

        String content = req == null ? null : req.getContent();
        String bizType = req == null ? null : req.getBizType();

        String requestId = buildRequestId(bizType, content);
        String cacheKey = CACHE_PREFIX + buildCacheFingerprint(req);

        // 1. 尝试从 Redis 语义指纹缓存命中（缓存命中不受限流额度惩罚）
        if (redisUtils != null) {
            try {
                String cachedJson = redisUtils.get(cacheKey);
                if (StringUtils.isNotBlank(cachedJson)) {
                    AiAnalyzeResp cached = objectMapper.readValue(cachedJson, AiAnalyzeResp.class);
                    if (cached != null) {
                        cached.setRequestId(requestId);
                        cached.setEngine("redis-cache");
                        cached.setCostMs(System.currentTimeMillis() - start);
                        return cached;
                    }
                }
            } catch (Exception ex) {
                log.warn("Read AI cache failed for key {}: {}", cacheKey, ex.getMessage());
            }
        }

        // 2. 检查租户/用户级调用配额与分钟限流
        String userId = resolveUserId(req);
        if (isRateLimitOrQuotaExceeded(userId)) {
            log.info("AI rate limit or quota exceeded for user {}. Degrading to RuleBased engine.", userId);
            AiAnalyzeReq degradedReq = new AiAnalyzeReq();
            degradedReq.setBizType(bizType);
            degradedReq.setContent(content);
            degradedReq.setEngine("rule-based");

            AiAnalyzeResp degradedResp = aiEngineRouter.analyze(degradedReq, requestId, start);
            if (degradedResp != null) {
                degradedResp.setEngine("rule-based(quota-degraded)");
                String origSummary = degradedResp.getSummary() != null ? degradedResp.getSummary() : "";
                degradedResp.setSummary(origSummary + "\n\n[提示：当前 AI 使用频率较高或今日额度已达上限，已自动切换为轻量规则引擎]");
            }
            return degradedResp;
        }

        // 3. 未命中且未超额，由 AiEngineRouter 路由至大模型或规则引擎
        AiAnalyzeResp resp = aiEngineRouter.analyze(req, requestId, start);

        // 4. 结果有效时写入缓存
        if (redisUtils != null && resp != null && StringUtils.isNotBlank(resp.getSummary())) {
            try {
                String json = objectMapper.writeValueAsString(resp);
                redisUtils.setEx(cacheKey, json, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.warn("Write AI cache failed for key {}: {}", cacheKey, ex.getMessage());
            }
        }

        return resp;
    }

    @Override
    public void analyzeStream(AiAnalyzeReq req, AiStreamSink sink) {
        long start = System.currentTimeMillis();
        String content = req == null ? null : req.getContent();
        String bizType = req == null ? null : req.getBizType();
        String requestId = buildRequestId(bizType, content);
        aiEngineRouter.analyzeStream(req, requestId, start, sink);
    }

    public boolean isRateLimitOrQuotaExceeded(String userId) {
        if (redisUtils == null || StringUtils.isBlank(userId)) {
            return false;
        }
        try {
            // 分钟限流
            String minuteKey = "AiLimitKey:minute:" + userId;
            Long minuteCount = redisUtils.incrementWithExpire(null, minuteKey, 60, TimeUnit.SECONDS);
            if (minuteCount != null && minuteCount > LIMIT_PER_MINUTE) {
                return true;
            }

            // 单日配额
            String dayKey = "AiLimitKey:daily:" + userId + ":" + LocalDate.now();
            Long dayCount = redisUtils.incrementWithExpire(null, dayKey, 86400, TimeUnit.SECONDS);
            if (dayCount != null && dayCount > LIMIT_PER_DAY) {
                return true;
            }
        } catch (Exception ex) {
            log.warn("Check AI rate limit failed: {}", ex.getMessage());
        }
        return false;
    }

    private String resolveUserId(AiAnalyzeReq req) {
        if (req != null && req.getContext() != null) {
            Object uid = req.getContext().get("userId");
            if (uid != null && StringUtils.isNotBlank(String.valueOf(uid))) {
                return String.valueOf(uid);
            }
        }
        return "default-user";
    }

    public String buildCacheFingerprint(AiAnalyzeReq req) {
        if (req == null) {
            return "empty";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String bizType = req.getBizType() != null ? req.getBizType() : "default";
            String content = req.getContent() != null ? req.getContent().trim() : "";
            String contextJson = "";
            if (req.getContext() != null && !req.getContext().isEmpty()) {
                Map<String, Object> sorted = new TreeMap<>(req.getContext());
                contextJson = objectMapper.writeValueAsString(sorted);
            }
            String raw = bizType + "|" + content + "|" + contextJson;
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return bizType + ":" + sb.toString();
        } catch (Exception e) {
            return (req.getBizType() != null ? req.getBizType() : "default") + ":" +
                    Math.abs((req.getContent() != null ? req.getContent() : "").hashCode());
        }
    }

    private String buildRequestId(String bizType, String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String raw = (bizType == null ? "" : bizType) + "|" + (content == null ? "" : content) + "|" + Instant.now().toEpochMilli();
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            // 取前 12 bytes 足够做追踪（避免超长）
            StringBuilder sb = new StringBuilder("ai-");
            for (int i = 0; i < 12 && i < digest.length; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "ai-" + Instant.now().toEpochMilli();
        }
    }
}
