package com.alex.finance.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.finance.entity.FinanceInfo;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.finance.service.impl.FinanceInfoServiceImp;
import com.alex.finance.handler.IExcelDictHandlerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 集成测试：财务流水跨家庭成员垂直数据权限隔离、越权修改/删除拦截与管理员穿透放行
 */
@ExtendWith(MockitoExtension.class)
class FinanceInfoCrossMemberPermissionIT {

    @Mock
    private FinanceInfoMapper financeInfoMapper;

    @Mock
    private IExcelDictHandlerImpl iExcelDictHandler;

    @Mock
    private UserApi userApi;

    @Mock
    private UserUtils userUtils;

    private FinanceInfoServiceImp financeInfoService;

    private TUserVo normalUser;
    private TUserVo adminUser;

    @BeforeEach
    void setUp() {
        financeInfoService = new FinanceInfoServiceImp(financeInfoMapper, iExcelDictHandler, userApi);
        ReflectionTestUtils.setField(financeInfoService, "userUtils", userUtils);
        ReflectionTestUtils.setField(financeInfoService, "baseMapper", financeInfoMapper);

        // 普通成员用户 (1001L)
        normalUser = new TUserVo();
        normalUser.setId(1001L);
        normalUser.setUsername("member_alice");
        RoleInfoVo normalRole = new RoleInfoVo();
        normalRole.setRoleCode("ROLE_USER");
        normalUser.setRoleInfoVoList(List.of(normalRole));

        // 管理员用户 (9999L)
        adminUser = new TUserVo();
        adminUser.setId(9999L);
        adminUser.setUsername("family_admin");
        RoleInfoVo adminRole = new RoleInfoVo();
        adminRole.setRoleCode("ROLE_ADMIN");
        adminUser.setRoleInfoVoList(List.of(adminRole));
    }

    @Test
    @DisplayName("垂直越权防御：普通家庭成员尝试修改他人财务流水时严格拦截并抛出 Forbidden 异常")
    void testUpdateFinanceInfo_CrossMemberModificationBlocked() {
        when(userUtils.getLoginUser()).thenReturn(normalUser);

        // 目标流水归属于 1002L（家庭其他成员 Bob）
        FinanceInfo existingBobRecord = new FinanceInfo();
        existingBobRecord.setId(100L);
        existingBobRecord.setBelongTo(1002L);
        existingBobRecord.setAmount(new BigDecimal("299.00"));

        when(financeInfoMapper.selectById(100L)).thenReturn(existingBobRecord);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(100L);
        updateVo.setAmount(new BigDecimal("999.00"));

        FinanceException ex = assertThrows(FinanceException.class, () -> financeInfoService.updateFinanceInfo(updateVo));
        assertTrue(ex.getMessage().contains("无权修改其他家庭成员的财务流水"));
        verify(financeInfoMapper, never()).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("垂直越权防御：批量删除包含他人流水时直接拦截拒绝，保证批量删除操作安全性")
    void testDeleteFinanceInfo_CrossMemberBatchDeletionBlocked() {
        when(userUtils.getLoginUser()).thenReturn(normalUser);

        FinanceInfo record1 = new FinanceInfo();
        record1.setId(10L);
        record1.setBelongTo(1001L); // 自己的

        FinanceInfo record2 = new FinanceInfo();
        record2.setId(11L);
        record2.setBelongTo(1002L); // 他人的

        when(financeInfoMapper.selectById(10L)).thenReturn(record1);
        when(financeInfoMapper.selectById(11L)).thenReturn(record2);

        FinanceException ex = assertThrows(FinanceException.class,
                () -> financeInfoService.deleteFinanceInfo("10,11"));
        assertTrue(ex.getMessage().contains("无权删除其他家庭成员的财务流水"));
        verify(financeInfoMapper, never()).deleteByIds(any());
    }

    @Test
    @DisplayName("归属权放行：普通家庭成员修改自身归属的流水正常放行")
    void testUpdateFinanceInfo_OwnRecordAllowed() {
        when(userUtils.getLoginUser()).thenReturn(normalUser);

        FinanceInfo ownRecord = new FinanceInfo();
        ownRecord.setId(100L);
        ownRecord.setBelongTo(1001L); // 自己的
        ownRecord.setAmount(new BigDecimal("50.00"));

        when(financeInfoMapper.selectById(100L)).thenReturn(ownRecord);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(100L);
        updateVo.setAmount(new BigDecimal("88.00"));

        FinanceInfo result = financeInfoService.updateFinanceInfo(updateVo);
        assertNotNull(result);
        verify(financeInfoMapper, times(1)).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("管理权限放行：机构管理员/家庭管理员可跨成员修改并管理全员流水")
    void testUpdateFinanceInfo_AdminCanManageOtherMembers() {
        when(userUtils.getLoginUser()).thenReturn(adminUser);

        FinanceInfo otherRecord = new FinanceInfo();
        otherRecord.setId(100L);
        otherRecord.setBelongTo(1002L);
        otherRecord.setAmount(new BigDecimal("300.00"));

        when(financeInfoMapper.selectById(100L)).thenReturn(otherRecord);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(100L);
        updateVo.setAmount(new BigDecimal("350.00"));

        assertDoesNotThrow(() -> financeInfoService.updateFinanceInfo(updateVo));
        verify(financeInfoMapper, times(1)).updateById(any(FinanceInfo.class));
    }

    @Test
    @DisplayName("自动归属绑定：新增流水未指定 belongTo 时自动注入当前登录人员 ID")
    void testAddFinanceInfo_AutoFillsCurrentLoginUserId() {
        when(userUtils.getLoginUser()).thenReturn(normalUser);

        FinanceInfoVo newVo = new FinanceInfoVo();
        newVo.setAmount(new BigDecimal("12.50"));
        // 未传 belongTo

        financeInfoService.addFinanceInfo(newVo);

        ArgumentCaptor<FinanceInfo> captor = ArgumentCaptor.forClass(FinanceInfo.class);
        verify(financeInfoMapper, times(1)).insert(captor.capture());

        FinanceInfo inserted = captor.getValue();
        assertNotNull(inserted);
        assertEquals(1001L, inserted.getBelongTo(), "必须自动绑定当前登录人员 ID 为流水归属人");
    }
}
