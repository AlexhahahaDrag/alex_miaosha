package com.alex.finance.gift;

import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.common.exception.FinanceException;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
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
 * Service 层集成测试：真实 {@link GiftDataScopeSupport} + {@link GiftRecordInfoServiceImp}，
 * 仅 Mock 外部 Mapper / 登录态。
 */
@ExtendWith(MockitoExtension.class)
class GiftRecordInfoServiceIT {

    private static final Long LOGIN_USER_ID = 10L;
    private static final Long LOGIN_ORG_ID = 20L;
    private static final Long OTHER_USER_ID = 99L;

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
        GiftDataScopeSupport giftDataScopeSupport = new GiftDataScopeSupport(userUtils);
        service = new GiftRecordInfoServiceImp(giftDataScopeSupport, giftPersonInfoMapper, giftEventInfoMapper, null);
        ReflectionTestUtils.setField(service, "baseMapper", giftRecordInfoMapper);
        when(userUtils.getLoginUser()).thenReturn(loginUser());
        when(giftRecordInfoMapper.insert(any(GiftRecordInfo.class))).thenAnswer(invocation -> {
            GiftRecordInfo entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
    }

    @Test
    void addRecord_should_reject_when_amount_zero_or_negative() {
        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfo(receiveVo(BigDecimal.ZERO)));
        assertEquals("礼金金额必须大于0", ex.getMsg());

        ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfo(receiveVo(new BigDecimal("-0.01"))));
        assertEquals("礼金金额必须大于0", ex.getMsg());
    }

    @Test
    void addRecord_RETURN_should_reject_when_related_not_RECEIVE() {
        GiftRecordInfo giveRecord = receiveRecord(1L, OTHER_USER_ID, LOGIN_ORG_ID);
        giveRecord.setDirection("GIVE");
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(giveRecord);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfo(returnVo(1L, new BigDecimal("100"))));
        assertEquals("回礼记录只能关联收礼记录", ex.getMsg());
    }

    @Test
    void addRecord_RETURN_should_reject_when_related_not_exists() {
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(null);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfo(returnVo(1L, new BigDecimal("100"))));
        assertEquals("关联的收礼记录不存在", ex.getMsg());
    }

    @Test
    void addRecord_RETURN_should_reject_when_related_belongs_to_other_user() {
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(receiveRecord(1L, OTHER_USER_ID, LOGIN_ORG_ID));

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfo(returnVo(1L, new BigDecimal("100"))));
        assertEquals("无权访问其他用户的礼金记录", ex.getMsg());
    }

    @Test
    void updateRecord_RETURN_should_reject_when_related_is_self() {
        GiftRecordInfo existing = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        existing.setDirection("RETURN");
        existing.setRelatedRecordId(1L);
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(existing);

        GiftRecordInfoVo update = returnVo(1L, new BigDecimal("50"));
        update.setId(1L);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.updateGiftRecordInfo(update));
        assertEquals("回礼记录不能关联自身", ex.getMsg());
    }

    @Test
    void addRecord_should_reject_when_personId_belongs_to_other() {
        when(giftPersonInfoMapper.selectById(5L)).thenReturn(person(5L, OTHER_USER_ID, LOGIN_ORG_ID));

        GiftRecordInfoVo vo = receiveVo(new BigDecimal("200"));
        vo.setGiverPersonId(5L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("无权访问其他用户的亲友", ex.getMsg());
    }

    @Test
    void addRecord_should_reject_when_personId_not_exists() {
        when(giftPersonInfoMapper.selectById(5L)).thenReturn(null);

        GiftRecordInfoVo vo = receiveVo(new BigDecimal("200"));
        vo.setGiverPersonId(5L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("亲友不存在", ex.getMsg());
    }

    @Test
    void addRecord_should_reject_when_eventId_belongs_to_other() {
        when(giftEventInfoMapper.selectById(8L)).thenReturn(event(8L, OTHER_USER_ID, LOGIN_ORG_ID));

        GiftRecordInfoVo vo = receiveVo(new BigDecimal("200"));
        vo.setEventId(8L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(vo));
        assertEquals("无权访问其他用户的事由", ex.getMsg());
    }

    @Test
    void addRecord_should_succeed_when_references_and_return_relation_valid() {
        GiftRecordInfo related = receiveRecord(1L, LOGIN_USER_ID, LOGIN_ORG_ID);
        when(giftRecordInfoMapper.selectById(1L)).thenReturn(related);
        when(giftPersonInfoMapper.selectById(5L)).thenReturn(person(5L, LOGIN_USER_ID, LOGIN_ORG_ID));
        when(giftEventInfoMapper.selectById(8L)).thenReturn(event(8L, LOGIN_USER_ID, LOGIN_ORG_ID));

        GiftRecordInfoVo vo = returnVo(1L, new BigDecimal("88.88"));
        vo.setGiverPersonId(5L);
        vo.setEventId(8L);

        GiftRecordInfoVo saved = service.addGiftRecordInfo(vo);

        assertNotNull(saved.getId());
        assertEquals(LOGIN_USER_ID, saved.getUserId());
        assertEquals(LOGIN_ORG_ID, saved.getOrgId());
        verify(giftRecordInfoMapper).insert(any(GiftRecordInfo.class));
    }

    @Test
    void updateRecord_should_reject_cross_user() {
        GiftRecordInfo existing = receiveRecord(2L, OTHER_USER_ID, LOGIN_ORG_ID);
        when(giftRecordInfoMapper.selectById(2L)).thenReturn(existing);

        GiftRecordInfoVo update = receiveVo(new BigDecimal("100"));
        update.setId(2L);

        FinanceException ex = assertThrows(FinanceException.class, () ->
                service.updateGiftRecordInfo(update));
        assertEquals("无权访问其他用户的礼金记录", ex.getMsg());
    }

    @Test
    void markReturned_should_reject_cross_user() {
        when(giftRecordInfoMapper.selectById(3L)).thenReturn(receiveRecord(3L, OTHER_USER_ID, LOGIN_ORG_ID));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.markReturned(3L));
        assertEquals("无权访问其他用户的礼金记录", ex.getMsg());
    }

    private GiftRecordInfoVo receiveVo(BigDecimal amount) {
        return new GiftRecordInfoVo()
                .setDirection("RECEIVE")
                .setAmount(amount);
    }

    private GiftRecordInfoVo returnVo(Long relatedRecordId, BigDecimal amount) {
        return new GiftRecordInfoVo()
                .setDirection("RETURN")
                .setRelatedRecordId(relatedRecordId)
                .setAmount(amount);
    }

    private GiftRecordInfo receiveRecord(Long id, Long userId, Long orgId) {
        GiftRecordInfo record = new GiftRecordInfo()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("500"));
        record.setId(id);
        record.setUserId(userId);
        record.setOrgId(orgId);
        return record;
    }

    private GiftPersonInfo person(Long id, Long userId, Long orgId) {
        GiftPersonInfo person = new GiftPersonInfo().setPersonName("test");
        person.setId(id);
        person.setUserId(userId);
        person.setOrgId(orgId);
        return person;
    }

    private GiftEventInfo event(Long id, Long userId, Long orgId) {
        GiftEventInfo event = new GiftEventInfo().setEventName("wedding");
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
