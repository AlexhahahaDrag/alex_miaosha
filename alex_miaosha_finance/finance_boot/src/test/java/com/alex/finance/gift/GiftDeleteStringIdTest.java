package com.alex.finance.gift;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.api.OrgUserInfoApi;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.event.service.impl.GiftEventInfoServiceImp;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoServiceImp;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.eventoption.service.GiftEventTypeOptionService;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.eventoption.service.GiftEventTypeOptionService;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiftDeleteStringIdTest {

    @Test
    void personDeleteConvertsStringIdsToLongIds() {
        TestPersonService service = new TestPersonService();

        service.deleteGiftPersonInfo("2056551606332862465,2056551606332862466");

        assertLongIds(service.removedIds);
    }

    @Test
    void eventDeleteConvertsStringIdsToLongIds() {
        TestEventService service = new TestEventService();

        service.deleteGiftEventInfo("2056551606332862465,2056551606332862466");

        assertLongIds(service.removedIds);
    }

    @Test
    void recordDeleteConvertsStringIdsToLongIds() {
        TestRecordService service = new TestRecordService();

        service.deleteGiftRecordInfo("2056551606332862465,2056551606332862466");

        assertLongIds(service.removedIds);
    }

    private static void assertLongIds(List<Object> ids) {
        assertEquals(2, ids.size());
        assertTrue(ids.stream().allMatch(Long.class::isInstance), "gift delete ids must be Long values");
        assertEquals(2056551606332862465L, ids.get(0));
        assertEquals(2056551606332862466L, ids.get(1));
    }

    private static TUserVo loginUser() {
        OrgInfoVo orgInfoVo = new OrgInfoVo();
        orgInfoVo.setId(20L);
        TUserVo user = new TUserVo();
        user.setId(10L);
        user.setOrgId(20L);
        user.setOrgInfoVo(orgInfoVo);
        return user;
    }

    private static class TestPersonService extends GiftPersonInfoServiceImp {
        private List<Object> removedIds = List.of();

        private TestPersonService() {
            super(
                    new GiftDataScopeSupport(loginUserUtils()),
                    mock(GiftPersonRelationOptionService.class),
                    mock(GiftRecordInfoService.class),
                    mock(OrgUserInfoApi.class),
                    mock(UserApi.class),
                    mock(OssApi.class));
        }

        @Override
        public GiftPersonInfo getById(Serializable id) {
            GiftPersonInfo entity = new GiftPersonInfo();
            entity.setId((Long) id);
            entity.setUserId(10L);
            entity.setOrgId(20L);
            return entity;
        }

        @Override
        public boolean removeBatchByIds(Collection<?> list) {
            removedIds = List.copyOf(list);
            return true;
        }
    }

    private static class TestEventService extends GiftEventInfoServiceImp {
        private List<Object> removedIds = List.of();

        private TestEventService() {
            super(
                    new GiftDataScopeSupport(loginUserUtils()),
                    mock(GiftEventTypeOptionService.class),
                    mock(GiftRecordInfoService.class));
        }

        @Override
        public GiftEventInfo getById(Serializable id) {
            GiftEventInfo entity = new GiftEventInfo();
            entity.setId((Long) id);
            entity.setUserId(10L);
            entity.setOrgId(20L);
            return entity;
        }

        @Override
        public boolean removeBatchByIds(Collection<?> list) {
            removedIds = List.copyOf(list);
            return true;
        }
    }

    private static class TestRecordService extends GiftRecordInfoServiceImp {
        private List<Object> removedIds = List.of();

        private TestRecordService() {
            super(
                    new GiftDataScopeSupport(loginUserUtils()),
                    mock(GiftPersonInfoMapper.class),
                    mock(GiftEventInfoMapper.class),
                    null);
            ReflectionTestUtils.setField(this, "baseMapper", mock(GiftRecordInfoMapper.class));
        }

        @Override
        public GiftRecordInfo getById(Serializable id) {
            GiftRecordInfo entity = new GiftRecordInfo();
            entity.setId((Long) id);
            entity.setUserId(10L);
            entity.setOrgId(20L);
            return entity;
        }

        @Override
        public boolean removeBatchByIds(Collection<?> list) {
            removedIds = List.copyOf(list);
            return true;
        }
    }

    private static UserUtils loginUserUtils() {
        UserUtils userUtils = mock(UserUtils.class);
        when(userUtils.getLoginUser()).thenReturn(loginUser());
        return userUtils;
    }
}
