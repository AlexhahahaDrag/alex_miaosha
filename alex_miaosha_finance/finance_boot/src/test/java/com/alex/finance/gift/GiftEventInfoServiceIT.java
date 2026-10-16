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
