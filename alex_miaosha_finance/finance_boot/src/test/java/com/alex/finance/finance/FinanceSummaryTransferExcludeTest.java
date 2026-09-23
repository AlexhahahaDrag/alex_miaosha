package com.alex.finance.finance;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceSummaryTransferExcludeTest {

    @Test
    void testTransferExclusionFigures() throws Exception {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword("02700083-9fd9-4b82-a4b4-9177e0560e92");
        String username = encryptor.decrypt("wzGvorwuoFra8yDJA66Xfg==");
        String password = encryptor.decrypt("TQ2oVKN42O4FWPbyKH7mCHBwhNc4xNhLZa2IBDN93TI=");

        String url = "jdbc:mysql://115.190.181.243:3336/alex_finance?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             var stmt = conn.createStatement()) {

            // 1. 全量账单（含转账）
            long totalAll = 0L;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(1) AS c FROM finance_info WHERE is_delete = 0")) {
                if (rs.next()) {
                    totalAll = rs.getLong("c");
                }
            }

            // 2. 剔除转账后的日常账单
            long totalNoTransfer = 0L;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(1) AS c FROM finance_info WHERE is_delete = 0 AND (type_code IS NULL OR type_code != '转账')")) {
                if (rs.next()) {
                    totalNoTransfer = rs.getLong("c");
                }
            }

            // 3. 转账内部流水账单
            long totalTransferOnly = 0L;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(1) AS c FROM finance_info WHERE is_delete = 0 AND type_code = '转账'")) {
                if (rs.next()) {
                    totalTransferOnly = rs.getLong("c");
                }
            }

            // 校验数学守恒：全量 = 剔除转账 + 转账流水
            assertEquals(totalAll, totalNoTransfer + totalTransferOnly);
            assertTrue(totalNoTransfer > 0, "日常账单数应大于0");
            assertTrue(totalTransferOnly > 0, "转账流水数应大于0");
        }
    }
}
