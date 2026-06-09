package com.alex.finance.gift;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.alex.finance.gift.event.mapper.GiftEventInfoTMapper;
import com.alex.finance.gift.event.service.impl.GiftEventInfoTServiceImp;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoTServiceImp;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.mapper.GiftRecordInfoTMapper;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoTServiceImp;
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

        service.deleteGiftPersonInfoT("2056551606332862465,2056551606332862466");

        assertLongIds(service.removedIds);
    }

    @Test
    void eventDeleteConvertsStringIdsToLongIds() {
        TestEventService service = new TestEventService();

        service.deleteGiftEventInfoT("2056551606332862465,2056551606332862466");

        assertLongIds(service.removedIds);
    }

    @Test
    void recordDeleteConvertsStringIdsToLongIds() {
        TestRecordService service = new TestRecordService();

        service.deleteGiftRecordInfoT("2056551606332862465,2056551606332862466");

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

    private static class TestPersonService extends GiftPersonInfoTServiceImp {
        private List<Object> removedIds = List.of();

        private TestPersonService() {
            super(new GiftDataScopeSupport(loginUserUtils()));
        }

        @Override
        public GiftPersonInfoT getById(Serializable id) {
            GiftPersonInfoT entity = new GiftPersonInfoT();
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

    private static class TestEventService extends GiftEventInfoTServiceImp {
        private List<Object> removedIds = List.of();

        private TestEventService() {
            super(new GiftDataScopeSupport(loginUserUtils()));
        }

        @Override
        public GiftEventInfoT getById(Serializable id) {
            GiftEventInfoT entity = new GiftEventInfoT();
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

    private static class TestRecordService extends GiftRecordInfoTServiceImp {
        private List<Object> removedIds = List.of();

        private TestRecordService() {
            super(
                    new GiftDataScopeSupport(loginUserUtils()),
                    mock(GiftPersonInfoTMapper.class),
                    mock(GiftEventInfoTMapper.class));
            ReflectionTestUtils.setField(this, "baseMapper", mock(GiftRecordInfoTMapper.class));
        }

        @Override
        public GiftRecordInfoT getById(Serializable id) {
            GiftRecordInfoT entity = new GiftRecordInfoT();
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
