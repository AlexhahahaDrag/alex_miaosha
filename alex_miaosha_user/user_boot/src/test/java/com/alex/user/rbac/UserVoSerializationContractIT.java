package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.roleOrgInfo.vo.RoleOrgInfoVo;
import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.api.user.tUserLogin.vo.TUserLoginVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户微服务全部 VO 雪花 ID 与审计字段序列化契约锁
 */
class UserVoSerializationContractIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<Class<?>> USER_VO_CLASSES = List.of(
            TUserVo.class,
            OrgInfoVo.class,
            MenuInfoVo.class,
            RoleInfoVo.class,
            PermissionInfoVo.class,
            TUserLoginVo.class,
            OrgUserInfoVo.class,
            RoleUserInfoVo.class,
            RoleOrgInfoVo.class,
            RolePermissionInfoVo.class,
            UserPermissionContextVo.class
    );

    @Test
    @DisplayName("反射契约锁：用户中心所有 VO 及其基类的 Long 类型 ID 字段必须标注 @JsonSerialize")
    void allUserVos_longIdFields_mustHaveJsonSerializeAnnotation() {
        for (Class<?> voClass : USER_VO_CLASSES) {
            Set<Field> fields = new HashSet<>();
            collectFields(voClass, fields);

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String fieldName = field.getName();
                // 数量统计类字段维持纯数值语义
                if ("boundUserCount".equalsIgnoreCase(fieldName) || "permissionCount".equalsIgnoreCase(fieldName)) {
                    continue;
                }

                // 1. 单个 Long 字段
                if (Long.class.equals(field.getType()) || long.class.equals(field.getType())) {
                    JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);
                    assertNotNull(annotation,
                            String.format("VO 字段 [%s.%s] 缺失 @JsonSerialize 注解，存在前端 JS 精度截断风险！",
                                    field.getDeclaringClass().getSimpleName(), fieldName));
                    assertEquals(Long2StringSerializer.class, annotation.using(),
                            String.format("VO 字段 [%s.%s] 的 @JsonSerialize 必须使用 Long2StringSerializer.class",
                                    field.getDeclaringClass().getSimpleName(), fieldName));
                }

                // 2. List<Long> 集合字段（如 roleIds, auxiliaryOrgIds）
                if (List.class.isAssignableFrom(field.getType())) {
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType pt) {
                        Type[] actualArgs = pt.getActualTypeArguments();
                        if (actualArgs.length == 1 && Long.class.equals(actualArgs[0])) {
                            JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);
                            assertNotNull(annotation,
                                    String.format("VO 集合字段 [%s.%s] 缺失 @JsonSerialize 注解！",
                                            field.getDeclaringClass().getSimpleName(), fieldName));
                            assertEquals(Long2StringSerializer.class, annotation.contentUsing(),
                                    String.format("VO 集合字段 [%s.%s] 必须使用 contentUsing = Long2StringSerializer.class",
                                            field.getDeclaringClass().getSimpleName(), fieldName));
                        }
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("OrgInfoVo 精度验证：id 与 parentId 均序列化为带引号 String")
    void orgInfoVo_jsonSerialization_precisionSafety() throws Exception {
        OrgInfoVo vo = new OrgInfoVo();
        vo.setId(1845123456789012345L);
        vo.setParentId(1845123456789012340L);
        vo.setOrgCode("ORG_ROOT");
        vo.setOrgName("总公司");

        String json = objectMapper.writeValueAsString(vo);

        assertTrue(json.contains("\"id\":\"1845123456789012345\""), "OrgInfoVo.id 必须序列化为字符串");
        assertTrue(json.contains("\"parentId\":\"1845123456789012340\""), "OrgInfoVo.parentId 必须序列化为字符串");
    }

    @Test
    @DisplayName("RoleInfoVo 字段验证：boundUserCount 与 permissionCount 维持数值类型")
    void roleInfoVo_counts_remainNumeric() throws Exception {
        RoleInfoVo vo = new RoleInfoVo();
        vo.setId(1001L);
        vo.setBoundUserCount(42L);
        vo.setPermissionCount(128L);

        String json = objectMapper.writeValueAsString(vo);

        assertTrue(json.contains("\"boundUserCount\":42"), "boundUserCount 必须维持数字");
        assertTrue(json.contains("\"permissionCount\":128"), "permissionCount 必须维持数字");
    }

    private void collectFields(Class<?> clazz, Set<Field> collected) {
        if (clazz == null || Object.class.equals(clazz)) {
            return;
        }
        collected.addAll(Arrays.asList(clazz.getDeclaredFields()));
        collectFields(clazz.getSuperclass(), collected);
    }
}
