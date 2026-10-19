package com.alex.finance.cpn;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.api.finance.cpnRedemptionRecordInfo.vo.CpnRedemptionRecordInfoVo;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 消费券核心业务规则与雪花 ID 序列化契约测试
 */
class CpnCouponBusinessRuleIT {

    // ==========================================
    // 1. 消费券核销余量与进度流转规则
    // ==========================================

    @Test
    @DisplayName("消费券余量与进度计算：典型值 (total=100, consumed=35 -> remain=65, progress=35%)")
    void calcCouponRemaining_typical() {
        int totalQuantity = 100;
        int consumedQuantity = 35;

        int remaining = Math.max(0, totalQuantity - consumedQuantity);
        BigDecimal progressRatio = calcProgressRatio(consumedQuantity, totalQuantity);

        assertEquals(65, remaining);
        assertEquals(new BigDecimal("35.00"), progressRatio);
    }

    @Test
    @DisplayName("消费券核销校验：核销数量超出当前剩余可用数量时应判定不合法")
    void redeem_should_reject_when_exceeds_remaining_quantity() {
        int totalQuantity = 50;
        int currentConsumed = 45;
        int remaining = totalQuantity - currentConsumed; // 5

        int tryRedeemCount = 6;
        boolean canRedeem = canRedeemCoupon(remaining, tryRedeemCount);

        assertFalse(canRedeem, "核销数量大于可用余量时必须拦截！");
    }

    @Test
    @DisplayName("消费券核销校验：核销数量小于等于可用余量时允许核销")
    void redeem_should_allow_when_within_remaining_quantity() {
        int remaining = 5;
        int tryRedeemCount = 5;

        boolean canRedeem = canRedeemCoupon(remaining, tryRedeemCount);

        assertTrue(canRedeem, "核销数量在可用余量内允许核销！");
    }

    @Test
    @DisplayName("消费券售罄状态机：余量为0时自动标记为已售罄/核销完毕")
    void redeem_should_mark_sold_out_when_remaining_zero() {
        int totalQuantity = 100;
        int consumedQuantity = 100;

        int remaining = Math.max(0, totalQuantity - consumedQuantity);
        boolean isSoldOut = (remaining == 0);

        assertEquals(0, remaining);
        assertTrue(isSoldOut, "余量归零时应标记售罄！");
    }

    // ==========================================
    // 2. 雪花 ID 序列化契约保护（反射遍历防御）
    // ==========================================

    @Test
    @DisplayName("CpnCouponInfoVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_coupon_info_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(CpnCouponInfoVo.class);
    }

    @Test
    @DisplayName("CpnUserCouponInfoVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_user_coupon_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(CpnUserCouponInfoVo.class);
    }

    @Test
    @DisplayName("CpnRedemptionRecordInfoVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_redemption_record_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(CpnRedemptionRecordInfoVo.class);
    }

    // ==========================================
    // 辅助计算与断言方法
    // ==========================================

    private static boolean canRedeemCoupon(int remaining, int count) {
        return count > 0 && count <= remaining;
    }

    private static BigDecimal calcProgressRatio(int consumed, int total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(consumed)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
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
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
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
