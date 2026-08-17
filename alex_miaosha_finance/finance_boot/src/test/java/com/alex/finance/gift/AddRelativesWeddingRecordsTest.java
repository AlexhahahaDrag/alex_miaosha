package com.alex.finance.gift;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class AddRelativesWeddingRecordsTest {

    @Test
    public void addWeddingRecords() throws Exception {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword("02700083-9fd9-4b82-a4b4-9177e0560e92");
        String username = encryptor.decrypt("wzGvorwuoFra8yDJA66Xfg==");
        String password = encryptor.decrypt("TQ2oVKN42O4FWPbyKH7mCHBwhNc4xNhLZa2IBDN93TI=");

        String url = "jdbc:mysql://115.190.181.243:3336/alex_finance?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            conn.setAutoCommit(false);
            try {
                long orgId = 1L;
                long xiaoFengZiUserId = 1L;

                // 1. 获取小疯子档案 ID
                long xiaoFengZiPersonId = 0L;
                String queryPersonSql = "SELECT id FROM gift_person_info_t WHERE org_id = ? AND bind_user_id = ? AND is_delete = 0 LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(queryPersonSql)) {
                    ps.setLong(1, orgId);
                    ps.setLong(2, xiaoFengZiUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            xiaoFengZiPersonId = rs.getLong("id");
                        }
                    }
                }

                // 2. 获取【我们结婚啦】事件 ID
                long weddingEventId = 0L;
                String queryWeddingEvent = "SELECT id FROM gift_event_info_t WHERE org_id = ? AND event_name = '我们结婚啦' AND is_delete = 0 LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(queryWeddingEvent)) {
                    ps.setLong(1, orgId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            weddingEventId = rs.getLong("id");
                        }
                    }
                }

                System.out.println("小疯子档案 ID: " + xiaoFengZiPersonId + ", 我们结婚啦事件 ID: " + weddingEventId);

                // 3. 为 张恕维, 大姑 增加 1000 元随礼记录
                List<String> targetNames = Arrays.asList("张恕维", "大姑");
                BigDecimal amount = new BigDecimal("1000.00");
                Timestamp payTime = Timestamp.valueOf("2024-08-25 00:00:00");

                for (String name : targetNames) {
                    long personId = 0L;
                    String queryTarget = "SELECT id FROM gift_person_info_t WHERE org_id = ? AND person_name = ? AND is_delete = 0 LIMIT 1";
                    try (PreparedStatement ps = conn.prepareStatement(queryTarget)) {
                        ps.setLong(1, orgId);
                        ps.setString(2, name);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                personId = rs.getLong("id");
                            }
                        }
                    }

                    if (personId == 0L) {
                        System.out.println("未找到亲友档案: " + name);
                        continue;
                    }

                    // 检查是否已有该亲友在【我们结婚啦】的收礼记录
                    String checkSql = "SELECT id FROM gift_record_info_t WHERE org_id = ? AND event_id = ? AND direction = 'RECEIVE' AND giver_person_id = ? AND is_delete = 0";
                    Long existRecordId = null;
                    try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                        ps.setLong(1, orgId);
                        ps.setLong(2, weddingEventId);
                        ps.setLong(3, personId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                existRecordId = rs.getLong("id");
                            }
                        }
                    }

                    if (existRecordId != null) {
                        // 更新为 1000 元
                        String updateSql = "UPDATE gift_record_info_t SET amount = ?, pay_time = ?, update_time = NOW(), operator = ? WHERE id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                            ps.setBigDecimal(1, amount);
                            ps.setTimestamp(2, payTime);
                            ps.setLong(3, xiaoFengZiUserId);
                            ps.setLong(4, existRecordId);
                            ps.executeUpdate();
                            System.out.println("更新 " + name + " 已有记录 ID=" + existRecordId + " 金额为 ¥1000.00");
                        }
                    } else {
                        // 插入新记录
                        long newRecordId = IdWorker.getId();
                        String insertSql = "INSERT INTO gift_record_info_t (id, org_id, user_id, event_id, direction, amount, pay_time, giver_person_id, receiver_person_id, is_delete, creator, operator, create_time, operate_time) VALUES (?, ?, ?, ?, 'RECEIVE', ?, ?, ?, ?, 0, ?, ?, NOW(), NOW())";
                        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                            ps.setLong(1, newRecordId);
                            ps.setLong(2, orgId);
                            ps.setLong(3, xiaoFengZiUserId);
                            ps.setLong(4, weddingEventId);
                            ps.setBigDecimal(5, amount);
                            ps.setTimestamp(6, payTime);
                            ps.setLong(7, personId);
                            ps.setLong(8, xiaoFengZiPersonId);
                            ps.setLong(9, xiaoFengZiUserId);
                            ps.setLong(10, xiaoFengZiUserId);
                            ps.executeUpdate();
                            System.out.println("插入 " + name + " 新增记录 ID=" + newRecordId + " 金额为 ¥1000.00");
                        }
                    }
                }

                conn.commit();
                System.out.println("========== 添加完成 ==========");

                // 校验
                String checkSummary = "SELECT gp.person_name, r.amount, r.direction, r.pay_time FROM gift_record_info_t r JOIN gift_person_info_t gp ON r.giver_person_id = gp.id WHERE r.org_id = 1 AND r.event_id = " + weddingEventId + " AND gp.person_name IN ('张恕维', '大姑') AND r.is_delete = 0";
                ResultSet checkRs = conn.createStatement().executeQuery(checkSummary);
                while (checkRs.next()) {
                    System.out.println("亲友: " + checkRs.getString("person_name") + " | 方向: " + checkRs.getString("direction") + " | 金额: ¥" + checkRs.getBigDecimal("amount") + " | 时间: " + checkRs.getTimestamp("pay_time"));
                }

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
