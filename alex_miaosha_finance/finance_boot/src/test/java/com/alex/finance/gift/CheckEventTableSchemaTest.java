package com.alex.finance.gift;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CheckEventTableSchemaTest {

    @Test
    public void checkSchema() throws Exception {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword("02700083-9fd9-4b82-a4b4-9177e0560e92");
        String username = encryptor.decrypt("wzGvorwuoFra8yDJA66Xfg==");
        String password = encryptor.decrypt("TQ2oVKN42O4FWPbyKH7mCHBwhNc4xNhLZa2IBDN93TI=");

        String url = "jdbc:mysql://115.190.181.243:3336/alex_finance?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("=== COLUMNS FOR gift_event_info_t ===");
            try (ResultSet rs = meta.getColumns(null, null, "gift_event_info_t", null)) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String typeName = rs.getString("TYPE_NAME");
                    int nullable = rs.getInt("NULLABLE");
                    String isNullable = rs.getString("IS_NULLABLE");
                    System.out.println(String.format("COLUMN: %-20s TYPE: %-15s NULLABLE: %s", colName, typeName, isNullable));
                }
            }

            System.out.println("=== RECENT ROWS FROM gift_event_info_t ===");
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT id, org_id, user_id, event_name, event_type, host_person_id, is_delete FROM gift_event_info_t ORDER BY id DESC LIMIT 5")) {
                while (rs.next()) {
                    System.out.println(String.format("id=%d, orgId=%d, userId=%d, name=%s, type=%s, hostId=%d, isDel=%d",
                            rs.getLong("id"), rs.getLong("org_id"), rs.getLong("user_id"),
                            rs.getString("event_name"), rs.getString("event_type"), rs.getLong("host_person_id"), rs.getInt("is_delete")));
                }
            }
        }
    }
}
