package com.alex.product.service;

import com.alex.api.product.vo.pmsShopProduct.ProductAiCopyReq;
import com.alex.api.product.vo.pmsShopProduct.ProductAiCopyVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class ProductAiServiceTest {

    private final ProductAiService productAiService = new ProductAiService(null);

    @Test
    @DisplayName("商品AI营销文案：降级规则生成测试")
    void testMarketingCopyFallback() {
        ProductAiCopyReq req = new ProductAiCopyReq()
                .setProductName("降噪无线蓝牙耳机")
                .setCategoryName("数码影音")
                .setOriginalPrice(new BigDecimal("399.00"))
                .setSeckillPrice(new BigDecimal("199.00"))
                .setTargetAudience("年轻白领与音乐发烧友")
                .setFeatures("主动混合降噪40dB，长续航30小时");

        ProductAiCopyVo vo = productAiService.generateMarketingCopy(req);

        Assertions.assertNotNull(vo);
        Assertions.assertTrue(vo.getTitle().contains("降噪无线蓝牙耳机"));
        Assertions.assertNotNull(vo.getSlogan());
        Assertions.assertNotNull(vo.getDiscountText());
        Assertions.assertTrue(vo.getDiscountText().contains("立省"));
        Assertions.assertFalse(vo.getSellingPoints().isEmpty());
        Assertions.assertTrue(vo.getMarketingDescription().contains("降噪无线蓝牙耳机"));
    }
}
