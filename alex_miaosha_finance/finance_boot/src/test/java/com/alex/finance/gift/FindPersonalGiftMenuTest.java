package com.alex.finance.gift;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class FindPersonalGiftMenuTest {

    @Test
    public void findMenu() throws Exception {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword("02700083-9fd9-4b82-a4b4-9177e0560e92");
        String username = encryptor.decrypt("wzGvorwuoFra8yDJA66Xfg==");
        String password = encryptor.decrypt("TQ2oVKN42O4FWPbyKH7mCHBwhNc4xNhLZa2IBDN93TI=");

        String url = "jdbc:mysql://115.190.181.243:3336/alex_user?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT id, permission_code, permission_name, parent_id, is_delete FROM t_permission_info WHERE id = 1810856883473846273 OR parent_id = 1810856883473846273";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    System.out.printf("FOUND_PERMISSION -> id: %s, code: %s, name: %s, parent_id: %s, is_delete: %s%n",
                            rs.getString("id"),
                            rs.getString("permission_code"),
                            rs.getString("permission_name"),
                            rs.getString("parent_id"),
                            rs.getString("is_delete"));
                }
            }

            String rpSql = "SELECT rp.id, rp.role_id, rp.permission_id FROM t_role_permission_info rp WHERE rp.permission_id IN (SELECT id FROM t_permission_info WHERE id = 1810856883473846273 OR parent_id = 1810856883473846273)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(rpSql)) {
                while (rs.next()) {
                    System.out.printf("FOUND_ROLE_PERMISSION -> id: %s, role_id: %s, permission_id: %s%n",
                            rs.getString("id"),
                            rs.getString("role_id"),
                            rs.getString("permission_id"));
                }
            }
        }
    }
}
