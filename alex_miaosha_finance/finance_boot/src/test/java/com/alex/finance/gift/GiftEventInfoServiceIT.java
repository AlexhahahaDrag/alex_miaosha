package com.alex.finance.gift;

import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.event.service.impl.GiftEventInfoServiceImp;
import com.alex.finance.gift.eventoption.service.GiftEventTypeOptionService;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service 层集成测试：真实 {@link GiftDataScopeSupport} + {@link GiftEventInfoServiceImp}，
 * 验证参数防御、主办人跨租户引用与 ORG_SHARED 数据权限隔离。
 */
@ExtendWith(MockitoExtension.class)
class GiftEventInfoServiceIT {

    private static final Long LOGIN_USER_ID = 10L;
    private static final Long LOGIN_ORG_ID = 20L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long OTHER_ORG_ID = 88L;

    @Mock
    private GiftEventInfoMapper giftEventInfoMapper;
    @Mock
    private GiftEventTypeOptionService giftEventTypeOptionService;
    @Mock
    private GiftRecordInfoService giftRecordInfoService;
    @Mock
    private GiftPersonInfoMapper giftPersonInfoMapper;
    @Mock
    private UserUtils userUtils;

    private GiftEventInfoServiceImp service;

    @BeforeEach
    void setUp() {
        GiftDataScopeSupport giftDataScopeSupport = new GiftDataScopeSupport(userUtils);
        service = new GiftEventInfoServiceImp(
                giftDataScopeSupport,
                giftEventTypeOptionService,
                giftRecordInfoService,
                giftPersonInfoMapper);
        ReflectionTestUtils.setField(service, "baseMapper", giftEventInfoMapper);

        lenient().when(userUtils.getLoginUser()).thenReturn(loginUser());
        lenient().when(giftEventInfoMapper.insert(any(GiftEventInfo.class))).thenAnswer(invocation -> {
            GiftEventInfo entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
    }

    @Test
    void addEvent_should_reject_when_name_is_empty_or_blank() {
        GiftEventInfoVo vo = new GiftEventInfoVo().setEventName("");
        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftEventInfo(vo));
        assertEquals("事由名称不能为空", ex.getMsg());

        vo.setEventName("   ");
        ex = assertThrows(FinanceException.class, () -> service.addGiftEventInfo(vo));
        assertEquals("事由名称不能为空", ex.getMsg());
    }

    @Test
    void addEvent_should_reject_when_name_too_long() {
        GiftEventInfoVo vo = new GiftEventInfoVo().setEventName("E".repeat(101));
        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftEventInfo(vo));
        assertEquals("事由名称最多100个字符", ex.getMsg());
    }

