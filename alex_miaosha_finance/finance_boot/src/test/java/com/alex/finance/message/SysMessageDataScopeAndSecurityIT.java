package com.alex.finance.message;

import com.alex.api.finance.message.vo.SysMessageInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.accountRecordInfo.mapper.AccountRecordInfoMapper;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.finance.mapper.FinanceBudgetInfoMapper;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 系统消息数据权限隔离、越权标记/删除防御与发布权限控制集成测试
 */
class SysMessageDataScopeAndSecurityIT {

    private SysMessageInfoMapper sysMessageInfoMapper;
    private GiftDataScopeSupport scopeSupport;
    private SysMessageInfoServiceImpl service;

    private TUserVo normalUser;
    private TUserVo superUser;

    @BeforeEach
    void setUp() {
        sysMessageInfoMapper = mock(SysMessageInfoMapper.class);
        scopeSupport = mock(GiftDataScopeSupport.class);

        service = spy(new SysMessageInfoServiceImpl(
                scopeSupport,
                mock(GiftEventInfoMapper.class),
                mock(CpnCouponInfoMapper.class),
                mock(AccountRecordInfoMapper.class),
                mock(PrepaidCardInfoTMapper.class),
                mock(GiftRecordInfoMapper.class),
                mock(GiftPersonInfoMapper.class),
                mock(FinanceBudgetInfoMapper.class),
                mock(FinanceInfoMapper.class),
                mock(WeChatService.class)
        ));
        ReflectionTestUtils.setField(service, "baseMapper", sysMessageInfoMapper);

        normalUser = new TUserVo();
        normalUser.setId(1001L);
        normalUser.setOrgId(2001L);

        superUser = new TUserVo();
        superUser.setId(9999L);
        superUser.setOrgId(8888L);
    }

