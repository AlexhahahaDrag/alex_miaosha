package com.alex.product.service;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.product.vo.pmsShopProduct.ProductAiCopyReq;
import com.alex.api.product.vo.pmsShopProduct.ProductAiCopyVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 商品秒杀 AI 文案生成服务双路由 (DeepSeek 主路由 + 本地规则降级) 集成测试
 */
@ExtendWith(MockitoExtension.class)
class ProductAiServiceDualRoutingIT {

    @Mock
    private AiAnalyzeApi aiAnalyzeApi;

    @InjectMocks
    private ProductAiService productAiService;

    @Test
    @DisplayName("主路由：AI调用成功并返回带Markdown代码块的JSON时，准确解析并填充全部VO字段")
    void ai_success_route_should_parse_markdown_json_and_fill_vo() {
        String mockAiMarkdownJson = "```json\n" +
                "{\n" +
                "  \"title\": \"【秒杀爆款】超清降噪无线耳机 音乐发烧友首选\",\n" +
                "  \"slogan\": \"戴上它，世界只剩你的音乐殿堂！\",\n" +
                "  \"sellingPoints\": [\"40dB主动混合降噪\", \"30小时超长续航\", \"定制高分子生物振膜\"],\n" +
                "  \"marketingDescription\": \"无论通勤差旅还是运动办公，一键开启静谧空间，尽享原汁原味高解析度天籁之声。\",\n" +
                "  \"discountText\": \"限时特惠，劲省200元！\"\n" +
                "}\n" +
                "```";

        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary(mockAiMarkdownJson);
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenReturn(Result.success(resp));

        ProductAiCopyReq req = new ProductAiCopyReq()
                .setProductName("超清降噪无线耳机")
                .setCategoryName("影音数码")
                .setOriginalPrice(new BigDecimal("399.00"))
                .setSeckillPrice(new BigDecimal("199.00"))
                .setTargetAudience("年轻白领与音乐发烧友")
                .setFeatures("主动混合降噪40dB，长续航30小时");

        ProductAiCopyVo vo = productAiService.generateMarketingCopy(req);

        assertNotNull(vo);
        assertEquals("【秒杀爆款】超清降噪无线耳机 音乐发烧友首选", vo.getTitle());
        assertEquals("戴上它，世界只剩你的音乐殿堂！", vo.getSlogan());
        assertEquals("限时特惠，劲省200元！", vo.getDiscountText());
        assertEquals("无论通勤差旅还是运动办公，一键开启静谧空间，尽享原汁原味高解析度天籁之声。", vo.getMarketingDescription());
        assertNotNull(vo.getSellingPoints());
        assertEquals(3, vo.getSellingPoints().size());
        assertTrue(vo.getSellingPoints().contains("40dB主动混合降噪"));
    }

    @Test
    @DisplayName("降级路由：Feign微服务熔断抛出 SystemException 时，无感降级到本地规则引擎")
    void feign_exception_should_degrade_to_rule_fallback_seamlessly() {
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class)))
                .thenThrow(new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "ai"));

        ProductAiCopyReq req = new ProductAiCopyReq()
                .setProductName("高端骨传导运动耳机")
                .setCategoryName("数码配件")
                .setOriginalPrice(new BigDecimal("599.00"))
                .setSeckillPrice(new BigDecimal("299.00"))
                .setTargetAudience("户外骑行与马拉松跑者")
                .setFeatures("IPX8级专业防水，轻量贴耳不压头");

        // 断言：服务绝不抛出 500 异常，而是平稳返回降级文案
        ProductAiCopyVo vo = assertDoesNotThrow(() -> productAiService.generateMarketingCopy(req));

        assertNotNull(vo);
        assertTrue(vo.getTitle().contains("高端骨传导运动耳机"));
        assertNotNull(vo.getSlogan());
        assertTrue(vo.getDiscountText().contains("立省"));
        assertNotNull(vo.getSellingPoints());
        assertFalse(vo.getSellingPoints().isEmpty());
        assertTrue(vo.getMarketingDescription().contains("高端骨传导运动耳机"));
    }

    @Test
    @DisplayName("容错测试：AI返回破损/畸变JSON时，安全保底并应用规则引擎")
    void malformed_json_from_ai_should_gracefully_fallback() {
        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary("{ malformed_json: not_a_valid_json ");
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenReturn(Result.success(resp));

        ProductAiCopyReq req = new ProductAiCopyReq()
                .setProductName("智能降温保温杯")
                .setOriginalPrice(new BigDecimal("99.00"))
                .setSeckillPrice(new BigDecimal("49.00"));

        ProductAiCopyVo vo = assertDoesNotThrow(() -> productAiService.generateMarketingCopy(req));

        assertNotNull(vo);
        assertTrue(vo.getTitle().contains("智能降温保温杯"));
        assertNotNull(vo.getSlogan());
        assertNotNull(vo.getDiscountText());
    }

    @Test
    @DisplayName("离线测试：FeignClient 为 null 场景下直接使用本地规则引擎兜底")
    void null_feign_client_should_return_fallback_directly() {
        ProductAiService offlineService = new ProductAiService(null);

        ProductAiCopyReq req = new ProductAiCopyReq()
                .setProductName("人体工学办公椅")
                .setOriginalPrice(new BigDecimal("899.00"))
                .setSeckillPrice(new BigDecimal("499.00"));

        ProductAiCopyVo vo = offlineService.generateMarketingCopy(req);

        assertNotNull(vo);
        assertTrue(vo.getTitle().contains("人体工学办公椅"));
        assertTrue(vo.getDiscountText().contains("立省 400 元"));
        assertTrue(vo.getMarketingDescription().contains("人体工学办公椅"));
    }

    @Test
    @DisplayName("七点法边界：请求参数全部为 null 或部分缺失时，规则引擎具备默认值容错兜底")
    void buildFallbackCopy_seven_point_boundary() {
        ProductAiService offlineService = new ProductAiService(null);

        // 1. 全部为 null
        ProductAiCopyVo emptyVo = offlineService.generateMarketingCopy(null);
        assertNotNull(emptyVo);
        assertTrue(emptyVo.getTitle().contains("精选品质商品"));

        // 2. 原价低于秒杀价（无负立省文案异常）
        ProductAiCopyReq reversePriceReq = new ProductAiCopyReq()
                .setProductName("特价数据线")
                .setOriginalPrice(new BigDecimal("10.00"))
                .setSeckillPrice(new BigDecimal("15.00"));
        ProductAiCopyVo reversePriceVo = offlineService.generateMarketingCopy(reversePriceReq);
        assertNotNull(reversePriceVo);
        assertEquals("限时狂欢秒杀，特惠抢购中！", reversePriceVo.getDiscountText());
    }
}
