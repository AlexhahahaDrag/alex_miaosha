package com.alex.finance.gift;

import com.alex.api.finance.gift.event.vo.GiftEventInfoTVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoTVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.alex.finance.gift.event.service.impl.GiftEventInfoTServiceImp;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoTServiceImp;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoTServiceImp;
import com.alex.finance.gift.relation.entity.GiftRelationInfoT;
import com.alex.finance.gift.relation.service.impl.GiftRelationInfoTServiceImp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiftOwnershipTest {

    @Test
    void addPersonOverridesClientOrgAndUserFromLoginUser() {
        UserUtils userUtils = loginUser(10L, 20L);
        TestPersonService service = new TestPersonService(userUtils);

        GiftPersonInfoTVo result = service.addGiftPersonInfoT(new GiftPersonInfoTVo()
                .setOrgId(999L)
                .setUserId(888L)
                .setPersonName("client"));

        assertEquals(20L, service.saved.getOrgId());
        assertEquals(10L, service.saved.getUserId());
        assertEquals(20L, result.getOrgId());
        assertEquals(10L, result.getUserId());
    }

    @Test
    void addRelationEventAndRecordOverrideClientOrgAndUserFromLoginUser() {
        UserUtils userUtils = loginUser(11L, 21L);

        TestRelationService relationService = new TestRelationService(userUtils);
        GiftRelationInfoTVo relation = relationService.addGiftRelationInfoT(new GiftRelationInfoTVo().setOrgId(1L).setUserId(2L));
        assertEquals(21L, relationService.saved.getOrgId());
        assertEquals(11L, relationService.saved.getUserId());
        assertEquals(21L, relation.getOrgId());
        assertEquals(11L, relation.getUserId());

        TestEventService eventService = new TestEventService(userUtils);
        GiftEventInfoTVo event = eventService.addGiftEventInfoT(new GiftEventInfoTVo().setOrgId(1L).setUserId(2L));
        assertEquals(21L, eventService.saved.getOrgId());
        assertEquals(11L, eventService.saved.getUserId());
        assertEquals(21L, event.getOrgId());
        assertEquals(11L, event.getUserId());

        TestRecordService recordService = new TestRecordService(userUtils);
        GiftRecordInfoTVo record = recordService.addGiftRecordInfoT(new GiftRecordInfoTVo()
                .setOrgId(1L)
                .setUserId(2L)
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("100.00")));
        assertEquals(21L, recordService.saved.getOrgId());
        assertEquals(11L, recordService.saved.getUserId());
        assertEquals(21L, record.getOrgId());
        assertEquals(11L, record.getUserId());
    }

    private UserUtils loginUser(Long userId, Long orgId) {
        UserUtils userUtils = mock(UserUtils.class);
        OrgInfoVo orgInfoVo = new OrgInfoVo();
        orgInfoVo.setId(orgId);
        TUserVo loginUser = new TUserVo()
                .setOrgId(777L)
                .setOrgInfoVo(orgInfoVo);
        loginUser.setId(userId);
        when(userUtils.getLoginUser()).thenReturn(loginUser);
        return userUtils;
    }

    private static class TestPersonService extends GiftPersonInfoTServiceImp {
        private GiftPersonInfoT saved;

        private TestPersonService(UserUtils userUtils) {
            super(userUtils);
        }

        @Override
        public boolean save(GiftPersonInfoT entity) {
            this.saved = entity;
            entity.setId(1L);
            return true;
        }
    }

    private static class TestRelationService extends GiftRelationInfoTServiceImp {
        private GiftRelationInfoT saved;

        private TestRelationService(UserUtils userUtils) {
            super(userUtils);
        }

        @Override
        public boolean save(GiftRelationInfoT entity) {
            this.saved = entity;
            entity.setId(2L);
            return true;
        }
    }

    private static class TestEventService extends GiftEventInfoTServiceImp {
        private GiftEventInfoT saved;

        private TestEventService(UserUtils userUtils) {
            super(userUtils);
        }

        @Override
        public boolean save(GiftEventInfoT entity) {
            this.saved = entity;
            entity.setId(3L);
            return true;
        }
    }

    private static class TestRecordService extends GiftRecordInfoTServiceImp {
        private GiftRecordInfoT saved;

        private TestRecordService(UserUtils userUtils) {
            super(userUtils);
        }

        @Override
        public boolean save(GiftRecordInfoT entity) {
            this.saved = entity;
            entity.setId(4L);
            return true;
        }
    }
}
