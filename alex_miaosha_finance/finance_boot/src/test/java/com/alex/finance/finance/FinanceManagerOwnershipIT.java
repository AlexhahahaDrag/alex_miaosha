package com.alex.finance.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.config.Long2StringSerializer;
import com.alex.common.exception.FinanceException;
import com.alex.finance.finance.entity.FinanceInfo;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.finance.service.impl.FinanceInfoServiceImp;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 个人财务流水服务层行级所有权防御 (Ownership Defense) 与雪花 ID 契约集成测试
 */
@ExtendWith(MockitoExtension.class)
class FinanceManagerOwnershipIT {

    @Mock
    private FinanceInfoMapper mapper;

    @Mock
    private UserUtils userUtils;

    private FinanceInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new FinanceInfoServiceImp(mapper, null, null);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        ReflectionTestUtils.setField(service, "userUtils", userUtils);
    }

    private TUserVo buildUser(Long id, String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(id);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(Collections.singletonList(role));
        return user;
    }

    @Test
    @DisplayName("普通用户试图修改他人财务流水时被强制拦截并抛出403 Forbidden")
    void normal_user_cannot_update_other_record() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(101L, "rbac_user"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(999L);
        existing.setBelongTo(888L); // 属于他人
        when(mapper.selectById(999L)).thenReturn(existing);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(999L);
        updateVo.setName("篡改他人账单");

        FinanceException ex = assertThrows(FinanceException.class,
                () -> service.updateFinanceInfo(updateVo));

        assertTrue(ex.getMessage().contains("无权修改其他家庭成员的财务流水"));
    }

    @Test
    @DisplayName("普通用户试图删除他人财务流水时被强制拦截并抛出403 Forbidden")
    void normal_user_cannot_delete_other_record() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(101L, "rbac_user"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(999L);
        existing.setBelongTo(888L); // 属于他人
        when(mapper.selectById(999L)).thenReturn(existing);

        FinanceException ex = assertThrows(FinanceException.class,
                () -> service.deleteFinanceInfo("999"));

        assertTrue(ex.getMessage().contains("无权删除其他家庭成员的财务流水"));
    }

    @Test
    @DisplayName("流水属主本人可以合规更新自己的财务流水")
    void owner_can_freely_update_own_record() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(101L, "rbac_user"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(100L);
        existing.setBelongTo(101L); // 属于本人
        when(mapper.selectById(100L)).thenReturn(existing);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(100L);
        updateVo.setName("更新本人午餐支出");
        updateVo.setAmount(new BigDecimal("35.00"));

        FinanceInfo result = service.updateFinanceInfo(updateVo);

        assertNotNull(result);
        verify(mapper).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("超级管理员具备全局流水管理特权，允许更新他人财务流水")
    void super_admin_can_update_other_record() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(1L, "super_admin"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(100L);
        existing.setBelongTo(999L);
        when(mapper.selectById(100L)).thenReturn(existing);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(100L);
        updateVo.setName("超管调账");

        FinanceInfo result = service.updateFinanceInfo(updateVo);

        assertNotNull(result);
        verify(mapper).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("新增财务流水未显式传递belongTo时应自动注入当前登录用户ID")
    void addFinanceInfo_auto_fill_belong_to_when_missing() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(102L, "rbac_user"));

        FinanceInfoVo vo = new FinanceInfoVo();
        vo.setName("超市购物");
        vo.setAmount(new BigDecimal("99.90"));
        vo.setBelongTo(null);

        FinanceInfo added = service.addFinanceInfo(vo);

        assertNotNull(added);
        assertEquals(102L, vo.getBelongTo());
        verify(mapper).insert(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("FinanceInfoVo 所有 Long 类型主外键均必须挂载 Long2StringSerializer 序列化器")
    void assert_finance_vo_long_ids_annotated() {
        assertAllLongFieldsAnnotated(FinanceInfoVo.class);
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
