package com.alex.finance.message;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.api.finance.message.vo.SysMessageInfoVo;
import com.alex.api.finance.message.vo.SysMessageSummaryVo;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.date.DateUtils;
import com.alex.finance.accountRecordInfo.entity.AccountRecordInfo;
import com.alex.finance.accountRecordInfo.mapper.AccountRecordInfoMapper;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.message.entity.SysMessageInfo;
import com.alex.finance.message.mapper.SysMessageInfoMapper;
import com.alex.finance.message.service.impl.SysMessageInfoServiceImpl;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.alex.finance.wechat.service.WeChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysMessageInfoServiceTest {

    private SysMessageInfoMapper sysMessageInfoMapper;
    private GiftDataScopeSupport scopeSupport;
    private GiftEventInfoMapper giftEventInfoMapper;
    private CpnCouponInfoMapper cpnCouponInfoMapper;
    private AccountRecordInfoMapper accountRecordInfoMapper;
    private PrepaidCardInfoTMapper prepaidCardInfoTMapper;
    private com.alex.finance.gift.record.mapper.GiftRecordInfoMapper giftRecordInfoMapper;
    private com.alex.finance.gift.person.mapper.GiftPersonInfoMapper giftPersonInfoMapper;
    private WeChatService weChatService;
    private SysMessageInfoServiceImpl service;

    private TUserVo currentUser;

    @BeforeEach
    void setUp() {
        sysMessageInfoMapper = mock(SysMessageInfoMapper.class);
        scopeSupport = mock(GiftDataScopeSupport.class);
        giftEventInfoMapper = mock(GiftEventInfoMapper.class);
        cpnCouponInfoMapper = mock(CpnCouponInfoMapper.class);
        accountRecordInfoMapper = mock(AccountRecordInfoMapper.class);
        prepaidCardInfoTMapper = mock(PrepaidCardInfoTMapper.class);
        giftRecordInfoMapper = mock(com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.class);
        giftPersonInfoMapper = mock(com.alex.finance.gift.person.mapper.GiftPersonInfoMapper.class);
        weChatService = mock(WeChatService.class);

        service = new SysMessageInfoServiceImpl(
                scopeSupport,
                giftEventInfoMapper,
                cpnCouponInfoMapper,
                accountRecordInfoMapper,
                prepaidCardInfoTMapper,
                giftRecordInfoMapper,
                giftPersonInfoMapper,
                weChatService
        );
        ReflectionTestUtils.setField(service, "baseMapper", sysMessageInfoMapper);

        currentUser = new TUserVo();
        currentUser.setId(1001L);
        currentUser.setOrgId(2001L);
        when(scopeSupport.requireLoginUser()).thenReturn(currentUser);
        when(scopeSupport.loginOrgId(currentUser)).thenReturn(2001L);
        when(scopeSupport.isSuper(currentUser)).thenReturn(false);
    }

    @Test
    @DisplayName("executeRemindJob：XXL-Job无登录态后台定时任务应扫描全量礼金事件、消费券、猫超卡与预付卡并联动微信模板推送")
    void testExecuteRemindJobWithoutLoginContext() throws Exception {
        // 模拟 XXL-Job 后台线程：如果调用 requireLoginUser 则直接抛未登录异常
        when(scopeSupport.requireLoginUser()).thenThrow(FinanceException.notLogin());

        LocalDateTime now = DateUtils.now();

        // 1. 礼尚往来事件：属于用户 2002（今天举行）与用户 3003（明天举行）
        GiftEventInfo todayEvent = new GiftEventInfo()
                .setUserId(2002L)
                .setOrgId(8001L)
                .setEventName("张三婚礼")
                .setEventType("婚礼")
                .setEventTime(now);
        todayEvent.setId(501L);

        GiftEventInfo tomorrowEvent = new GiftEventInfo()
                .setUserId(3003L)
                .setOrgId(8002L)
                .setEventName("李四乔迁")
                .setEventType("乔迁")
                .setEventTime(now.plusDays(1));
        tomorrowEvent.setId(502L);

        when(giftEventInfoMapper.selectList(any()))
                .thenReturn(Arrays.asList(todayEvent, tomorrowEvent));

        // 2. 消费券：创建人为 2002，明天到期且剩余 2 张
        CpnCouponInfoVo validCoupon = new CpnCouponInfoVo()
                .setCouponName("京东满100减20券")
                .setRemainingQuantity(2)
                .setUnitValue(new BigDecimal("20"))
                .setEndDate(now.plusDays(1));
        validCoupon.setId(601L);
        validCoupon.setCreator(2002L);

        when(cpnCouponInfoMapper.getList(any()))
                .thenReturn(Collections.singletonList(validCoupon));

        // 3. 猫超卡：2天后到期
        AccountRecordInfo tmallCard = new AccountRecordInfo()
                .setId(701L)
                .setName("天猫超市享淘卡")
                .setAmount(new BigDecimal("88.50"))
                .setAccount("13800000000")
                .setAvliDate(now.plusDays(2));
        when(accountRecordInfoMapper.selectList(any()))
                .thenReturn(Collections.singletonList(tmallCard));

        // 4. 预付消费卡：持卡人 3003，当天到期且余额 > 0
        PrepaidCardInfoTVo activeCard = new PrepaidCardInfoTVo()
                .setCardName("盒马礼品卡")
                .setUserId(3003L)
                .setCurrentBalance(new BigDecimal("150.00"))
                .setExpireDate(now);
        activeCard.setId(801L);

        when(prepaidCardInfoTMapper.getList(any()))
                .thenReturn(Collections.singletonList(activeCard));

        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection()))
                .thenReturn(Collections.emptyList());

        int count = service.executeRemindJob();

        // 预期生成 5 条：2礼金事件(501,502) + 1消费券(601) + 1猫超卡(701) + 1预付卡(801)
        Assertions.assertEquals(5, count);

        ArgumentCaptor<SysMessageInfo> captor = ArgumentCaptor.forClass(SysMessageInfo.class);
        verify(sysMessageInfoMapper, times(5)).insert(captor.capture());

        // 验证微信模板消息联动推送：2条礼尚往来事件 + 1条消费券到期 + 1条预付消费卡到期 = 4次推送
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), eq("今日举行"), eq("礼尚往来事件"));
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), eq("明日举行"), eq("礼尚往来事件"));
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), eq("明日到期(剩1天)"), eq("消费券到期"));
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), eq("今日到期"), eq("消费卡到期"));

        List<SysMessageInfo> inserted = captor.getAllValues();
        SysMessageInfo giftMsg1 = inserted.stream()
                .filter(m -> "501".equals(m.getBizId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertEquals(2002L, giftMsg1.getUserId());
        Assertions.assertEquals(8001L, giftMsg1.getOrgId());
        Assertions.assertEquals("D_DAY", giftMsg1.getSubType());

        SysMessageInfo giftMsg2 = inserted.stream()
                .filter(m -> "502".equals(m.getBizId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertEquals(3003L, giftMsg2.getUserId());
        Assertions.assertEquals(8002L, giftMsg2.getOrgId());
        Assertions.assertEquals("D_MINUS_1", giftMsg2.getSubType());
    }

    @Test
    @DisplayName("executeRemindJob：微信推送异常时应优雅降级，不回滚站内信入库")
    void testWeChatFailureDoesNotRollbackMessageInsert() throws Exception {
        LocalDateTime now = DateUtils.now();
        GiftEventInfo todayEvent = new GiftEventInfo()
                .setUserId(2002L)
                .setOrgId(8001L)
                .setEventName("张三婚礼")
                .setEventTime(now);
        todayEvent.setId(501L);

        when(giftEventInfoMapper.selectList(any())).thenReturn(Collections.singletonList(todayEvent));
        when(cpnCouponInfoMapper.getList(any())).thenReturn(Collections.emptyList());
        when(accountRecordInfoMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(prepaidCardInfoTMapper.getList(any())).thenReturn(Collections.emptyList());
        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection())).thenReturn(Collections.emptyList());

        doThrow(new RuntimeException("WeChat Network Error"))
                .when(weChatService).sendRemindTemplateMessage(anyString(), anyString(), anyString());

        int count = service.executeRemindJob();
        Assertions.assertEquals(1, count);
        verify(sysMessageInfoMapper, times(1)).insert(any(SysMessageInfo.class));
    }

    @Test
    @DisplayName("executeRemindJob：应过滤远期礼金事件、已核销消费券、远期消费券与空余额预付卡")
    void testExecuteRemindJobBoundaryFiltering() {
        LocalDateTime now = DateUtils.now();

        GiftEventInfo todayEvent = new GiftEventInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setEventName("张三婚礼")
                .setEventType("婚礼")
                .setEventTime(now);
        todayEvent.setId(501L);

        GiftEventInfo twoDaysLaterEvent = new GiftEventInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setEventName("王五满月")
                .setEventTime(now.plusDays(2));
        twoDaysLaterEvent.setId(503L);

        when(giftEventInfoMapper.selectList(any()))
                .thenReturn(Arrays.asList(todayEvent, twoDaysLaterEvent));

        CpnCouponInfoVo validCoupon = new CpnCouponInfoVo()
                .setCouponName("京东满100减20券")
                .setRemainingQuantity(2)
                .setUnitValue(new BigDecimal("20"))
                .setEndDate(now.plusDays(1));
        validCoupon.setId(601L);
        validCoupon.setCreator(1001L);

        CpnCouponInfoVo redeemedCoupon = new CpnCouponInfoVo()
                .setCouponName("已用完消费券")
                .setRemainingQuantity(0)
                .setEndDate(now);
        redeemedCoupon.setId(602L);

        CpnCouponInfoVo farCoupon = new CpnCouponInfoVo()
                .setCouponName("远期消费券")
                .setRemainingQuantity(1)
                .setEndDate(now.plusDays(5));
        farCoupon.setId(603L);

        when(cpnCouponInfoMapper.getList(any()))
                .thenReturn(Arrays.asList(validCoupon, redeemedCoupon, farCoupon));

        AccountRecordInfo tmallCard = new AccountRecordInfo()
                .setId(701L)
                .setName("天猫超市享淘卡")
                .setAmount(new BigDecimal("88.50"))
                .setAccount("13800000000")
                .setAvliDate(now.plusDays(2));
        when(accountRecordInfoMapper.selectList(any()))
                .thenReturn(Collections.singletonList(tmallCard));

        PrepaidCardInfoTVo activeCard = new PrepaidCardInfoTVo()
                .setCardName("盒马礼品卡")
                .setUserId(1001L)
                .setCurrentBalance(new BigDecimal("150.00"))
                .setExpireDate(now);
        activeCard.setId(801L);

        PrepaidCardInfoTVo zeroCard = new PrepaidCardInfoTVo()
                .setCardName("空余额卡")
                .setUserId(1001L)
                .setCurrentBalance(BigDecimal.ZERO)
                .setExpireDate(now);
        zeroCard.setId(802L);

        when(prepaidCardInfoTMapper.getList(any()))
                .thenReturn(Arrays.asList(activeCard, zeroCard));

        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection()))
                .thenReturn(Collections.emptyList());

        int count = service.executeRemindJob();

        // 仅 501(今日事件) + 601(有效消费券) + 701(猫超卡) + 801(有余额预付卡) = 4 条
        Assertions.assertEquals(4, count);

        ArgumentCaptor<SysMessageInfo> captor = ArgumentCaptor.forClass(SysMessageInfo.class);
        verify(sysMessageInfoMapper, times(4)).insert(captor.capture());

        List<SysMessageInfo> inserted = captor.getAllValues();
        Set<String> msgTypes = inserted.stream().map(SysMessageInfo::getMsgType).collect(Collectors.toSet());
        Assertions.assertTrue(msgTypes.contains(SysMessageInfoServiceImpl.MSG_TYPE_GIFT_EVENT));
        Assertions.assertTrue(msgTypes.contains(SysMessageInfoServiceImpl.MSG_TYPE_COUPON_EXPIRE));
        Assertions.assertTrue(msgTypes.contains(SysMessageInfoServiceImpl.MSG_TYPE_CARD_EXPIRE));
    }

    @Test
    @DisplayName("executeRemindJob：已存在相同 dedupKey 时严格幂等不重复插入且不重发微信")
    void testExecuteRemindJobIdempotentDeduplication() throws Exception {
        LocalDateTime now = DateUtils.now();
        GiftEventInfo todayEvent = new GiftEventInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setEventName("张三婚礼")
                .setEventTime(now);
        todayEvent.setId(501L);

        when(giftEventInfoMapper.selectList(any())).thenReturn(Collections.singletonList(todayEvent));
        when(cpnCouponInfoMapper.getList(any())).thenReturn(Collections.emptyList());
        when(accountRecordInfoMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(prepaidCardInfoTMapper.getList(any())).thenReturn(Collections.emptyList());

        String existingKey = String.format("GIFT_EVENT:501:D_DAY:%s:1001",
                DateUtils.FORMATTER_YYYY_MM_DD.format(now.toLocalDate()));
        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection()))
                .thenReturn(Collections.singletonList(existingKey));

        int count = service.executeRemindJob();
        Assertions.assertEquals(0, count);
        verify(sysMessageInfoMapper, never()).insert(any(SysMessageInfo.class));
        verify(weChatService, never()).sendRemindTemplateMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("getSummary：纯查询按消息大类准确汇总未读数量，不隐式触发全表业务扫描")
    void testGetSummaryCountsByCategoryWithoutImplicitSync() {
        SysMessageInfoVo m1 = new SysMessageInfoVo().setMsgType("SYSTEM");
        SysMessageInfoVo m2 = new SysMessageInfoVo().setMsgType("GIFT_EVENT");
        SysMessageInfoVo m3 = new SysMessageInfoVo().setMsgType("GIFT_EVENT");
        SysMessageInfoVo m4 = new SysMessageInfoVo().setMsgType("COUPON_EXPIRE");
        SysMessageInfoVo m5 = new SysMessageInfoVo().setMsgType("CARD_EXPIRE");

        when(sysMessageInfoMapper.selectUnreadList(1001L, 2001L))
                .thenReturn(Arrays.asList(m1, m2, m3, m4, m5));

        SysMessageSummaryVo summary = service.getSummary();
        Assertions.assertEquals(5L, summary.getUnreadTotal());
        Assertions.assertEquals(1L, summary.getSystemUnread());
        Assertions.assertEquals(2L, summary.getGiftEventUnread());
        Assertions.assertEquals(1L, summary.getCouponExpireUnread());
        Assertions.assertEquals(1L, summary.getCardExpireUnread());

        // 验证 getSummary 纯查询不触发业务表扫描或写入
        verify(giftEventInfoMapper, never()).listEntities(any());
        verify(giftEventInfoMapper, never()).selectList(any());
        verify(sysMessageInfoMapper, never()).insert(any(SysMessageInfo.class));
    }

    @Test
    @DisplayName("markRead：他人消息越权标记已读应抛出权限异常")
    void testMarkReadForbiddenForOtherUser() {
        SysMessageInfo otherMsg = new SysMessageInfo()
                .setUserId(9999L)
                .setOrgId(2001L)
                .setReadStatus(0);
        otherMsg.setId(9001L);

        when(sysMessageInfoMapper.selectById(9001L)).thenReturn(otherMsg);

        Assertions.assertThrows(FinanceException.class, () -> service.markRead(9001L));
    }

    @Test
    @DisplayName("publishSystemMessage：标题或内容为空应抛出参数异常，合法参数正常入库")
    void testPublishSystemMessageValidationAndSuccess() {
        SysMessageInfoVo emptyTitle = new SysMessageInfoVo().setTitle("  ").setContent("正文");
        Assertions.assertThrows(FinanceException.class, () -> service.publishSystemMessage(emptyTitle));

        SysMessageInfoVo validVo = new SysMessageInfoVo()
                .setTitle("年度账单复盘提醒")
                .setContent("别忘了本周末整理家庭消费券与猫超卡余额")
                .setPriority("HIGH");

        SysMessageInfoVo saved = service.publishSystemMessage(validVo);
        Assertions.assertEquals("年度账单复盘提醒", saved.getTitle());
        Assertions.assertEquals("SYSTEM", saved.getMsgType());
        Assertions.assertEquals("HIGH", saved.getPriority());
        Assertions.assertEquals(0, saved.getReadStatus());
        verify(sysMessageInfoMapper, times(1)).insert(any(SysMessageInfo.class));
    }

    @Test
    @DisplayName("executeRemindJob：大额礼金盈余且超180天无互动应生成回礼备忘提醒")
    void testCollectGiftReturnImbalanceReminder() {
        // 模拟亲友与往来流水
        com.alex.finance.gift.record.entity.GiftRecordInfo r1 = new com.alex.finance.gift.record.entity.GiftRecordInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setGiverPersonId(888L)
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("2000.00"))
                .setPayTime(DateUtils.now().minusDays(200));

        com.alex.finance.gift.record.entity.GiftRecordInfo r2 = new com.alex.finance.gift.record.entity.GiftRecordInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setReceiverPersonId(888L)
                .setDirection("GIVE")
                .setAmount(new BigDecimal("500.00"))
                .setPayTime(DateUtils.now().minusDays(190));

        when(giftRecordInfoMapper.selectList(null)).thenReturn(Arrays.asList(r1, r2));

        com.alex.finance.gift.person.entity.GiftPersonInfo person = new com.alex.finance.gift.person.entity.GiftPersonInfo()
                .setPersonName("二舅")
                .setRelationType("亲戚");
        when(giftPersonInfoMapper.selectById(888L)).thenReturn(person);

        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection())).thenReturn(Collections.emptyList());

        int count = service.executeRemindJob();
        Assertions.assertTrue(count >= 1);

        ArgumentCaptor<SysMessageInfo> captor = ArgumentCaptor.forClass(SysMessageInfo.class);
        verify(sysMessageInfoMapper, times(1)).insert(captor.capture());

        SysMessageInfo inserted = captor.getValue();
        Assertions.assertEquals("GIFT_RETURN", inserted.getMsgType());
        Assertions.assertEquals("IMBALANCE", inserted.getSubType());
        Assertions.assertEquals("888", inserted.getBizId());
        Assertions.assertTrue(inserted.getTitle().contains("二舅"));
        Assertions.assertTrue(inserted.getContent().contains("1500"));
        Assertions.assertEquals("/finance/gift/analysis", inserted.getJumpUrl());
    }
}
