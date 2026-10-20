package com.alex.finance.message;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.utils.date.DateUtils;
import com.alex.finance.accountRecordInfo.entity.AccountRecordInfo;
import com.alex.finance.accountRecordInfo.mapper.AccountRecordInfoMapper;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.finance.mapper.FinanceBudgetInfoMapper;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.message.entity.SysMessageInfo;
import com.alex.finance.message.mapper.SysMessageInfoMapper;
import com.alex.finance.message.service.impl.SysMessageInfoServiceImpl;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.alex.finance.wechat.service.WeChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 系统消息与多业务到期提醒中心：全生命周期状态机、定时巡检幂等、防复活与微信降级集成测试
 */
class SysMessageLifecycleAndDedupIT {

    private SysMessageInfoMapper sysMessageInfoMapper;
    private GiftDataScopeSupport scopeSupport;
    private GiftEventInfoMapper giftEventInfoMapper;
    private CpnCouponInfoMapper cpnCouponInfoMapper;
    private AccountRecordInfoMapper accountRecordInfoMapper;
    private PrepaidCardInfoTMapper prepaidCardInfoTMapper;
    private GiftRecordInfoMapper giftRecordInfoMapper;
    private GiftPersonInfoMapper giftPersonInfoMapper;
    private FinanceBudgetInfoMapper financeBudgetInfoMapper;
    private FinanceInfoMapper financeInfoMapper;
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
        giftRecordInfoMapper = mock(GiftRecordInfoMapper.class);
        giftPersonInfoMapper = mock(GiftPersonInfoMapper.class);
        financeBudgetInfoMapper = mock(FinanceBudgetInfoMapper.class);
        financeInfoMapper = mock(FinanceInfoMapper.class);
        weChatService = mock(WeChatService.class);

        service = new SysMessageInfoServiceImpl(
                scopeSupport,
                giftEventInfoMapper,
                cpnCouponInfoMapper,
                accountRecordInfoMapper,
                prepaidCardInfoTMapper,
                giftRecordInfoMapper,
                giftPersonInfoMapper,
                financeBudgetInfoMapper,
                financeInfoMapper,
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
    @DisplayName("巡检扫描与插入：executeRemindJob 应精准聚合礼金、消费券、猫超卡与预付卡提醒并触发微信模板推送")
    void executeRemindJob_aggregatesMultiBizRemindersAndNotifiesWeChat() throws Exception {
        LocalDateTime now = DateUtils.now();

        // 1. 礼尚往来事件：今天举行
        GiftEventInfo event = new GiftEventInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setEventName("李四百日宴")
                .setEventType("百日宴")
                .setEventTime(now);
        event.setId(301L);
        when(giftEventInfoMapper.selectList(any())).thenReturn(List.of(event));

        // 2. 消费券：剩余 1 张，明天到期
        CpnCouponInfoVo coupon = new CpnCouponInfoVo()
                .setCouponName("天猫50元品类券")
                .setRemainingQuantity(1)
                .setUnitValue(new BigDecimal("50"))
                .setEndDate(now.plusDays(1));
        coupon.setId(401L);
        coupon.setCreator(1001L);
        when(cpnCouponInfoMapper.getList(any())).thenReturn(List.of(coupon));

        // 3. 预付卡：还有 200 元余额，今天到期
        PrepaidCardInfoTVo card = new PrepaidCardInfoTVo()
                .setCardName("星巴克星礼卡")
                .setUserId(1001L)
                .setCurrentBalance(new BigDecimal("200.00"))
                .setExpireDate(now);
        card.setId(501L);
        when(prepaidCardInfoTMapper.getList(any())).thenReturn(List.of(card));

        // 4. 猫超卡：2 天后到期
        AccountRecordInfo tmall = new AccountRecordInfo()
                .setId(601L)
                .setName("猫超生鲜卡")
                .setAmount(new BigDecimal("100.00"))
                .setAccount("card-100")
                .setAvliDate(now.plusDays(2));
        when(accountRecordInfoMapper.selectList(any())).thenReturn(List.of(tmall));

        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection())).thenReturn(Collections.emptyList());

        int insertedCount = service.executeRemindJob();

        // 预期入库 4 条不同类型的提醒
        assertEquals(4, insertedCount);
        verify(sysMessageInfoMapper, times(4)).insert(any(SysMessageInfo.class));

