package com.alex.finance.gift;

import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.alex.finance.gift.event.mapper.GiftEventInfoTMapper;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.mapper.GiftRecordInfoTMapper;
import com.alex.common.exception.FinanceException;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoTServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service 层集成测试：真实 {@link GiftDataScopeSupport} + {@link GiftRecordInfoTServiceImp}，
 * 仅 Mock 外部 Mapper / 登录态。
 */
@ExtendWith(MockitoExtension.class)
class GiftRecordInfoTServiceIT {

    private static final Long LOGIN_USER_ID = 10L;
    private static final Long LOGIN_ORG_ID = 20L;
    private static final Long OTHER_USER_ID = 99L;

    @Mock
    private GiftRecordInfoTMapper giftRecordInfoTMapper;
    @Mock
    private GiftPersonInfoTMapper giftPersonInfoTMapper;
    @Mock
    private GiftEventInfoTMapper giftEventInfoTMapper;
    @Mock
    private UserUtils userUtils;

    private GiftRecordInfoTServiceImp service;

    @BeforeEach
    void setUp() {
        GiftDataScopeSupport giftDataScopeSupport = new GiftDataScopeSupport(userUtils);
        service = new GiftRecordInfoTServiceImp(giftDataScopeSupport, giftPersonInfoTMapper, giftEventInfoTMapper);
        ReflectionTestUtils.setField(service, "baseMapper", giftRecordInfoTMapper);
        when(userUtils.getLoginUser()).thenReturn(loginUser());
        when(giftRecordInfoTMapper.insert(any(GiftRecordInfoT.class))).thenAnswer(invocation -> {
            GiftRecordInfoT entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
    }

    @Test
    void addRecord_should_reject_when_amount_zero_or_negative() {
        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfoT(receiveVo(BigDecimal.ZERO)));
        assertEquals("礼金金额必须大于0", ex.getMsg());

        ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfoT(receiveVo(new BigDecimal("-0.01"))));
        assertEquals("礼金金额必须大于0", ex.getMsg());
    }

