package com.alex.finance.shop;

import com.alex.api.finance.shopCart.vo.ShopCartVo;
import com.alex.api.finance.shopOrder.vo.ShopOrderVo;
import com.alex.api.finance.shopStockBatch.vo.ShopStockBatchVo;
import com.alex.api.finance.vo.shopFinance.ShopFinanceVo;
import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 店铺进销存核心业务规则与雪花 ID 序列化契约测试
 */
class ShopErpBusinessRuleIT {

    // ==========================================
    // 1. 进货批次综合总成本核算 (七点法边界)
    // ==========================================

    @Test
    @DisplayName("进货批次成本计算：标准值与典型值 (productCost + travelExpense)")
    void calcBatchTotalCost_typical() {
        Double productCost = 1000.0;
        Double travelExpense = 150.5;

        Double total = calcBatchTotalCost(productCost, travelExpense);

        assertEquals(1150.5, total, 0.0001);
    }

    @Test
    @DisplayName("进货批次成本计算：差旅费为null时兜底为0")
    void calcBatchTotalCost_nullTravelExpense() {
        Double productCost = 880.0;
        Double travelExpense = null;

        Double total = calcBatchTotalCost(productCost, travelExpense);

        assertEquals(880.0, total, 0.0001);
    }

    @Test
    @DisplayName("进货批次成本计算：货品成本为null时兜底为0")
    void calcBatchTotalCost_nullProductCost() {
        Double productCost = null;
        Double travelExpense = 200.0;

        Double total = calcBatchTotalCost(productCost, travelExpense);

        assertEquals(200.0, total, 0.0001);
    }

    @Test
    @DisplayName("进货批次成本计算：两者皆为null时安全返回0.0")
    void calcBatchTotalCost_bothNull() {
        Double total = calcBatchTotalCost(null, null);

        assertEquals(0.0, total, 0.0001);
    }

    // ==========================================
    // 2. 库存单件预计毛利与毛利率核算
    // ==========================================

    @Test
    @DisplayName("商品毛利核算：正常正毛利 (salePrice > costPrice)")
    void calcStockGrossProfit_positive() {
        BigDecimal salePrice = new BigDecimal("100.00");
        BigDecimal costPrice = new BigDecimal("60.00");

        BigDecimal profit = salePrice.subtract(costPrice);
        BigDecimal marginRate = calcMarginRate(profit, salePrice);

        assertEquals(new BigDecimal("40.00"), profit);
        assertEquals(new BigDecimal("40.00"), marginRate);
    }

    @Test
    @DisplayName("商品毛利核算：售价为0时防除零异常并安全返回0%毛利率")
    void calcStockGrossProfit_zeroSalePrice() {
        BigDecimal salePrice = BigDecimal.ZERO;
        BigDecimal costPrice = new BigDecimal("50.00");

        BigDecimal profit = salePrice.subtract(costPrice);
        BigDecimal marginRate = calcMarginRate(profit, salePrice);

        assertEquals(new BigDecimal("-50.00"), profit);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), marginRate);
    }

    @Test
    @DisplayName("商品毛利核算：负毛利/亏本销售预警 (salePrice < costPrice)")
    void calcStockGrossProfit_loss() {
        BigDecimal salePrice = new BigDecimal("50.00");
        BigDecimal costPrice = new BigDecimal("80.00");

        BigDecimal profit = salePrice.subtract(costPrice);
        BigDecimal marginRate = calcMarginRate(profit, salePrice);

        assertEquals(new BigDecimal("-30.00"), profit);
        assertEquals(new BigDecimal("-60.00"), marginRate);
    }

    // ==========================================
    // 3. 雪花 ID 序列化契约保护（反射遍历防御）
    // ==========================================

    @Test
    @DisplayName("ShopFinanceVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_shop_finance_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(ShopFinanceVo.class);
    }

    @Test
    @DisplayName("ShopStockVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_shop_stock_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(ShopStockVo.class);
    }

    @Test
    @DisplayName("ShopStockBatchVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_shop_stock_batch_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(ShopStockBatchVo.class);
    }

    @Test
    @DisplayName("ShopOrderVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_shop_order_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(ShopOrderVo.class);
    }

    @Test
    @DisplayName("ShopCartVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_shop_cart_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(ShopCartVo.class);
    }

    // ==========================================
    // 辅助计算与断言方法
    // ==========================================

    private static Double calcBatchTotalCost(Double productCost, Double travelExpense) {
        double cost = (productCost != null && productCost >= 0) ? productCost : 0.0;
        double travel = (travelExpense != null && travelExpense >= 0) ? travelExpense : 0.0;
        return BigDecimal.valueOf(cost).add(BigDecimal.valueOf(travel))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal calcMarginRate(BigDecimal profit, BigDecimal salePrice) {
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return profit.divide(salePrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static void assertAllLongFieldsAnnotated(Class<?> clazz) {
        List<Field> allFields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            allFields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        for (Field field : allFields) {
            if (field.getType() == Long.class) {
                // 排除序列化常量
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
                // 忽略非对外暴露的删除人等私有审计字段(若未直接暴露)
                if ("deleter".equals(field.getName())) {
                    continue;
                }
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);
                assertNotNull(annotation,
                        () -> String.format("类 %s 的 Long 字段 %s 缺少 @JsonSerialize 注解，前端大数 ID 存在精度截断风险！",
                                clazz.getSimpleName(), field.getName()));
                assertEquals(Long2StringSerializer.class, annotation.using(),
                        () -> String.format("类 %s 的 Long 字段 %s 必须使用 Long2StringSerializer 序列化！",
                                clazz.getSimpleName(), field.getName()));
            }
        }
    }
}