    @Test
    void addEvent_should_reject_when_hostPerson_belongs_to_other_org() {
        when(giftPersonInfoMapper.selectById(99L)).thenReturn(person(99L, OTHER_USER_ID, OTHER_ORG_ID));

        GiftEventInfoVo vo = new GiftEventInfoVo()
                .setEventName("婚礼")
                .setHostPersonId(99L);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftEventInfo(vo));
        assertEquals("无权访问其他机构的亲友", ex.getMsg());
    }

    @Test
    void addEvent_should_allow_when_hostPerson_in_same_org() {
        when(giftPersonInfoMapper.selectById(10L)).thenReturn(person(10L, OTHER_USER_ID, LOGIN_ORG_ID));

        GiftEventInfoVo vo = new GiftEventInfoVo()
                .setEventName("生日宴")
                .setHostPersonId(10L);

        GiftEventInfoVo saved = service.addGiftEventInfo(vo);
        assertNotNull(saved.getId());
        assertEquals(LOGIN_USER_ID, saved.getUserId());
        assertEquals(LOGIN_ORG_ID, saved.getOrgId());
        verify(giftEventInfoMapper).insert(any(GiftEventInfo.class));
    }

    @Test
    void addEvent_should_override_orgId_and_userId_with_login_user() {
        GiftEventInfoVo vo = new GiftEventInfoVo()
                .setEventName("乔迁之喜")
                .setUserId(999L)
                .setOrgId(888L);

        GiftEventInfoVo saved = service.addGiftEventInfo(vo);
        assertNotNull(saved.getId());
        assertEquals(LOGIN_USER_ID, saved.getUserId());
        assertEquals(LOGIN_ORG_ID, saved.getOrgId());
    }

    @Test
    void updateEvent_should_reject_when_id_is_null() {
        GiftEventInfoVo vo = new GiftEventInfoVo().setEventName("满月酒");
        FinanceException ex = assertThrows(FinanceException.class, () -> service.updateGiftEventInfo(vo));
        assertEquals("事由ID不能为空", ex.getMsg());
    }

    @Test
    void updateEvent_should_reject_cross_org() {
        GiftEventInfo existing = event(2L, OTHER_USER_ID, OTHER_ORG_ID);
        when(giftEventInfoMapper.selectById(2L)).thenReturn(existing);

        GiftEventInfoVo update = new GiftEventInfoVo().setEventName("修改事由");
        update.setId(2L);
        FinanceException ex = assertThrows(FinanceException.class, () -> service.updateGiftEventInfo(update));
        assertEquals("无权访问其他机构的事由", ex.getMsg());
    }

    @Test
    void updateEvent_should_allow_same_org() {
        GiftEventInfo existing = event(3L, OTHER_USER_ID, LOGIN_ORG_ID);
        when(giftEventInfoMapper.selectById(3L)).thenReturn(existing);
        when(giftEventInfoMapper.updateById(any(GiftEventInfo.class))).thenReturn(1);

        GiftEventInfoVo update = new GiftEventInfoVo().setEventName("更新事由");
        update.setId(3L);
        Boolean ok = service.updateGiftEventInfo(update);
        assertTrue(ok);
    }

    @Test
    void deleteEvent_should_reject_cross_org() {
        GiftEventInfo existing = event(4L, OTHER_USER_ID, OTHER_ORG_ID);
        when(giftEventInfoMapper.selectById(4L)).thenReturn(existing);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteGiftEventInfo("4"));
        assertEquals("无权访问其他机构的事由", ex.getMsg());
    }

    @Test
    void deleteEvent_should_reject_invalid_id_format() {
        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteGiftEventInfo("1,abc,3"));
        assertEquals("事由ID格式不合法", ex.getMsg());
    }

    @Test
    @DisplayName("H17: monthPendingCount_should_respect_user_timezone - 当月待办事件时区安全统计与聚合")
    void monthPendingCount_should_respect_user_timezone() {
        LocalDateTime now = com.alex.common.utils.date.DateUtils.now();
        GiftEventInfo currentMonthEvent = event(10L, LOGIN_USER_ID, LOGIN_ORG_ID).setEventTime(now);
        GiftEventInfo lastMonthEvent = event(11L, LOGIN_USER_ID, LOGIN_ORG_ID).setEventTime(now.minusMonths(1));
        GiftEventInfo nextMonthEvent = event(12L, LOGIN_USER_ID, LOGIN_ORG_ID).setEventTime(now.plusMonths(1));
        GiftEventInfo nullTimeEvent = event(13L, LOGIN_USER_ID, LOGIN_ORG_ID).setEventTime(null);

        when(giftEventInfoMapper.listEntities(null)).thenReturn(
                java.util.List.of(currentMonthEvent, lastMonthEvent, nextMonthEvent, nullTimeEvent));

        com.alex.api.finance.gift.record.vo.GiftRecordInfoVo r1 = new com.alex.api.finance.gift.record.vo.GiftRecordInfoVo();
        r1.setAmount(new java.math.BigDecimal("200.00"));
        r1.setGiverPersonId(101L);
        r1.setReceiverPersonId(102L);

        com.alex.api.finance.gift.record.vo.GiftRecordInfoVo r2 = new com.alex.api.finance.gift.record.vo.GiftRecordInfoVo();
        r2.setAmount(new java.math.BigDecimal("300.50"));
        r2.setGiverPersonId(101L); // duplicate giver
        r2.setReceiverPersonId(null);

        when(giftRecordInfoService.getList(any())).thenReturn(java.util.List.of(r1, r2));

        com.alex.api.finance.gift.event.vo.GiftEventSummaryVo summary = service.getSummary();
        assertEquals(1L, summary.getMonthPendingCount(), "仅统计当月待办事由");
        assertEquals(new java.math.BigDecimal("500.50"), summary.getTotalAmount(), "礼金金额求和准确");
        assertEquals(2L, summary.getActivePersonCount(), "活跃亲友去重统计为2 (101L, 102L)");
    }

    @Test
    @DisplayName("summary_should_handle_empty_events_and_records - 空事件与空记录安全兜底")
    void summary_should_handle_empty_events_and_records() {
        when(giftEventInfoMapper.listEntities(null)).thenReturn(java.util.List.of());
        when(giftRecordInfoService.getList(any())).thenReturn(java.util.List.of());

        com.alex.api.finance.gift.event.vo.GiftEventSummaryVo summary = service.getSummary();
        assertEquals(0L, summary.getMonthPendingCount());
        assertEquals(java.math.BigDecimal.ZERO, summary.getTotalAmount());
        assertEquals(0L, summary.getActivePersonCount());
    }

    @Test
    @DisplayName("getBusinessPage_pagination_boundaries - 分页参数默认值与越界回退")
    void getBusinessPage_pagination_boundaries() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.alex.api.finance.gift.event.vo.GiftEventBusinessVo> pageResult =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 0);
        when(giftEventInfoMapper.getBusinessPage(any(), any())).thenReturn(pageResult);

        // pageNum=null, pageSize=null 自动降级为 1 与 10
        service.getBusinessPage(null, null, new com.alex.api.finance.gift.event.query.GiftEventQuery());
        org.mockito.ArgumentCaptor<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.alex.api.finance.gift.event.vo.GiftEventBusinessVo>> captor =
                org.mockito.ArgumentCaptor.forClass(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class);
        verify(giftEventInfoMapper).getBusinessPage(captor.capture(), any());
        assertEquals(1L, captor.getValue().getCurrent());
        assertEquals(10L, captor.getValue().getSize());
    }

    @Test
    @DisplayName("updateEvent_should_reject_invalid_name - 更新事由名称空与超长拦截")
    void updateEvent_should_reject_invalid_name() {
        GiftEventInfo existing = event(5L, LOGIN_USER_ID, LOGIN_ORG_ID);
        when(giftEventInfoMapper.selectById(5L)).thenReturn(existing);

        GiftEventInfoVo emptyVo = new GiftEventInfoVo().setEventName("");
        emptyVo.setId(5L);
        FinanceException ex1 = assertThrows(FinanceException.class, () -> service.updateGiftEventInfo(emptyVo));
        assertEquals("事由名称不能为空", ex1.getMsg());

        GiftEventInfoVo tooLongVo = new GiftEventInfoVo().setEventName("L".repeat(101));
        tooLongVo.setId(5L);
        FinanceException ex2 = assertThrows(FinanceException.class, () -> service.updateGiftEventInfo(tooLongVo));
        assertEquals("事由名称最多100个字符", ex2.getMsg());
    }

    @Test
    @DisplayName("deleteEvent_should_skip_when_ids_empty - 删除空ids静默跳过")
    void deleteEvent_should_skip_when_ids_empty() {
        Boolean res1 = service.deleteGiftEventInfo(null);
        assertTrue(res1);

        Boolean res2 = service.deleteGiftEventInfo("   ");
        assertTrue(res2);
    }

    private GiftEventInfo event(Long id, Long userId, Long orgId) {
        GiftEventInfo event = new GiftEventInfo().setEventName("test event");
        event.setId(id);
        event.setUserId(userId);
        event.setOrgId(orgId);
        return event;
    }

    private GiftPersonInfo person(Long id, Long userId, Long orgId) {
        GiftPersonInfo person = new GiftPersonInfo().setPersonName("test person");
        person.setId(id);
        person.setUserId(userId);
        person.setOrgId(orgId);
        return person;
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