    @Test
    void addRecord_RETURN_should_reject_when_related_not_RECEIVE() {
        GiftRecordInfoT giveRecord = receiveRecord(1L, OTHER_USER_ID, LOGIN_ORG_ID);
        giveRecord.setDirection("GIVE");
        when(giftRecordInfoTMapper.selectById(1L)).thenReturn(giveRecord);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfoT(returnVo(1L, new BigDecimal("100"))));
        assertEquals("回礼记录只能关联收礼记录", ex.getMsg());
    }

    @Test
    void addRecord_RETURN_should_reject_when_related_not_exists() {
        when(giftRecordInfoTMapper.selectById(1L)).thenReturn(null);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfoT(returnVo(1L, new BigDecimal("100"))));
        assertEquals("关联的收礼记录不存在", ex.getMsg());
    }

    @Test
    void addRecord_RETURN_should_reject_when_related_belongs_to_other_user() {
        when(giftRecordInfoTMapper.selectById(1L)).thenReturn(receiveRecord(1L, OTHER_USER_ID, LOGIN_ORG_ID));

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfoT(returnVo(1L, new BigDecimal("100"))));
        assertEquals("无权访问其他用户的礼金记录", ex.getMsg());
    }

    @Test
    void updateRecord_RETURN_should_reject_when_related_is_self() {
        GiftRecordInfoT existing = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        existing.setDirection("RETURN");
        existing.setRelatedRecordId(1L);
        when(giftRecordInfoTMapper.selectById(1L)).thenReturn(existing);

        GiftRecordInfoTVo update = returnVo(1L, new BigDecimal("50"));
        update.setId(1L);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.updateGiftRecordInfoT(update));
        assertEquals("回礼记录不能关联自身", ex.getMsg());
    }

    @Test
    void addRecord_should_reject_when_personId_belongs_to_other() {
        when(giftPersonInfoTMapper.selectById(5L)).thenReturn(person(5L, OTHER_USER_ID, LOGIN_ORG_ID));

        GiftRecordInfoTVo vo = receiveVo(new BigDecimal("200"));
        vo.setGiverPersonId(5L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfoT(vo));
        assertEquals("无权访问其他用户的亲友", ex.getMsg());
    }

    @Test
    void addRecord_should_reject_when_personId_not_exists() {
        when(giftPersonInfoTMapper.selectById(5L)).thenReturn(null);

        GiftRecordInfoTVo vo = receiveVo(new BigDecimal("200"));
        vo.setGiverPersonId(5L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfoT(vo));
        assertEquals("亲友不存在", ex.getMsg());
    }

    @Test
    void addRecord_should_reject_when_eventId_belongs_to_other() {
        when(giftEventInfoTMapper.selectById(8L)).thenReturn(event(8L, OTHER_USER_ID, LOGIN_ORG_ID));

        GiftRecordInfoTVo vo = receiveVo(new BigDecimal("200"));
        vo.setEventId(8L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfoT(vo));
        assertEquals("无权访问其他用户的事由", ex.getMsg());
    }

    @Test
    void addRecord_should_succeed_when_references_and_return_relation_valid() {
        GiftRecordInfoT related = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        when(giftRecordInfoTMapper.selectById(1L)).thenReturn(related);
        when(giftPersonInfoTMapper.selectById(5L)).thenReturn(person(5L, LOGIN_USER_ID, LOGIN_ORG_ID));
        when(giftEventInfoTMapper.selectById(8L)).thenReturn(event(8L, LOGIN_USER_ID, LOGIN_ORG_ID));

        GiftRecordInfoTVo vo = returnVo(1L, new BigDecimal("88.88"));
        vo.setGiverPersonId(5L);
        vo.setEventId(8L);

        GiftRecordInfoTVo saved = service.addGiftRecordInfoT(vo);

        assertNotNull(saved.getId());
        assertEquals(LOGIN_USER_ID, saved.getUserId());
        assertEquals(LOGIN_ORG_ID, saved.getOrgId());
        verify(giftRecordInfoTMapper).insert(any(GiftRecordInfoT.class));
    }

    @Test
    void updateRecord_should_reject_cross_user() {
        GiftRecordInfoT existing = receiveRecord(2L, OTHER_USER_ID, LOGIN_ORG_ID);
        when(giftRecordInfoTMapper.selectById(2L)).thenReturn(existing);

        GiftRecordInfoTVo update = receiveVo(new BigDecimal("100"));
        update.setId(2L);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.updateGiftRecordInfoT(update));
        assertEquals("无权访问其他用户的礼金记录", ex.getMsg());
    }

    @Test
    void markReturned_should_reject_cross_user() {
        when(giftRecordInfoTMapper.selectById(3L)).thenReturn(receiveRecord(3L, OTHER_USER_ID, LOGIN_ORG_ID));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.markReturned(3L));
        assertEquals("无权访问其他用户的礼金记录", ex.getMsg());
    }

    private GiftRecordInfoTVo receiveVo(BigDecimal amount) {
        return new GiftRecordInfoTVo()
                .setDirection("RECEIVE")
                .setAmount(amount);
    }

    private GiftRecordInfoTVo returnVo(Long relatedRecordId, BigDecimal amount) {
        return new GiftRecordInfoTVo()
                .setDirection("RETURN")
                .setRelatedRecordId(relatedRecordId)
                .setAmount(amount);
    }

    private GiftRecordInfoT receiveRecord(Long id, Long userId, Long orgId) {
        GiftRecordInfoT record = new GiftRecordInfoT()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("500"));
        record.setId(id);
        record.setUserId(userId);
        record.setOrgId(orgId);
        return record;
    }

    private GiftPersonInfoT person(Long id, Long userId, Long orgId) {
        GiftPersonInfoT person = new GiftPersonInfoT().setPersonName("test");
        person.setId(id);
        person.setUserId(userId);
        person.setOrgId(orgId);
        return person;
    }

    private GiftEventInfoT event(Long id, Long userId, Long orgId) {
        GiftEventInfoT event = new GiftEventInfoT().setEventName("wedding");
        event.setId(id);
        event.setUserId(userId);
        event.setOrgId(orgId);
        return event;
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
