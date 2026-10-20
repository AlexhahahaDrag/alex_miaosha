package com.alex.finance.message;

import com.alex.api.finance.message.vo.SysMessageInfoVo;
import com.alex.api.finance.message.vo.SysMessageSummaryVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.alex.finance.accountRecordInfo.mapper.AccountRecordInfoMapper;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.finance.mapper.FinanceBudgetInfoMapper;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.message.mapper.SysMessageInfoMapper;
import com.alex.finance.message.service.impl.SysMessageInfoServiceImpl;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.alex.finance.wechat.service.WeChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 系统消息中心未读分类汇总统计口径、一键已读与雪花 ID 序列化契约测试
 */
class SysMessageSummaryAndVoContractIT {

    private SysMessageInfoMapper sysMessageInfoMapper;
    private GiftDataScopeSupport scopeSupport;
    private SysMessageInfoServiceImpl service;
    private TUserVo currentUser;

    @BeforeEach
    void setUp() {
        sysMessageInfoMapper = mock(SysMessageInfoMapper.class);
        scopeSupport = mock(GiftDataScopeSupport.class);

        service = new SysMessageInfoServiceImpl(
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
        );
        ReflectionTestUtils.setField(service, "baseMapper", sysMessageInfoMapper);

        currentUser = new TUserVo();
        currentUser.setId(1001L);
        currentUser.setOrgId(2001L);
        when(scopeSupport.requireLoginUser()).thenReturn(currentUser);
        when(scopeSupport.loginOrgId(currentUser)).thenReturn(2001L);
    }

    @Test
    @DisplayName("分类未读汇总统计：getSummary 必须精准统计 giftReturnUnread 且不再混入 systemUnread")
    void getSummary_accurateClassificationAndGiftReturnIndependence() {
        List<SysMessageInfoVo> mockUnreadList = new ArrayList<>();
        // 2条礼尚往来事件
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_GIFT_EVENT));
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_GIFT_EVENT));

        // 3条人情回礼备忘
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_GIFT_RETURN));
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_GIFT_RETURN));
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_GIFT_RETURN));

        // 1条消费券到期
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_COUPON_EXPIRE));

        // 2条卡包到期
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_CARD_EXPIRE));
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_CARD_EXPIRE));

        // 2条系统通知
        mockUnreadList.add(new SysMessageInfoVo().setMsgType(SysMessageInfoServiceImpl.MSG_TYPE_SYSTEM));
        mockUnreadList.add(new SysMessageInfoVo().setMsgType("ANNOUNCEMENT")); // 未知默认归为系统

        when(sysMessageInfoMapper.selectUnreadList(1001L, 2001L)).thenReturn(mockUnreadList);

        SysMessageSummaryVo summary = service.getSummary();

        assertNotNull(summary);
        assertEquals(2L, summary.getGiftEventUnread(), "礼尚往来事件未读数应为 2");
        assertEquals(3L, summary.getGiftReturnUnread(), "人情回礼备忘未读数应为 3 (已独立统计)");
        assertEquals(1L, summary.getCouponExpireUnread(), "消费券到期未读数应为 1");
        assertEquals(2L, summary.getCardExpireUnread(), "卡包到期未读数应为 2");
        assertEquals(2L, summary.getSystemUnread(), "系统通知未读数应为 2 (不再包含回礼备忘)");
        assertEquals(10L, summary.getUnreadTotal(), "总未读数应为 10 (2+3+1+2+2)");
    }

    @Test
    @DisplayName("一键已读透传：markAllRead 支持指定类型与全局一键已读")
    void markAllRead_delegatesToMapperWithScope() {
        when(sysMessageInfoMapper.markAllRead(eq(1001L), eq(2001L), eq("GIFT_RETURN"), any())).thenReturn(3);
        when(sysMessageInfoMapper.markAllRead(eq(1001L), eq(2001L), isNull(), any())).thenReturn(10);

        // 1. 指定类型全部已读
        Integer updated1 = service.markAllRead("GIFT_RETURN");
        assertEquals(3, updated1);

        // 2. 全部类型一键已读
        Integer updated2 = service.markAllRead(null);
        assertEquals(10, updated2);
    }

    @Test
    @DisplayName("反射契约锁：SysMessageInfoVo 继承链上所有 Long 类型 ID 必须标注 Long2StringSerializer")
    void sysMessageInfoVo_reflectionContractLock() {
        List<Field> allFields = new ArrayList<>();
        // 当前类字段
        allFields.addAll(List.of(SysMessageInfoVo.class.getDeclaredFields()));
        // 父类 BaseVo 字段
        allFields.addAll(List.of(BaseVo.class.getDeclaredFields()));

        for (Field field : allFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType().equals(Long.class)) {
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);
                assertNotNull(annotation, String.format(
                        "契约漏洞探测：字段 [%s#%s] 缺失 @JsonSerialize 注解，前端雪花 ID 存在被 JavaScript 截断风险！",
                        field.getDeclaringClass().getSimpleName(), field.getName()));

                assertEquals(Long2StringSerializer.class, annotation.using(), String.format(
                        "契约漏洞探测：字段 [%s#%s] 未绑定 Long2StringSerializer.class！",
                        field.getDeclaringClass().getSimpleName(), field.getName()));
            }
        }
    }

    @Test
    @DisplayName("雪花 ID 序列化高保真：19 位 Long ID 序列化为字符串，杜绝前端精度截断")
    void sysMessageInfoVo_jsonSerialization_precisionSafety() throws Exception {
        SysMessageInfoVo vo = new SysMessageInfoVo();
        vo.setId(1848291049281738291L);
        vo.setUserId(1848291049281738292L);
        vo.setOrgId(1848291049281738293L);
        vo.setTitle("重要提醒");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(vo);

        JsonNode node = mapper.readTree(json);
        assertTrue(node.get("id").isTextual(), "id 必须序列化为字符串类型");
        assertEquals("1848291049281738291", node.get("id").asText());

        assertTrue(node.get("userId").isTextual(), "userId 必须序列化为字符串类型");
        assertEquals("1848291049281738292", node.get("userId").asText());

        assertTrue(node.get("orgId").isTextual(), "orgId 必须序列化为字符串类型");
        assertEquals("1848291049281738293", node.get("orgId").asText());
    }
}
