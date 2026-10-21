package com.alex.finance.message.notice;

import com.alex.api.finance.message.vo.UserNoticeConfigVo;
import com.alex.finance.message.enums.NoticeBizType;
import com.alex.finance.message.mapper.UserNoticeConfigMapper;
import com.alex.finance.message.service.impl.UserNoticeConfigServiceImpl;
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
import static org.mockito.Mockito.when;

@DisplayName("用户个性化消息通知：默认全发送策略集成测试")
class UserNoticeDefaultPolicyIT {

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
    @DisplayName("1. 未配置的新用户调用 isInternalEnabled 与 isWechatEnabled 恒定默认返回 true (全渠道放行)")
    void testNewUserDefaultAllow() {
        // 模拟 DB 中无任何记录
        when(userNoticeConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

        Long newUserId = 99999L;
        for (NoticeBizType bizType : NoticeBizType.values()) {
            assertTrue(userNoticeConfigService.isInternalEnabled(newUserId, bizType.getCode()),
                    bizType.getCode() + " 站内信未配置时默认应放行");
            assertTrue(userNoticeConfigService.isWechatEnabled(newUserId, bizType.getCode()),
                    bizType.getCode() + " 微信未配置时默认应放行");
        }
    }

    @Test
    @DisplayName("2. 新用户查询 getMyConfigs 成功返回完整的 8 大业务项且开关全为 1")
    void testGetMyConfigsReturnsAll8ItemsWithDefaultOpen() {
        when(userNoticeConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

        Long newUserId = 99999L;
        List<UserNoticeConfigVo> configs = userNoticeConfigService.getMyConfigs(newUserId);

        assertNotNull(configs);
        assertEquals(NoticeBizType.values().length, configs.size(), "必须完整返回 8 大业务项");

        for (UserNoticeConfigVo vo : configs) {
            assertNotNull(vo.getNoticeCode());
            assertNotNull(vo.getNoticeName());
            assertNotNull(vo.getNoticeDesc());
            assertNull(vo.getId(), "未落库时 ID 应为 null");
            assertTrue(vo.getInternalEnabled(), vo.getNoticeCode() + " 默认站内信应为开启");
            assertTrue(vo.getWechatEnabled(), vo.getNoticeCode() + " 默认微信应为开启");
        }
    }
}