        // 验证微信模板消息：触发 3 次（礼金、消费券、预付卡，猫超卡由独立 Job 汇总）
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), eq("今日举行"), eq("礼尚往来事件"));
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), eq("明日到期(剩1天)"), eq("消费券到期"));
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), eq("今日到期"), eq("消费卡到期"));
    }

    @Test
    @DisplayName("巡检幂等契约：同一巡检周期内重复执行 executeRemindJob，新增条数恒等于 0")
    void executeRemindJob_secondExecution_mustBeIdempotentZeroInsert() throws Exception {
        LocalDateTime now = DateUtils.now();
        GiftEventInfo event = new GiftEventInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setEventName("周年庆典")
                .setEventTime(now);
        event.setId(701L);
        when(giftEventInfoMapper.selectList(any())).thenReturn(List.of(event));

        // 模拟数据库已存在该 dedupKey
        String existingKey = "GIFT_EVENT:701:D_DAY:" + DateUtils.FORMATTER_YYYY_MM_DD.format(now.toLocalDate()) + ":1001";
        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection())).thenReturn(List.of(existingKey));

        int insertedCount = service.executeRemindJob();

        assertEquals(0, insertedCount, "已存在 dedupKey 时必须 100% 幂等阻断");
        verify(sysMessageInfoMapper, never()).insert(any(SysMessageInfo.class));
        verify(weChatService, never()).sendRemindTemplateMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("防复活契约：用户主动删除消息后，后续调度再次巡检时绝不二次插入复活已删消息")
    void executeRemindJob_deletedMessage_mustNotBeResurrected() {
        LocalDateTime now = DateUtils.now();
        CpnCouponInfoVo coupon = new CpnCouponInfoVo()
                .setCouponName("已删除提醒的消费券")
                .setRemainingQuantity(5)
                .setEndDate(now.plusDays(1));
        coupon.setId(888L);
        coupon.setCreator(1001L);
        when(cpnCouponInfoMapper.getList(any())).thenReturn(List.of(coupon));

        // 模拟已存在记录（即使被用户软删除 is_delete=1，selectExistingDedupKeys 依然返回其 dedupKey）
        String dedupKey = "COUPON:888:D_MINUS_1:" + DateUtils.FORMATTER_YYYY_MM_DD.format(now.toLocalDate()) + ":1001";
        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection())).thenReturn(List.of(dedupKey));

        int insertedCount = service.executeRemindJob();

        assertEquals(0, insertedCount, "已删除历史消息命中 dedupKey 严禁复活插入");
        verify(sysMessageInfoMapper, never()).insert(any(SysMessageInfo.class));
    }

    @Test
    @DisplayName("微信推送容错：微信服务网络超时或异常时平滑降级，站内信入库事务不回滚")
    void executeRemindJob_weChatError_shouldGracefullyDegradeWithoutRollback() throws Exception {
        LocalDateTime now = DateUtils.now();
        GiftEventInfo event = new GiftEventInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setEventName("亲友婚礼")
                .setEventTime(now);
        event.setId(901L);
        when(giftEventInfoMapper.selectList(any())).thenReturn(List.of(event));
        when(sysMessageInfoMapper.selectExistingDedupKeys(anyCollection())).thenReturn(Collections.emptyList());

        // 模拟微信接口网络故障
        doThrow(new RuntimeException("WeChat API Connection Timeout 504"))
                .when(weChatService).sendRemindTemplateMessage(anyString(), anyString(), anyString());

        int count = service.executeRemindJob();

        assertEquals(1, count, "微信异常时站内信依然必须成功入库");
        verify(sysMessageInfoMapper, times(1)).insert(any(SysMessageInfo.class));
    }

    @Test
    @DisplayName("消息已读状态机流转：未读标为已读成功更新，已读重复标记保持幂等")
    void markRead_stateTransitionAndIdempotency() {
        // 1. 未读消息 (read_status = 0) -> 标为已读
        SysMessageInfo unreadMsg = new SysMessageInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setReadStatus(0);
        unreadMsg.setId(10L);
        when(sysMessageInfoMapper.selectById(10L)).thenReturn(unreadMsg);
        when(sysMessageInfoMapper.updateById(any(SysMessageInfo.class))).thenReturn(1);

        Boolean result1 = service.markRead(10L);
        assertTrue(result1);
        assertEquals(1, unreadMsg.getReadStatus());
        assertNotNull(unreadMsg.getReadTime());
        verify(sysMessageInfoMapper, times(1)).updateById(any(SysMessageInfo.class));

        // 2. 已读消息 (read_status = 1) -> 重复标为已读幂等返回 true，不执行 updateById
        SysMessageInfo alreadyReadMsg = new SysMessageInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setReadStatus(1)
                .setReadTime(LocalDateTime.of(2026, 10, 1, 12, 0, 0));
        alreadyReadMsg.setId(20L);
        when(sysMessageInfoMapper.selectById(20L)).thenReturn(alreadyReadMsg);

        Boolean result2 = service.markRead(20L);
        assertTrue(result2);
        // 强断言：旧的阅读时间未被篡改覆盖
        assertEquals(LocalDateTime.of(2026, 10, 1, 12, 0, 0), alreadyReadMsg.getReadTime());
        verify(sysMessageInfoMapper, never()).updateById(alreadyReadMsg);
    }
}
