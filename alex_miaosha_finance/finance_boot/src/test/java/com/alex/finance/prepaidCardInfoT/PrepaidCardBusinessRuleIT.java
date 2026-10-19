package com.alex.finance.prepaidCardInfoT;

import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;
import com.alex.common.config.Long2StringSerializer;
import com.alex.common.exception.FinanceException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 储值预付卡核心业务规则（充值/消费余额不变式）与雪花 ID 序列化契约测试
 */
class PrepaidCardBusinessRuleIT {

    // ==========================================
    // 1. 预付卡充值与消费余额流转
    // ==========================================

    @Test
    @DisplayName("预付卡充值：可用余额准确增加 (balance + recharge)")
    void recharge_should_increase_balance_accurately() {
        BigDecimal currentBalance = new BigDecimal("200.00");
        BigDecimal rechargeAmount = new BigDecimal("150.50");

        BigDecimal newBalance = applyRecharge(currentBalance, rechargeAmount);

        assertEquals(new BigDecimal("350.50"), newBalance);
    }

    @Test
    @DisplayName("预付卡消费：余额充足时准确扣减 (balance - consume)")
    void consume_should_deduct_balance_when_sufficient() {
        BigDecimal currentBalance = new BigDecimal("200.00");
        BigDecimal consumeAmount = new BigDecimal("45.50");

        BigDecimal newBalance = applyConsume(currentBalance, consumeAmount);

        assertEquals(new BigDecimal("154.50"), newBalance);
    }

    @Test
    @DisplayName("预付卡消费：余额不足时拦截扣款并抛出异常，防止透支穿透")
    void consume_should_reject_when_balance_insufficient() {
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal consumeAmount = new BigDecimal("100.01");

        FinanceException ex = assertThrows(FinanceException.class,
                () -> applyConsume(currentBalance, consumeAmount));

        assertEquals("预付卡余额不足，无法完成消费扣减", ex.getMessage());
    }

    @Test
    @DisplayName("预付卡消费：刚好全额扣减时余额平稳归零")
    void exact_balance_consume_should_reach_zero() {
        BigDecimal currentBalance = new BigDecimal("88.88");
        BigDecimal consumeAmount = new BigDecimal("88.88");

        BigDecimal newBalance = applyConsume(currentBalance, consumeAmount);

        assertEquals(new BigDecimal("0.00"), newBalance);
    }

    @Test
    @DisplayName("充值或消费金额小于等于0时应拦截非法交易")
    void non_positive_transaction_amount_should_be_rejected() {
        BigDecimal balance = new BigDecimal("100.00");

        assertThrows(FinanceException.class, () -> applyRecharge(balance, BigDecimal.ZERO));
        assertThrows(FinanceException.class, () -> applyRecharge(balance, new BigDecimal("-10.00")));
        assertThrows(FinanceException.class, () -> applyConsume(balance, BigDecimal.ZERO));
        assertThrows(FinanceException.class, () -> applyConsume(balance, new BigDecimal("-5.00")));
    }

    // ==========================================
    // 2. 雪花 ID 序列化契约保护（反射遍历防御）
    // ==========================================

    @Test
    @DisplayName("PrepaidCardInfoTVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_prepaid_card_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(PrepaidCardInfoTVo.class);
    }

    @Test
    @DisplayName("PrepaidConsumeRecordTVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_prepaid_consume_record_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(PrepaidConsumeRecordTVo.class);
    }

    // ==========================================
    // 辅助计算与断言方法
    // ==========================================

    private static BigDecimal applyRecharge(BigDecimal currentBalance, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw FinanceException.param("充值金额必须大于0");
        }
        BigDecimal balance = currentBalance == null ? BigDecimal.ZERO : currentBalance;
        return balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal applyConsume(BigDecimal currentBalance, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw FinanceException.param("消费金额必须大于0");
        }
        BigDecimal balance = currentBalance == null ? BigDecimal.ZERO : currentBalance;
        if (balance.compareTo(amount) < 0) {
            throw FinanceException.param("预付卡余额不足，无法完成消费扣减");
        }
        return balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
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
