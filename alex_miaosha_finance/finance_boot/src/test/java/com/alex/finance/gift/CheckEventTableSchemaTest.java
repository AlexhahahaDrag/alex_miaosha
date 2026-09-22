package com.alex.finance.gift;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CheckEventTableSchemaTest {

    @Test
    public void checkSchema() throws Exception {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword("02700083-9fd9-4b82-a4b4-9177e0560e92");
        String username = encryptor.decrypt("wzGvorwuoFra8yDJA66Xfg==");
        String password = encryptor.decrypt("TQ2oVKN42O4FWPbyKH7mCHBwhNc4xNhLZa2IBDN93TI=");

        String financeUrl = "jdbc:mysql://115.190.181.243:3336/alex_finance?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String userUrl = "jdbc:mysql://115.190.181.243:3336/alex_user?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        try (Connection conn = DriverManager.getConnection(userUrl, username, password)) {
            System.out.println("=== USER 2 INFO IN alex_user ===");
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT u.id, u.username, ou.org_id as active_org_id FROM t_user u LEFT JOIN t_org_user_info ou ON u.id = ou.user_id AND ou.status = 1 WHERE u.id = 2")) {
                while (rs.next()) {
                    System.out.println(String.format("User: id=%d, name=%s, active_org_id=%d",
                            rs.getLong("id"), rs.getString("username"), rs.getLong("active_org_id")));
                }
            }
        }

        try (Connection conn = DriverManager.getConnection(financeUrl, username, password)) {
            System.out.println("=== DING NINGMING IN gift_person_info_t ===");
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT id, org_id, user_id, person_name, is_delete FROM gift_person_info_t WHERE id = 2102188550459902401")) {
                while (rs.next()) {
                    System.out.println(String.format("Person: id=%d, orgId=%d, userId=%d, name=%s, isDel=%d",
                            rs.getLong("id"), rs.getLong("org_id"), rs.getLong("user_id"), rs.getString("person_name"), rs.getInt("is_delete")));
                }
            }

            System.out.println("=== gift_event_info_t CONSTRAINTS ===");
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = 'alex_finance' AND TABLE_NAME = 'gift_event_info_t'")) {
                while (rs.next()) {
                    System.out.println(String.format("Constraint: name=%s, type=%s", rs.getString(1), rs.getString(2)));
                }
            }

            System.out.println("=== TRY INSERT SIMULATION ===");
            String testSql = "INSERT INTO gift_event_info_t (id, org_id, user_id, event_name, event_type, event_time, host_person_id, is_delete, creator, create_time, operator, operate_time) " +
                    "VALUES (2102188550459909999, 1, 2, '丁宁明结婚', 'WEDDING', '2026-09-06 00:00:00', 2102188550459902401, 0, 2, NOW(), 2, NOW())";
            try (var stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                int count = stmt.executeUpdate(testSql);
                System.out.println("Direct insert succeeded: count=" + count);
                conn.rollback();
            } catch (Exception e) {
                System.out.println("Direct insert FAILED: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
