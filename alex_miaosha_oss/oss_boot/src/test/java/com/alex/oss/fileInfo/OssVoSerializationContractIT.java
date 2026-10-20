package com.alex.oss.fileInfo;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OSS 文件信息 VO 雪花 ID 与审计字段序列化契约测试
 * 确保所有主外键 ID 均序列化为 String，彻底杜绝前端 JS 64 位大数精度丢失
 */
class OssVoSerializationContractIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("反射契约锁：FileInfoVo 及其基类 BaseVo 所有 Long 类型 ID 字段必须标注 @JsonSerialize(using = Long2StringSerializer.class)")
    void fileInfoVo_and_baseVo_ids_mustHaveJsonSerializeAnnotation() {
        Set<Field> longFields = new HashSet<>();
        collectLongFields(FileInfoVo.class, longFields);

        assertFalse(longFields.isEmpty(), "FileInfoVo 及其基类必须包含 Long 字段");

        for (Field field : longFields) {
            String fieldName = field.getName();
            // fileSize 表示文件字节数，需保留纯数字语义，无需转字符串
            if ("fileSize".equalsIgnoreCase(fieldName)) {
                continue;
            }

            JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);
            assertNotNull(annotation,
                    String.format("VO 字段 [%s.%s] 缺失 @JsonSerialize 注解，存在前端 JS 精度截断风险！",
                            field.getDeclaringClass().getSimpleName(), fieldName));

            assertEquals(Long2StringSerializer.class, annotation.using(),
                    String.format("VO 字段 [%s.%s] 的 @JsonSerialize 必须使用 Long2StringSerializer.class",
                            field.getDeclaringClass().getSimpleName(), fieldName));
        }
    }

    @Test
    @DisplayName("JSON 序列化验证：雪花 ID（id/creator/updater/operator/deleter）序列化为字符串，fileSize 维持数值")
    void fileInfoVo_jsonSerialization_precisionSafety() throws Exception {
        FileInfoVo vo = new FileInfoVo();
        vo.setId(1845123456789012345L);
        vo.setCreator(1845123456789012346L);
        vo.setUpdater(1845123456789012347L);
        vo.setOperator(1845123456789012348L);
        vo.setDeleter(1845123456789012349L);
        vo.setFileSize(2097152L);
        vo.setFileName("contract.pdf");
        vo.setBucketName("finance-bucket");

        String json = objectMapper.writeValueAsString(vo);

        // 强断言雪花 ID 全部带双引号（String 契约）
        assertTrue(json.contains("\"id\":\"1845123456789012345\""), "id 必须序列化为字符串");
        assertTrue(json.contains("\"creator\":\"1845123456789012346\""), "creator 必须序列化为字符串");
        assertTrue(json.contains("\"updater\":\"1845123456789012347\""), "updater 必须序列化为字符串");
        assertTrue(json.contains("\"operator\":\"1845123456789012348\""), "operator 必须序列化为字符串");
        assertTrue(json.contains("\"deleter\":\"1845123456789012349\""), "deleter 必须序列化为字符串");

        // 强断言 fileSize 不带双引号（维持数字语义）
        assertTrue(json.contains("\"fileSize\":2097152"), "fileSize 必须维持数值类型");
    }

    private void collectLongFields(Class<?> clazz, Set<Field> collected) {
        if (clazz == null || Object.class.equals(clazz)) {
            return;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Long.class.equals(field.getType()) || long.class.equals(field.getType())) {
                collected.add(field);
            }
        }
        collectLongFields(clazz.getSuperclass(), collected);
    }
}
