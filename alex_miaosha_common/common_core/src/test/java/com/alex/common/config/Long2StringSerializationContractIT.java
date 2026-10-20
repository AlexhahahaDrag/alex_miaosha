package com.alex.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试：前后端防 JS 精度丢失之 Long2StringSerializer 序列化契约与 null 安全验证
 */
class Long2StringSerializationContractIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class SnowflakeModel {
        @JsonSerialize(using = Long2StringSerializer.class)
        private Long id;

        @JsonSerialize(using = Long2StringSerializer.class)
        private Long parentId;

        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ContainerModel {
        private String title;
        private List<SnowflakeModel> items;
    }

    @Test
    @DisplayName("Long2StringSerializer: 19位雪花算法ID序列化为带引号字符串，杜绝前端精度截断")
    void testSnowflakeIdSerializedToString() throws JsonProcessingException {
        // 大于 JavaScript Number.MAX_SAFE_INTEGER (9007199254740991L) 的雪花 ID
        long snowflakeId = 1845678901234567890L;
        SnowflakeModel model = new SnowflakeModel(snowflakeId, 1845678901234567800L, "测试节点");

        String json = objectMapper.writeValueAsString(model);

        // 验证 json 中包含带引号的纯字符串，而不是裸数字
        assertTrue(json.contains("\"id\":\"1845678901234567890\""), "19位主键必须序列化为带双引号字符串");
        assertTrue(json.contains("\"parentId\":\"1845678901234567800\""), "外键必须序列化为带双引号字符串");
        assertFalse(json.contains("\"id\":1845678901234567890"), "禁止输出为裸 Long 数字以防止前端 JS 截断");
    }

    @Test
    @DisplayName("Long2StringSerializer: null 值必须安全写入 null 而不抛出 NPE 或遗漏字段")
    void testNullValueHandling() throws JsonProcessingException {
        SnowflakeModel model = new SnowflakeModel(null, null, "空根节点");

        String json = objectMapper.writeValueAsString(model);

        assertTrue(json.contains("\"id\":null"), "null 字段必须显式序列化为 null");
        assertTrue(json.contains("\"parentId\":null"), "null 外键必须显式序列化为 null");
    }

    @Test
    @DisplayName("Long2StringSerializer: 0 与极小值边界下仍统一格式化为 String 保证契约一致性")
    void testZeroAndSmallValues() throws JsonProcessingException {
        SnowflakeModel model = new SnowflakeModel(0L, -1L, "边界节点");

        String json = objectMapper.writeValueAsString(model);

        assertTrue(json.contains("\"id\":\"0\""), "0L 必须统一转为 \"0\"");
        assertTrue(json.contains("\"parentId\":\"-1\""), "-1L 必须统一转为 \"-1\"");
    }

    @Test
    @DisplayName("Long2StringSerializer: 集合与嵌套容器场景下批量序列化稳定无丢失")
    void testCollectionAndNestedContainerSerialization() throws JsonProcessingException {
        SnowflakeModel item1 = new SnowflakeModel(1900000000000000001L, null, "Item 1");
        SnowflakeModel item2 = new SnowflakeModel(1900000000000000002L, 1900000000000000001L, "Item 2");

        ContainerModel container = new ContainerModel("容器数据", Arrays.asList(item1, item2));

        String json = objectMapper.writeValueAsString(container);

        assertTrue(json.contains("\"id\":\"1900000000000000001\""));
        assertTrue(json.contains("\"parentId\":null"));
        assertTrue(json.contains("\"id\":\"1900000000000000002\""));
        assertTrue(json.contains("\"parentId\":\"1900000000000000001\""));
    }
}
