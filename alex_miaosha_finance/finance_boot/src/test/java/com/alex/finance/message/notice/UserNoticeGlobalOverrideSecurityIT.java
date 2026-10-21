package com.alex.finance.message.notice;

import com.alex.api.finance.message.vo.UserNoticeConfigVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.message.entity.UserNoticeConfig;
import com.alex.finance.message.enums.NoticeBizType;
import com.alex.finance.message.mapper.UserNoticeConfigMapper;
import com.alex.finance.message.service.impl.UserNoticeConfigServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("用户个性化消息通知：GLOBAL一票否决与安全边界集成测试")
class UserNoticeGlobalOverrideSecurityIT {

    @Mock
    private UserNoticeConfigMapper userNoticeConfigMapper;

    @InjectMocks
    private UserNoticeConfigServiceImpl userNoticeConfigService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userNoticeConfigService, "baseMapper", userNoticeConfigMapper);
    }

    @Test
    @DisplayName("GLOBAL站内信总关：各子业务无论开启与否均被一票否决，微信通道不受影响")
    void testGlobalInternalDisabled_overridesAllChildBizTypes() {
        Long userId = 88001L;

        // GLOBAL: 站内信关闭(0), 微信开启(1)
        UserNoticeConfig globalCfg = new UserNoticeConfig();
        globalCfg.setId(1L);
        globalCfg.setUserId(userId);
        globalCfg.setNoticeCode(NoticeBizType.GLOBAL.getCode());
        globalCfg.setInternalEnabled(false);
        globalCfg.setWechatEnabled(true);

        // GIFT_EVENT: 站内信开启(1), 微信开启(1)
        UserNoticeConfig giftCfg = new UserNoticeConfig();
        giftCfg.setId(2L);
        giftCfg.setUserId(userId);
        giftCfg.setNoticeCode(NoticeBizType.GIFT_EVENT.getCode());
        giftCfg.setInternalEnabled(true);
        giftCfg.setWechatEnabled(true);

        when(userNoticeConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(globalCfg, giftCfg));

        // 验证：站内信被 GLOBAL 一票否决
        boolean internalEnabled = userNoticeConfigService.isInternalEnabled(userId, NoticeBizType.GIFT_EVENT.getCode());
        assertFalse(internalEnabled, "GLOBAL站内信关闭时，GIFT_EVENT即使开启也必须被一票否决");

        // 验证：微信通道正交独立，依然开启
        boolean wechatEnabled = userNoticeConfigService.isWechatEnabled(userId, NoticeBizType.GIFT_EVENT.getCode());
        assertTrue(wechatEnabled, "GLOBAL站内信关闭不应影响微信通道");
    }

    @Test
    @DisplayName("GLOBAL微信总关：各子业务无论开启与否均被一票否决，站内信通道不受影响")
    void testGlobalWechatDisabled_overridesAllChildBizTypes() {
        Long userId = 88002L;

        // GLOBAL: 站内信开启(1), 微信关闭(0)
        UserNoticeConfig globalCfg = new UserNoticeConfig();
        globalCfg.setId(10L);
        globalCfg.setUserId(userId);
        globalCfg.setNoticeCode(NoticeBizType.GLOBAL.getCode());
        globalCfg.setInternalEnabled(true);
        globalCfg.setWechatEnabled(false);

        // COUPON: 站内信开启(1), 微信开启(1)
        UserNoticeConfig couponCfg = new UserNoticeConfig();
        couponCfg.setId(11L);
        couponCfg.setUserId(userId);
        couponCfg.setNoticeCode(NoticeBizType.COUPON.getCode());
        couponCfg.setInternalEnabled(true);
        couponCfg.setWechatEnabled(true);

        when(userNoticeConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(globalCfg, couponCfg));

        // 验证：微信被 GLOBAL 一票否决
        boolean wechatEnabled = userNoticeConfigService.isWechatEnabled(userId, NoticeBizType.COUPON.getCode());
        assertFalse(wechatEnabled, "GLOBAL微信关闭时，COUPON即使开启也必须被一票否决");

        // 验证：站内信通道正交独立，依然开启
        boolean internalEnabled = userNoticeConfigService.isInternalEnabled(userId, NoticeBizType.COUPON.getCode());
        assertTrue(internalEnabled, "GLOBAL微信关闭不应影响站内信通道");
    }

    @Test
    @DisplayName("参数防御性校验：非法用户、空编码或不存在编码均抛出合理业务异常")
    void testSaveMyConfig_securityValidations() {
        // 1. 无效用户
        UserNoticeConfigVo vo = new UserNoticeConfigVo();
        vo.setNoticeCode("GLOBAL");
        assertThrows(FinanceException.class, () -> userNoticeConfigService.saveMyConfig(null, vo));
        assertThrows(FinanceException.class, () -> userNoticeConfigService.saveMyConfig(0L, vo));

        // 2. 空 VO 或空编码
        assertThrows(FinanceException.class, () -> userNoticeConfigService.saveMyConfig(1001L, null));
        vo.setNoticeCode("");
        assertThrows(FinanceException.class, () -> userNoticeConfigService.saveMyConfig(1001L, vo));

        // 3. 非法编码 (非 8 大枚举)
        vo.setNoticeCode("ILLEGAL_CODE_ATTACK");
        assertThrows(FinanceException.class, () -> userNoticeConfigService.saveMyConfig(1001L, vo));
    }

    @Test
    @DisplayName("查询安全防御：空用户或空编码安全回退true，不抛异常")
    void testQuerySafetyFallback() {
        assertTrue(userNoticeConfigService.isInternalEnabled(null, "GLOBAL"));
        assertTrue(userNoticeConfigService.isInternalEnabled(-1L, "GLOBAL"));
        assertTrue(userNoticeConfigService.isInternalEnabled(1001L, null));
        assertTrue(userNoticeConfigService.isInternalEnabled(1001L, "   "));

        assertTrue(userNoticeConfigService.isWechatEnabled(null, "GLOBAL"));
        assertTrue(userNoticeConfigService.isWechatEnabled(-1L, "GLOBAL"));
        assertTrue(userNoticeConfigService.isWechatEnabled(1001L, null));
        assertTrue(userNoticeConfigService.isWechatEnabled(1001L, "   "));
    }
}
