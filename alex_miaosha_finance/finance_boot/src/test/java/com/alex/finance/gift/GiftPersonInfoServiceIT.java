package com.alex.finance.gift;

import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.api.OrgUserInfoApi;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoServiceImp;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service 层集成测试：真实 {@link GiftDataScopeSupport} + {@link GiftPersonInfoServiceImp}，
 * 验证参数防御与 ORG_SHARED 数据权限隔离。
 */
@ExtendWith(MockitoExtension.class)
class GiftPersonInfoServiceIT {

    private static final Long LOGIN_USER_ID = 10L;
    private static final Long LOGIN_ORG_ID = 20L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long OTHER_ORG_ID = 88L;

    @Mock
    private GiftPersonInfoMapper giftPersonInfoMapper;
    @Mock
    private GiftPersonRelationOptionService giftPersonRelationOptionService;
    @Mock
    private GiftRecordInfoService giftRecordInfoService;
    @Mock
    private OrgUserInfoApi orgUserInfoApi;
    @Mock
    private UserApi userApi;
    @Mock
    private OssApi ossApi;
    @Mock
    private UserUtils userUtils;

    private GiftPersonInfoServiceImp service;

    @BeforeEach
    void setUp() {
        GiftDataScopeSupport giftDataScopeSupport = new GiftDataScopeSupport(userUtils);
        service = new GiftPersonInfoServiceImp(
                giftDataScopeSupport,
                giftPersonRelationOptionService,
                giftRecordInfoService,
                orgUserInfoApi,
                userApi,
                ossApi);
        ReflectionTestUtils.setField(service, "baseMapper", giftPersonInfoMapper);

        lenient().when(userUtils.getLoginUser()).thenReturn(loginUser());
        lenient().when(giftPersonInfoMapper.insert(any(GiftPersonInfo.class))).thenAnswer(invocation -> {
            GiftPersonInfo entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
    }

    @Test
    void addPerson_should_reject_when_name_is_empty_or_blank() {
        GiftPersonInfoVo vo = new GiftPersonInfoVo().setPersonName("");
        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftPersonInfo(vo));
        assertEquals("亲友姓名不能为空", ex.getMsg());

        vo.setPersonName("   ");
        ex = assertThrows(FinanceException.class, () -> service.addGiftPersonInfo(vo));
        assertEquals("亲友姓名不能为空", ex.getMsg());
    }

    @Test
    void addPerson_should_reject_when_name_too_long() {
        GiftPersonInfoVo vo = new GiftPersonInfoVo().setPersonName("A".repeat(51));
        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftPersonInfo(vo));
        assertEquals("亲友姓名最多50个字符", ex.getMsg());
    }

    @Test
    void addPerson_should_reject_when_phone_too_long() {
        GiftPersonInfoVo vo = new GiftPersonInfoVo().setPersonName("张三").setPhone("1".repeat(21));
        FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftPersonInfo(vo));
        assertEquals("电话号码长度不能超过20位", ex.getMsg());
    }

    @Test
    void addPerson_should_override_orgId_and_userId_with_login_user() {
        GiftPersonInfoVo vo = new GiftPersonInfoVo()
                .setPersonName("李四")
                .setUserId(999L)
                .setOrgId(888L);

        GiftPersonInfoVo saved = service.addGiftPersonInfo(vo);
        assertNotNull(saved.getId());
        assertEquals(LOGIN_USER_ID, saved.getUserId());
        assertEquals(LOGIN_ORG_ID, saved.getOrgId());
        verify(giftPersonInfoMapper).insert(any(GiftPersonInfo.class));
    }

    @Test
    void updatePerson_should_reject_when_id_is_null() {
        GiftPersonInfoVo vo = new GiftPersonInfoVo().setPersonName("王五");
        FinanceException ex = assertThrows(FinanceException.class, () -> service.updateGiftPersonInfo(vo));
        assertEquals("亲友ID不能为空", ex.getMsg());
    }

    @Test
    void updatePerson_should_reject_cross_org() {
        GiftPersonInfo existing = person(2L, OTHER_USER_ID, OTHER_ORG_ID);
        when(giftPersonInfoMapper.selectById(2L)).thenReturn(existing);

        GiftPersonInfoVo update = new GiftPersonInfoVo().setPersonName("修改名称");
        update.setId(2L);
        FinanceException ex = assertThrows(FinanceException.class, () -> service.updateGiftPersonInfo(update));
        assertEquals("无权访问其他机构的亲友", ex.getMsg());
    }

    @Test
    void updatePerson_should_allow_same_org() {
        GiftPersonInfo existing = person(3L, OTHER_USER_ID, LOGIN_ORG_ID);
        when(giftPersonInfoMapper.selectById(3L)).thenReturn(existing);
        when(giftPersonInfoMapper.updateById(any(GiftPersonInfo.class))).thenReturn(1);

        GiftPersonInfoVo update = new GiftPersonInfoVo().setPersonName("新名称");
        update.setId(3L);
        Boolean ok = service.updateGiftPersonInfo(update);
        assertTrue(ok);
    }

    @Test
    void deletePerson_should_reject_cross_org() {
        GiftPersonInfo existing = person(4L, OTHER_USER_ID, OTHER_ORG_ID);
        when(giftPersonInfoMapper.selectById(4L)).thenReturn(existing);

        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteGiftPersonInfo("4"));
        assertEquals("无权访问其他机构的亲友", ex.getMsg());
    }

    @Test
    void deletePerson_should_reject_invalid_id_format() {
        FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteGiftPersonInfo("1,abc,3"));
        assertEquals("亲友ID格式不合法", ex.getMsg());
    }

    @Test
    void getProfile_should_reject_cross_org() {
        when(giftPersonInfoMapper.selectById(5L)).thenReturn(person(5L, OTHER_USER_ID, OTHER_ORG_ID));

        FinanceException ex = assertThrows(FinanceException.class, () -> service.getProfile(5L));
        assertEquals("无权访问其他机构的亲友", ex.getMsg());
    }

    @Test
    void getProfile_should_succeed_for_same_org() {
        when(giftPersonInfoMapper.selectById(6L)).thenReturn(person(6L, OTHER_USER_ID, LOGIN_ORG_ID));
        when(giftRecordInfoService.getList(any())).thenReturn(List.of());

        GiftPersonProfileVo profile = service.getProfile(6L);
        assertNotNull(profile);
        assertNotNull(profile.getPerson());
        assertEquals("test", profile.getPerson().getPersonName());
    }

    private GiftPersonInfo person(Long id, Long userId, Long orgId) {
        GiftPersonInfo person = new GiftPersonInfo().setPersonName("test");
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
