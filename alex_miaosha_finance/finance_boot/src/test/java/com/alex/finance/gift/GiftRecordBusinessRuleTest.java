package com.alex.finance.gift;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftDirectionAggVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.common.exception.FinanceException;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftRecordBusinessRuleTest {

    private static final Long LOGIN_USER_ID = 10L;
    private static final Long LOGIN_ORG_ID = 20L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long OTHER_ORG_ID = 88L;

    @Mock
    private GiftRecordInfoMapper giftRecordInfoMapper;
    @Mock
    private GiftPersonInfoMapper giftPersonInfoMapper;
    @Mock
    private GiftEventInfoMapper giftEventInfoMapper;
    @Mock
    private UserUtils userUtils;

    private GiftRecordInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new GiftRecordInfoServiceImp(
                new GiftDataScopeSupport(userUtils),
                giftPersonInfoMapper,
                giftEventInfoMapper,
                null,
                null);
        ReflectionTestUtils.setField(service, "baseMapper", giftRecordInfoMapper);
        lenient().when(userUtils.getLoginUser()).thenReturn(loginUser());
        lenient().when(giftRecordInfoMapper.insert(any(GiftRecordInfo.class))).thenAnswer(invocation -> {
            GiftRecordInfo entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
    }

    // =========================================================================
    // 5.1.1 addGiftRecordInfo 测试
    // =========================================================================

    @Test
    @DisplayName("L1: should_create_GIVE_when_valid - 合法送礼记录正常创建")
    void should_create_GIVE_when_valid() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("GIVE")
                .setAmount(new BigDecimal("500.00"));

        GiftRecordInfoVo result = service.addGiftRecordInfo(vo);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(LOGIN_USER_ID, result.getUserId());
        assertEquals(LOGIN_ORG_ID, result.getOrgId());
        verify(giftRecordInfoMapper).insert(any(GiftRecordInfo.class));
    }

    @Test
    @DisplayName("L2: should_default_returnedFlag_to_0_when_RECEIVE - 收礼记录默认 returnedFlag=0")
    void should_default_returnedFlag_to_0_when_RECEIVE() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("666.00"));

        GiftRecordInfoVo result = service.addGiftRecordInfo(vo);

        assertEquals(0, result.getReturnedFlag());
        ArgumentCaptor<GiftRecordInfo> captor = ArgumentCaptor.forClass(GiftRecordInfo.class);
        verify(giftRecordInfoMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getReturnedFlag());
    }

    @Test
    @DisplayName("L3: should_create_RETURN_when_relatedRecordId_exists - 合法关联收礼的回礼记录正常创建")
    void should_create_RETURN_when_relatedRecordId_exists() {
        GiftRecordInfo related = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(related);

        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("RETURN")
                .setRelatedRecordId(1L)
                .setAmount(new BigDecimal("600.00"));

        GiftRecordInfoVo result = service.addGiftRecordInfo(vo);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("I1: should_throw_when_direction_is_null - 礼金方向为空拦截")
    void should_throw_when_direction_is_null() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo().setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("礼金方向不能为空", ex.getMsg());
    }

    @Test
    @DisplayName("I2: should_throw_when_direction_is_lowercase - 礼金方向小写边界拦截")
    void should_throw_when_direction_is_lowercase() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("give")
                .setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("礼金方向不合法", ex.getMsg());
    }

    @Test
    @DisplayName("I3: should_throw_when_RETURN_relatedRecordId_is_null - 回礼记录关联ID为空拦截")
    void should_throw_when_RETURN_relatedRecordId_is_null() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("RETURN")
                .setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("回礼记录必须关联原始收礼记录", ex.getMsg());
    }

    @Test
    @DisplayName("I4: should_throw_when_RETURN_relatedRecordId_is_self - 回礼记录不能关联自身")
    void should_throw_when_RETURN_relatedRecordId_is_self() {
        GiftRecordInfo existing = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        existing.setDirection("RETURN");
        existing.setRelatedRecordId(1L);
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(existing);

        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("RETURN")
                .setRelatedRecordId(1L)
                .setAmount(new BigDecimal("100"));
        vo.setId(1L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.updateGiftRecordInfo(vo));
        assertEquals("回礼记录不能关联自身", ex.getMsg());
    }

    @Test
    @DisplayName("I5: should_throw_when_RETURN_relatedRecordId_not_exists - 关联记录不存在拦截")
    void should_throw_when_RETURN_relatedRecordId_not_exists() {
        when(giftRecordInfoMapper.selectById(999L)).thenReturn(null);

        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("RETURN")
                .setRelatedRecordId(999L)
                .setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("关联的收礼记录不存在", ex.getMsg());
    }

    @Test
    @DisplayName("I6: should_throw_when_RETURN_relatedRecordId_belongs_to_other_user - 跨机构关联收礼记录拦截")
    void should_throw_when_RETURN_relatedRecordId_belongs_to_other_user() {
        GiftRecordInfo otherRecord = receiveRecord(2L, OTHER_USER_ID, OTHER_ORG_ID);
        when(giftRecordInfoMapper.selectById(2L)).thenReturn(otherRecord);

        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("RETURN")
                .setRelatedRecordId(2L)
                .setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
    }

    @Test
    @DisplayName("I6b: should_throw_when_RETURN_relatedRecordId_direction_is_not_RECEIVE - 关联非收礼记录拦截")
    void should_throw_when_RETURN_relatedRecordId_direction_is_not_RECEIVE() {
        GiftRecordInfo giveRecord = receiveRecord(3L, LOGIN_USER_ID, LOGIN_ORG_ID);
        giveRecord.setDirection("GIVE");
        when(giftRecordInfoMapper.selectById(3L)).thenReturn(giveRecord);

        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("RETURN")
                .setRelatedRecordId(3L)
                .setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("回礼记录只能关联收礼记录", ex.getMsg());
    }

    @Test
    @DisplayName("I7a: should_throw_when_amount_is_negative - 负数金额拦截")
    void should_throw_when_amount_is_negative() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("GIVE")
                .setAmount(new BigDecimal("-50.00"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("礼金金额必须大于0", ex.getMsg());
    }

    @Test
    @DisplayName("I7b: should_throw_when_amount_is_zero - 0元金额拦截")
    void should_throw_when_amount_is_zero() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("GIVE")
                .setAmount(BigDecimal.ZERO);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("礼金金额必须大于0", ex.getMsg());
    }

    @Test
    @DisplayName("I7c: should_truncate_when_amount_precision_overflow - 金额超出2位小数半入截断")
    void should_truncate_when_amount_precision_overflow() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("GIVE")
                .setAmount(new BigDecimal("100.886"));

        GiftRecordInfoVo saved = service.addGiftRecordInfo(vo);
        assertEquals(new BigDecimal("100.89"), saved.getAmount());
    }

    @Test
    @DisplayName("H11: should_throw_when_userUtils_returns_null_user - 未登录拦截")
    void should_throw_when_userUtils_returns_null_user() {
        when(userUtils.getLoginUser()).thenReturn(null);

        GiftRecordInfoVo vo = new GiftRecordInfoVo()
                .setDirection("GIVE")
                .setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("请先登录", ex.getMsg());
    }

    // =========================================================================
    // 5.1.1 markReturned 测试
    // =========================================================================

    @Test
    @DisplayName("I8: should_throw_when_id_is_null - 标记回礼ID为空拦截")
    void markReturned_should_throw_when_id_is_null() {
        FinanceException ex = assertThrows(FinanceException.class, () -> service.markReturned(null));
        assertEquals("原始收礼记录不能为空", ex.getMsg());
    }

    @Test
    @DisplayName("I9: should_throw_when_record_not_exists - 标记回礼记录不存在拦截")
    void markReturned_should_throw_when_record_not_exists() {
        when(giftRecordInfoMapper.selectById(999L)).thenReturn(null);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.markReturned(999L));
        assertEquals("礼金记录不存在", ex.getMsg());
    }

    @Test
    @DisplayName("I10: should_throw_when_record_direction_is_not_RECEIVE - 仅收礼记录可标记已回礼")
    void markReturned_should_throw_when_record_direction_is_not_RECEIVE() {
        GiftRecordInfo giveRecord = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        giveRecord.setDirection("GIVE");
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(giveRecord);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.markReturned(1L));
        assertEquals("仅收礼记录可标记已回礼", ex.getMsg());
    }

    @Test
    @DisplayName("I11: should_throw_when_record_belongs_to_other_user - 跨机构标记回礼拦截")
    void markReturned_should_throw_when_record_belongs_to_other_user() {
        GiftRecordInfo otherRecord = receiveRecord(1L, OTHER_USER_ID, OTHER_ORG_ID);
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(otherRecord);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.markReturned(1L));
        assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
    }

    @Test
    @DisplayName("I12: should_be_idempotent_when_already_returned - 已回礼记录幂等返回true")
    void markReturned_should_be_idempotent_when_already_returned() {
        GiftRecordInfo receiveRecord = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        receiveRecord.setReturnedFlag(1);
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(receiveRecord);

        Boolean result = service.markReturned(1L);

        assertTrue(result);
        verify(giftRecordInfoMapper, never()).updateById((GiftRecordInfo) any());
    }

    // =========================================================================
    // 5.1.1 updateGiftRecordInfo & deleteGiftRecordInfo 测试
    // =========================================================================

    @Test
    @DisplayName("update: should_throw_when_record_belongs_to_other_user - 修改他人记录越权拦截")
    void update_should_throw_when_record_belongs_to_other_user() {
        GiftRecordInfo otherRecord = receiveRecord(2L, OTHER_USER_ID, OTHER_ORG_ID);
        when(giftRecordInfoMapper.selectById(2L)).thenReturn(otherRecord);

        GiftRecordInfoVo vo = new GiftRecordInfoVo();
        vo.setId(2L);
        vo.setDirection("GIVE");
        vo.setAmount(new BigDecimal("100"));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.updateGiftRecordInfo(vo));
        assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
    }

    @Test
    @DisplayName("update: should_keep_orgId_userId_immutable - 修改操作锁定归属人与机构不可篡改")
    void update_should_keep_orgId_userId_immutable() {
        GiftRecordInfo existing = receiveRecord(5L, LOGIN_USER_ID, LOGIN_ORG_ID);
        when(giftRecordInfoMapper.selectById(5L)).thenReturn(existing);
        when(giftRecordInfoMapper.updateById((GiftRecordInfo) any())).thenReturn(1);

        GiftRecordInfoVo vo = new GiftRecordInfoVo();
        vo.setId(5L);
        vo.setDirection("GIVE");
        vo.setAmount(new BigDecimal("300"));
        vo.setUserId(999L);
        vo.setOrgId(888L);

        service.updateGiftRecordInfo(vo);

        ArgumentCaptor<GiftRecordInfo> captor = ArgumentCaptor.forClass(GiftRecordInfo.class);
        verify(giftRecordInfoMapper).updateById(captor.capture());
        assertEquals(LOGIN_USER_ID, captor.getValue().getUserId());
        assertEquals(LOGIN_ORG_ID, captor.getValue().getOrgId());
    }

    @Test
    @DisplayName("delete: should_throw_when_record_belongs_to_other_user - 删除他人记录越权拦截")
    void delete_should_throw_when_record_belongs_to_other_user() {
        when(giftRecordInfoMapper.selectById(7L)).thenReturn(receiveRecord(7L, OTHER_USER_ID, OTHER_ORG_ID));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteGiftRecordInfo("7"));
        assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
    }

    @Test
    @DisplayName("delete: should_skip_silently_when_ids_empty - 空ID删除静默跳过")
    void delete_should_skip_silently_when_ids_empty() {
        assertTrue(service.deleteGiftRecordInfo(""));
        assertTrue(service.deleteGiftRecordInfo("   "));
        assertTrue(service.deleteGiftRecordInfo(null));
    }

    @Test
    @DisplayName("delete: should_throw_business_error_when_id_not_parsable - 非法ID字符报错拦截")
    void delete_should_throw_business_error_when_id_not_parsable() {
        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteGiftRecordInfo("1,abc,3"));
        assertEquals("礼金记录ID格式不合法", ex.getMsg());
    }

    @Test
    @DisplayName("delete: should_delete_all_when_ids_are_valid_csv - 合法ID批量删除成功")
    void delete_should_delete_all_when_ids_are_valid_csv() {
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID));
        when(giftRecordInfoMapper.selectById(2L)).thenReturn(receiveRecord(2L, LOGIN_USER_ID, LOGIN_ORG_ID));

        GiftRecordInfoServiceImp spyService = spy(service);
        doReturn(true).when(spyService).removeBatchByIds(any());

        Boolean result = spyService.deleteGiftRecordInfo("1,2");
        assertTrue(result);
    }

    // =========================================================================
    // 5.1.1 calculatePendingReturnAmount & getSummary 测试
    // =========================================================================

    @Test
    @DisplayName("calcPending: should_throw_when_id_is_null_or_not_exists_or_not_RECEIVE")
    void calcPending_should_throw_on_invalid_conditions() {
        // null ID
        FinanceException ex = assertThrows(FinanceException.class, () -> service.calculatePendingReturnAmount(null));
        assertEquals("原始收礼记录不能为空", ex.getMsg());

        // not exists
        when(giftRecordInfoMapper.selectById(99L)).thenReturn(null);
        ex = assertThrows(FinanceException.class, () -> service.calculatePendingReturnAmount(99L));
        assertEquals("礼金记录不存在", ex.getMsg());

        // not RECEIVE
        GiftRecordInfo give = receiveRecord(8L, LOGIN_USER_ID, LOGIN_ORG_ID);
        give.setDirection("GIVE");
        when(giftRecordInfoMapper.selectById(8L)).thenReturn(give);
        ex = assertThrows(FinanceException.class, () -> service.calculatePendingReturnAmount(8L));
        assertEquals("原始记录必须是收礼记录", ex.getMsg());
    }

    @Test
    @DisplayName("calcPending: should_return_full_amount_when_no_return_records - 零回礼返回全额")
    void calcPending_should_return_full_amount_when_no_return_records() {
        GiftRecordInfo receive = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        receive.setAmount(new BigDecimal("800.00"));
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(receive);
        when(giftRecordInfoMapper.sumReturnAmountByRelatedRecordId(1L)).thenReturn(null);

        BigDecimal pending = service.calculatePendingReturnAmount(1L);
        assertEquals(new BigDecimal("800.00"), pending);
    }

    @Test
    @DisplayName("calcPending: should_return_zero_when_over_returned - 超额回礼时归零")
    void calcPending_should_return_zero_when_over_returned() {
        GiftRecordInfo receive = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        receive.setAmount(new BigDecimal("500.00"));
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(receive);
        when(giftRecordInfoMapper.sumReturnAmountByRelatedRecordId(1L)).thenReturn(new BigDecimal("600.00"));

        BigDecimal pending = service.calculatePendingReturnAmount(1L);
        assertEquals(BigDecimal.ZERO, pending);
    }

    @Test
    @DisplayName("calcPending: should_handle_null_amount_in_records - 金额为null兜底防御")
    void calcPending_should_handle_null_amount_in_records() {
        GiftRecordInfo receive = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        receive.setAmount(null);
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(receive);
        when(giftRecordInfoMapper.sumReturnAmountByRelatedRecordId(1L)).thenReturn(null);

        BigDecimal pending = service.calculatePendingReturnAmount(1L);
        assertEquals(BigDecimal.ZERO, pending);
    }

    @Test
    @DisplayName("getSummary: should_calculate_netAmount_correctly - 准确计算三向金额与净额")
    void getSummary_should_calculate_netAmount_correctly() {
        GiftDirectionAggVo receiveAgg = new GiftDirectionAggVo();
        receiveAgg.setDirection("RECEIVE");
        receiveAgg.setTotalAmount(new BigDecimal("1000.00"));
        receiveAgg.setRecordCount(2L);

        GiftDirectionAggVo giveAgg = new GiftDirectionAggVo();
        giveAgg.setDirection("GIVE");
        giveAgg.setTotalAmount(new BigDecimal("300.00"));
        giveAgg.setRecordCount(1L);

        GiftDirectionAggVo returnAgg = new GiftDirectionAggVo();
        returnAgg.setDirection("RETURN");
        returnAgg.setTotalAmount(new BigDecimal("200.00"));
        returnAgg.setRecordCount(1L);

        when(giftRecordInfoMapper.sumDirectionAgg(any())).thenReturn(List.of(receiveAgg, giveAgg, returnAgg));

        GiftRecordSummaryVo summary = service.getSummary(new GiftRecordQuery());

        assertEquals(new BigDecimal("1000.00"), summary.getReceiveAmount());
        assertEquals(new BigDecimal("300.00"), summary.getGiveAmount());
        assertEquals(new BigDecimal("200.00"), summary.getReturnAmount());
        // netAmount = receiveAmount - giveAmount - returnAmount = 1000 - 300 - 200 = 500
        assertEquals(new BigDecimal("500.00"), summary.getNetAmount());
        assertEquals(4L, summary.getRecordCount());
        assertEquals(2L, summary.getReceiveCount());
        assertEquals(1L, summary.getGiveCount());
        assertEquals(1L, summary.getReturnCount());
    }

    @Test
    @DisplayName("getSummary: should_handle_empty_records - 空数据安全兜底全为0")
    void getSummary_should_handle_empty_records() {
        when(giftRecordInfoMapper.sumDirectionAgg(any())).thenReturn(Collections.emptyList());

        GiftRecordSummaryVo summary = service.getSummary(new GiftRecordQuery());

        assertEquals(BigDecimal.ZERO, summary.getReceiveAmount());
        assertEquals(BigDecimal.ZERO, summary.getGiveAmount());
        assertEquals(BigDecimal.ZERO, summary.getReturnAmount());
        assertEquals(BigDecimal.ZERO, summary.getNetAmount());
        assertEquals(0L, summary.getRecordCount());
    }

    // =========================================================================
    // 辅助构造
    // =========================================================================

    private GiftRecordInfo receiveRecord(Long id, Long userId, Long orgId) {
        GiftRecordInfo r = new GiftRecordInfo()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("500.00"));
        r.setId(id);
        r.setUserId(userId);
        r.setOrgId(orgId);
        return r;
    }

    private TUserVo loginUser() {
        OrgInfoVo orgInfoVo = new OrgInfoVo();
        orgInfoVo.setId(LOGIN_ORG_ID);
        TUserVo user = new TUserVo();
        user.setId(LOGIN_USER_ID);
        user.setOrgId(LOGIN_ORG_ID);
        user.setOrgInfoVo(orgInfoVo);
        return user;
    }
}