    @Test
    @DisplayName("跨用户已读拦截：普通用户尝试标为已读他人私有消息时强制抛出 403 Forbidden")
    void markRead_crossUserAccess_mustBeForbidden() {
        when(scopeSupport.requireLoginUser()).thenReturn(normalUser);
        when(scopeSupport.loginOrgId(normalUser)).thenReturn(2001L);
        when(scopeSupport.isSuper(normalUser)).thenReturn(false);

        // 归属于用户 2002 的私有消息
        SysMessageInfo otherUserMsg = new SysMessageInfo()
                .setUserId(2002L)
                .setOrgId(2001L)
                .setReadStatus(0);
        otherUserMsg.setId(101L);
        when(sysMessageInfoMapper.selectById(101L)).thenReturn(otherUserMsg);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.markRead(101L));
        assertEquals("403", ex.getCode());
        assertTrue(ex.getMsg().contains("无权操作他人的消息通知"));
        verify(sysMessageInfoMapper, never()).updateById(any(SysMessageInfo.class));
    }

    @Test
    @DisplayName("跨机构已读拦截：普通用户尝试标为已读其他机构广播消息时强制抛出 403 Forbidden")
    void markRead_crossOrgAccess_mustBeForbidden() {
        when(scopeSupport.requireLoginUser()).thenReturn(normalUser);
        when(scopeSupport.loginOrgId(normalUser)).thenReturn(2001L);
        when(scopeSupport.isSuper(normalUser)).thenReturn(false);

        // 归属于机构 3001 的广播消息 (userId = 0/null)
        SysMessageInfo otherOrgMsg = new SysMessageInfo()
                .setUserId(0L)
                .setOrgId(3001L)
                .setReadStatus(0);
        otherOrgMsg.setId(102L);
        when(sysMessageInfoMapper.selectById(102L)).thenReturn(otherOrgMsg);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.markRead(102L));
        assertEquals("403", ex.getCode());
        assertTrue(ex.getMsg().contains("无权操作其他机构的消息通知"));
    }

    @Test
    @DisplayName("已删除或不存在防御：标为已读已被删除的消息时安全抛出 PARAM 异常")
    void markRead_deletedOrNonExistentMessage_mustThrowParamError() {
        when(scopeSupport.requireLoginUser()).thenReturn(normalUser);

        // 1. 记录不存在
        when(sysMessageInfoMapper.selectById(404L)).thenReturn(null);
        FinanceException ex1 = assertThrows(FinanceException.class, () -> service.markRead(404L));
        assertTrue(ex1.getMsg().contains("消息不存在或已被删除"));

        // 2. 软删除记录 (is_delete = 1)
        SysMessageInfo deletedMsg = new SysMessageInfo();
        deletedMsg.setId(405L);
        deletedMsg.setIsDelete(1);
        when(sysMessageInfoMapper.selectById(405L)).thenReturn(deletedMsg);

        FinanceException ex2 = assertThrows(FinanceException.class, () -> service.markRead(405L));
        assertTrue(ex2.getMsg().contains("消息不存在或已被删除"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1,xyz,2", "null", "---", "1,,2"})
    @DisplayName("删除参数防呆：deleteMessages 传入非法非数字字符串时友好提示参数错误，彻底杜绝 500")
    void deleteMessages_invalidNonNumericIds_mustThrowParamErrorNot500(String badIds) {
        when(scopeSupport.requireLoginUser()).thenReturn(normalUser);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteMessages(badIds));
        assertTrue(ex.getMsg().contains("删除消息ID格式不正确"));
        verify(sysMessageInfoMapper, never()).selectById(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("删除空参数防御：ids 为空时明确提示参数异常")
    void deleteMessages_blankIds_mustThrow(String blankIds) {
        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteMessages(blankIds));
        assertTrue(ex.getMsg().contains("删除的消息ID不能为空"));
    }

    @Test
    @DisplayName("跨用户删除防御：普通用户尝试删除他人私有消息时严格抛出 403 Forbidden")
    void deleteMessages_crossUserDelete_mustBeForbidden() {
        when(scopeSupport.requireLoginUser()).thenReturn(normalUser);
        when(scopeSupport.loginOrgId(normalUser)).thenReturn(2001L);
        when(scopeSupport.isSuper(normalUser)).thenReturn(false);

        SysMessageInfo otherMsg = new SysMessageInfo()
                .setUserId(2002L)
                .setOrgId(2001L);
        otherMsg.setId(999L);
        when(sysMessageInfoMapper.selectById(999L)).thenReturn(otherMsg);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteMessages("999"));
        assertEquals("403", ex.getCode());
        assertTrue(ex.getMsg().contains("无权操作他人的消息通知"));
        verify(service, never()).removeBatchByIds(any());
    }

    @Test
    @DisplayName("超管特权放行：超级管理员标记他人消息已读或删除他人消息均畅通放行")
    void superAdmin_canMarkReadAndManageAnyMessage() {
        when(scopeSupport.requireLoginUser()).thenReturn(superUser);
        when(scopeSupport.isSuper(superUser)).thenReturn(true);

        SysMessageInfo userMsg = new SysMessageInfo()
                .setUserId(1001L)
                .setOrgId(2001L)
                .setReadStatus(0);
        userMsg.setId(88L);
        when(sysMessageInfoMapper.selectById(88L)).thenReturn(userMsg);
        when(sysMessageInfoMapper.updateById(any(SysMessageInfo.class))).thenReturn(1);
        doReturn(true).when(service).removeBatchByIds(any());

        // 1. 超管标记已读放行
        assertDoesNotThrow(() -> service.markRead(88L));
        assertEquals(1, userMsg.getReadStatus());

        // 2. 超管删除放行
        assertDoesNotThrow(() -> service.deleteMessages("88"));
    }

    @Test
    @DisplayName("发布权限防伪：普通用户发布消息时强制绑定自身 ID 与当前机构，杜绝伪造他人或全局广播")
    void publishSystemMessage_normalUser_forcedOwnershipBinding() {
        when(scopeSupport.requireLoginUser()).thenReturn(normalUser);
        when(scopeSupport.loginOrgId(normalUser)).thenReturn(2001L);
        when(scopeSupport.isSuper(normalUser)).thenReturn(false);

        // 攻击场景：普通用户试图发布广播 (userId=0, orgId=0) 或冒用他人 (userId=8888L)
        SysMessageInfoVo spoofVo = new SysMessageInfoVo()
                .setTitle("紧急全员系统维护通知")
                .setContent("系统将于今晚24点停机更新")
                .setUserId(8888L)
                .setOrgId(0L);

        SysMessageInfoVo result = service.publishSystemMessage(spoofVo);
        assertNotNull(result);

        ArgumentCaptor<SysMessageInfo> captor = ArgumentCaptor.forClass(SysMessageInfo.class);
        verify(sysMessageInfoMapper).insert(captor.capture());

        SysMessageInfo inserted = captor.getValue();
        // 强断言：已被安全重置为当前操作者和当前机构
        assertEquals(1001L, inserted.getUserId(), "非超管用户发布消息强制绑定自身 userId");
        assertEquals(2001L, inserted.getOrgId(), "非超管用户发布消息强制绑定自身 orgId");
    }

    @Test
    @DisplayName("发布字符边界防御：标题超过 200 字符或内容超过 1000 字符时拦截")
    void publishSystemMessage_lengthBoundaries_mustBeEnforced() {
        when(scopeSupport.requireLoginUser()).thenReturn(normalUser);

        // 1. 标题超长 201 字符
        String longTitle = "T".repeat(201);
        SysMessageInfoVo titleVo = new SysMessageInfoVo().setTitle(longTitle).setContent("正常正文");
        FinanceException ex1 = assertThrows(FinanceException.class, () -> service.publishSystemMessage(titleVo));
        assertTrue(ex1.getMsg().contains("标题长度不能超过200个字符"));

        // 2. 正文超长 1001 字符
        String longContent = "C".repeat(1001);
        SysMessageInfoVo contentVo = new SysMessageInfoVo().setTitle("正常标题").setContent(longContent);
        FinanceException ex2 = assertThrows(FinanceException.class, () -> service.publishSystemMessage(contentVo));
        assertTrue(ex2.getMsg().contains("正文长度不能超过1000个字符"));
    }
}
