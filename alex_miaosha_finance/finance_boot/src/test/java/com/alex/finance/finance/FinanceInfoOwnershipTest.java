package com.alex.finance.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.finance.entity.FinanceInfo;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.finance.service.impl.FinanceInfoServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FinanceInfoOwnershipTest {

    private FinanceInfoMapper mapper;
    private UserUtils userUtils;
    private FinanceInfoServiceImp service;

    @BeforeEach
    void setUp() {
        mapper = mock(FinanceInfoMapper.class);
        userUtils = mock(UserUtils.class);
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
    @DisplayName("超级管理员可以更新他人流水")
    void testSuperAdminCanUpdateOtherRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(1L, "super_admin"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(100L);
        existing.setBelongTo(999L);
        when(mapper.selectById(100L)).thenReturn(existing);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(100L);
        updateVo.setName("更新名称");
        updateVo.setAmount(new BigDecimal("88.00"));

        assertDoesNotThrow(() -> service.updateFinanceInfo(updateVo));
        verify(mapper, times(1)).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("家庭管理员可以更新他人流水")
    void testFamilyAdminCanUpdateOtherRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(2L, "family_admin"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(100L);
        existing.setBelongTo(999L);
        when(mapper.selectById(100L)).thenReturn(existing);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(100L);
        updateVo.setName("管理员修改");

        assertDoesNotThrow(() -> service.updateFinanceInfo(updateVo));
        verify(mapper, times(1)).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("普通家庭成员可以更新本人流水")
    void testFamilyMemberCanUpdateOwnRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(5L, "family_member"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(101L);
        existing.setBelongTo(5L);
        when(mapper.selectById(101L)).thenReturn(existing);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(101L);
        updateVo.setName("修改自己的流水");

        assertDoesNotThrow(() -> service.updateFinanceInfo(updateVo));
        verify(mapper, times(1)).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("普通家庭成员更新他人流水时被拦截")
    void testFamilyMemberCannotUpdateOtherRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(5L, "family_member"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(102L);
        existing.setBelongTo(888L); // 他人的流水
        when(mapper.selectById(102L)).thenReturn(existing);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(102L);
        updateVo.setName("非法修改他人流水");

        FinanceException ex = assertThrows(FinanceException.class, () -> service.updateFinanceInfo(updateVo));
        assertTrue(ex.getMessage().contains("无权修改其他家庭成员的财务流水"));
        verify(mapper, never()).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("超级管理员可以删除他人流水")
    void testSuperAdminCanDeleteOtherRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(1L, "super_super"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(200L);
        existing.setBelongTo(999L);
        when(mapper.selectById(200L)).thenReturn(existing);

        assertDoesNotThrow(() -> service.deleteFinanceInfo("200"));
        verify(mapper, times(1)).deleteByIds(List.of(200L));
    }

    @Test
    @DisplayName("家庭管理员可以删除他人流水")
    void testFamilyAdminCanDeleteOtherRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(2L, "admin"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(201L);
        existing.setBelongTo(999L);
        when(mapper.selectById(201L)).thenReturn(existing);

        assertDoesNotThrow(() -> service.deleteFinanceInfo("201"));
        verify(mapper, times(1)).deleteByIds(List.of(201L));
    }

    @Test
    @DisplayName("普通家庭成员可以删除本人流水")
    void testFamilyMemberCanDeleteOwnRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(6L, "family_member"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(202L);
        existing.setBelongTo(6L);
        when(mapper.selectById(202L)).thenReturn(existing);

        assertDoesNotThrow(() -> service.deleteFinanceInfo("202"));
        verify(mapper, times(1)).deleteByIds(List.of(202L));
    }

    @Test
    @DisplayName("普通家庭成员删除他人流水时被拦截")
    void testFamilyMemberCannotDeleteOtherRecord() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(6L, "family_member"));

        FinanceInfo existing = new FinanceInfo();
        existing.setId(203L);
        existing.setBelongTo(777L); // 他人流水
        when(mapper.selectById(203L)).thenReturn(existing);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteFinanceInfo("203"));
        assertTrue(ex.getMessage().contains("无权删除其他家庭成员的财务流水"));
        verify(mapper, never()).deleteByIds(any());
    }

    @Test
    @DisplayName("新增流水未传入belongTo时自动填充当前用户ID")
    void testAddFinanceInfoDefaultsBelongTo() {
        when(userUtils.getLoginUser()).thenReturn(buildUser(7L, "family_member"));

        FinanceInfoVo newVo = new FinanceInfoVo();
        newVo.setName("自动绑定属主");
        newVo.setAmount(new BigDecimal("15.00"));

        FinanceInfo result = service.addFinanceInfo(newVo);
        assertNotNull(result);
        assertEquals(7L, newVo.getBelongTo());
        verify(mapper, times(1)).insert(any(FinanceInfo.class));
    }
}
