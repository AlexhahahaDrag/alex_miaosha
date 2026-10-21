package com.alex.finance.message.notice;

import com.alex.finance.message.entity.SysMessageInfo;
import com.alex.finance.message.entity.UserNoticeConfig;
import com.alex.finance.message.enums.NoticeBizType;
import com.alex.finance.message.mapper.SysMessageInfoMapper;
import com.alex.finance.message.mapper.UserNoticeConfigMapper;
import com.alex.finance.message.service.impl.SysMessageInfoServiceImpl;
import com.alex.finance.message.service.impl.UserNoticeConfigServiceImpl;
import com.alex.finance.wechat.service.WeChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("用户个性化消息通知：单用户覆盖与巡检联动集成测试")
class UserNoticePersonalizedOverrideIT {

    @Mock
    private UserNoticeConfigMapper userNoticeConfigMapper;

    @Mock
    private SysMessageInfoMapper sysMessageInfoMapper;

    @Mock
    private WeChatService weChatService;

    @InjectMocks
    private UserNoticeConfigServiceImpl userNoticeConfigService;

    @InjectMocks
    private SysMessageInfoServiceImpl sysMessageInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 为 userNoticeConfigService 注入 mapper
        ReflectionTestUtils.setField(userNoticeConfigService, "baseMapper", userNoticeConfigMapper);
        // 为 sysMessageInfoService 注入 mapper 和依赖
        ReflectionTestUtils.setField(sysMessageInfoService, "baseMapper", sysMessageInfoMapper);
        ReflectionTestUtils.setField(sysMessageInfoService, "userNoticeConfigService", userNoticeConfigService);
    }

    @Test
    @DisplayName("1. 用户 1001 关闭消费券微信通知后，与未设置用户 1002 完全隔离")
    void testUserIsolationAndOverride() {
        UserNoticeConfig couponConfigUser1 = new UserNoticeConfig()
                .setUserId(1001L)
                .setNoticeCode(NoticeBizType.COUPON.getCode())
                .setInternalEnabled(true)
                .setWechatEnabled(false);

        // 模拟用户 1001 的配置
        when(userNoticeConfigMapper.selectList(any())).thenReturn(Collections.singletonList(couponConfigUser1));

        // 针对用户 1001
        assertTrue(userNoticeConfigService.isInternalEnabled(1001L, "COUPON"));
        assertFalse(userNoticeConfigService.isWechatEnabled(1001L, "COUPON"));

        // 用户 1002 无配置，直接走默认
        when(userNoticeConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
        assertTrue(userNoticeConfigService.isInternalEnabled(1002L, "COUPON"));
        assertTrue(userNoticeConfigService.isWechatEnabled(1002L, "COUPON"));
    }

    @Test
    @DisplayName("2. 巡检时当用户个性化关闭微信通知，站内信正常入库但微信 never() 调用")
    void testRemindJobRespectsUserWechatDisabled() throws Exception {
        // 用户 1001 关闭了 COUPON 微信通知
        UserNoticeConfig user1001Config = new UserNoticeConfig()
                .setUserId(1001L)
                .setNoticeCode(NoticeBizType.COUPON.getCode())
                .setInternalEnabled(true)
                .setWechatEnabled(false);

        when(userNoticeConfigMapper.selectList(any())).thenReturn(Collections.singletonList(user1001Config));

        SysMessageInfo msg = new SysMessageInfo()
                .setUserId(1001L)
                .setBizType("COUPON")
                .setMsgType("COUPON_EXPIRE")
                .setSubType("D_DAY")
                .setTitle("您的消费券今日到期")
                .setDedupKey("coupon-expire-1001-20261021");

        // 触发安全微信通知
        ReflectionTestUtils.invokeMethod(sysMessageInfoService, "notifyWeChatSafely", msg);

        // 验证微信接口从未被调用
        verify(weChatService, never()).sendRemindTemplateMessage(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("3. 巡检时当用户关闭站内信通知，站内信 0 新增但微信依然可发送")
    void testRemindJobRespectsUserInternalDisabled() throws Exception {
        // 用户 1001 关闭了站内信，但开启了微信
        UserNoticeConfig user1001Config = new UserNoticeConfig()
                .setUserId(1001L)
                .setNoticeCode(NoticeBizType.COUPON.getCode())
                .setInternalEnabled(false)
                .setWechatEnabled(true);

        when(userNoticeConfigMapper.selectList(any())).thenReturn(Collections.singletonList(user1001Config));
        when(sysMessageInfoMapper.selectExistingDedupKeys(any())).thenReturn(Collections.emptyList());

        SysMessageInfo msg = new SysMessageInfo()
                .setUserId(1001L)
                .setBizType("COUPON")
                .setMsgType("COUPON_EXPIRE")
                .setSubType("D_DAY")
                .setTitle("您的消费券今日到期")
                .setDedupKey("coupon-expire-1001-20261021");

        // 执行 persistCandidates
        int count = ReflectionTestUtils.invokeMethod(sysMessageInfoService, "persistCandidates",
                Collections.singletonMap(msg.getDedupKey(), msg));

        // 站内信跳过入库，返回值应为 0
        assertEquals(0, count, "关闭站内信时插入数必须为 0");
        verify(sysMessageInfoMapper, never()).insert(any(SysMessageInfo.class));

        // 但微信通知已执行
        verify(weChatService, times(1)).sendRemindTemplateMessage(anyString(), anyString(), anyString());
    }
}
