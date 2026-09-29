package com.alex.product.service;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.product.vo.pmsShopProduct.ProductAiCopyReq;
import com.alex.api.product.vo.pmsShopProduct.ProductAiCopyVo;
import com.alex.base.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 商品/秒杀 AI 文案生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAiService {

    private final AiAnalyzeApi aiAnalyzeApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductAiCopyVo generateMarketingCopy(ProductAiCopyReq req) {
        ProductAiCopyVo copyVo = buildFallbackCopy(req);

        if (aiAnalyzeApi == null) {
            return copyVo;
        }

        try {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("productName", req != null ? req.getProductName() : null);
            ctx.put("categoryName", req != null ? req.getCategoryName() : null);
            ctx.put("originalPrice", req != null ? req.getOriginalPrice() : null);
            ctx.put("seckillPrice", req != null ? req.getSeckillPrice() : null);
            ctx.put("targetAudience", req != null ? req.getTargetAudience() : null);
            ctx.put("features", req != null ? req.getFeatures() : null);

            String prompt = String.format(
                    "作为顶级电商秒杀与新媒体种草文案专家，请为以下商品撰写极具吸引力的高转化营销文案：\n" +
                    "- 商品名称：%s\n" +
                    "- 分类：%s\n" +
                    "- 原价：%s 元，秒杀促销价：%s 元\n" +
                    "- 目标人群：%s\n" +
                    "- 核心亮点/参数：%s\n" +
                    "请以标准 JSON 格式输出，包含以下字段：title(30字以内爆款商品标题), slogan(一句话营销吸睛口号), sellingPoints(数组，3-4条核心卖点简短有力), marketingDescription(150字左右的商品种草转化详情), discountText(折扣力度说明)。只需返回JSON，不要markdown。",
                    (req != null && StringUtils.hasText(req.getProductName())) ? req.getProductName() : "精选爆款商品",
                    (req != null && StringUtils.hasText(req.getCategoryName())) ? req.getCategoryName() : "品质好物",
                    (req != null && req.getOriginalPrice() != null) ? req.getOriginalPrice() : "99",
                    (req != null && req.getSeckillPrice() != null) ? req.getSeckillPrice() : "49",
                    (req != null && StringUtils.hasText(req.getTargetAudience())) ? req.getTargetAudience() : "年轻大众与品质追求者",
                    (req != null && StringUtils.hasText(req.getFeatures())) ? req.getFeatures() : "高性价比、品质出众"
            );

            AiAnalyzeReq aiReq = new AiAnalyzeReq();
            aiReq.setBizType("product-ai-copy");
            aiReq.setContent(prompt);
            aiReq.setContext(ctx);

            Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(aiReq);
            if (result != null && result.getData() != null && StringUtils.hasText(result.getData().getSummary())) {
                String text = result.getData().getSummary().trim();
                if (text.startsWith("```json")) {
                    text = text.substring(7);
                } else if (text.startsWith("```")) {
                    text = text.substring(3);
                }
                if (text.endsWith("```")) {
                    text = text.substring(0, text.length() - 3);
                }
                text = text.trim();
                JsonNode root = objectMapper.readTree(text);
                if (root != null) {
                    if (root.hasNonNull("title")) copyVo.setTitle(root.get("title").asText());
                    if (root.hasNonNull("slogan")) copyVo.setSlogan(root.get("slogan").asText());
                    if (root.hasNonNull("marketingDescription")) copyVo.setMarketingDescription(root.get("marketingDescription").asText());
                    if (root.hasNonNull("discountText")) copyVo.setDiscountText(root.get("discountText").asText());
                    if (root.has("sellingPoints") && root.get("sellingPoints").isArray()) {
                        List<String> points = new ArrayList<>();
                        for (JsonNode node : root.get("sellingPoints")) {
                            if (node != null && StringUtils.hasText(node.asText())) {
                                points.add(node.asText());
                            }
                        }
                        if (!points.isEmpty()) {
                            copyVo.setSellingPoints(points);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("ProductAiService AI copy call degraded to fallback: {}", ex.getMessage());
        }

        return copyVo;
    }

    public ProductAiCopyVo buildFallbackCopy(ProductAiCopyReq req) {
        String name = (req != null && StringUtils.hasText(req.getProductName())) ? req.getProductName().trim() : "精选品质商品";
        String category = (req != null && StringUtils.hasText(req.getCategoryName())) ? req.getCategoryName().trim() : "全品类";
        BigDecimal orig = (req != null && req.getOriginalPrice() != null) ? req.getOriginalPrice() : BigDecimal.ZERO;
        BigDecimal seckill = (req != null && req.getSeckillPrice() != null) ? req.getSeckillPrice() : BigDecimal.ZERO;

        String discountText = "限时狂欢秒杀，特惠抢购中！";
        if (orig.compareTo(BigDecimal.ZERO) > 0 && seckill.compareTo(BigDecimal.ZERO) > 0 && orig.compareTo(seckill) > 0) {
            BigDecimal diff = orig.subtract(seckill);
            BigDecimal rate = seckill.divide(orig, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("10"));
            discountText = String.format("限时立省 %s 元，低至 %s 折超值疯抢！", diff.stripTrailingZeros().toPlainString(), rate.stripTrailingZeros().toPlainString());
        }

        String title = String.format("【限时秒杀】%s %s 旗舰爆款 极速发货", category, name);
        String slogan = "火爆疯抢 · 破盘特惠 · 限量秒杀手慢无！";

        List<String> points = new ArrayList<>();
        points.add("官方正品保障：严苛质检，售后无忧退换");
        points.add("极致性价比让利：秒杀狂降，闭眼入不踩雷");
        if (req != null && StringUtils.hasText(req.getFeatures())) {
            points.add("核心优势：" + req.getFeatures().trim());
        } else {
            points.add("出众性能与质感：专为品质生活与高频使用场景量身定制");
        }

        String desc = String.format(
                "热销推荐！%s 凭借精湛做工与优秀口碑广受好评。本次平台专属秒杀直降让利，数量有限先到先得。不仅拥有高颜值外观，更在日常使用中展现持久稳定的卓越性能，立即加购抢先体验！",
                name
        );

        return new ProductAiCopyVo()
                .setTitle(title)
                .setSlogan(slogan)
                .setSellingPoints(points)
                .setMarketingDescription(desc)
                .setDiscountText(discountText);
    }
}
