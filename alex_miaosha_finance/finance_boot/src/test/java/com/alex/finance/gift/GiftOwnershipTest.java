package com.alex.finance.gift;

import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.service.impl.GiftEventInfoServiceImp;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoServiceImp;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.relation.entity.GiftRelationInfo;
import com.alex.finance.gift.relation.service.impl.GiftRelationInfoServiceImp;
import com.alex.finance.gift.eventoption.service.GiftEventTypeOptionService;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiftOwnershipTest {

    @Test
    void addPersonOverridesClientOrgAndUserFromLoginUser() {
        UserUtils userUtils = loginUser(10L, 20L);
        TestPersonService service = new TestPersonService(userUtils);

        GiftPersonInfoVo result = service.addGiftPersonInfo(new GiftPersonInfoVo()
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
        GiftRelationInfoVo relation = relationService.addGiftRelationInfo(new GiftRelationInfoVo().setOrgId(1L).setUserId(2L));
        assertEquals(21L, relationService.saved.getOrgId());
        assertEquals(11L, relationService.saved.getUserId());
        assertEquals(21L, relation.getOrgId());
        assertEquals(11L, relation.getUserId());

        TestEventService eventService = new TestEventService(userUtils);
        GiftEventInfoVo event = eventService.addGiftEventInfo(new GiftEventInfoVo().setOrgId(1L).setUserId(2L));
        assertEquals(21L, eventService.saved.getOrgId());
        assertEquals(11L, eventService.saved.getUserId());
        assertEquals(21L, event.getOrgId());
        assertEquals(11L, event.getUserId());

        TestRecordService recordService = new TestRecordService(userUtils);
        GiftRecordInfoVo record = recordService.addGiftRecordInfo(new GiftRecordInfoVo()
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
        TUserVo loginUser = new TUserVo();
        loginUser.setOrgId(777L);
        loginUser.setOrgInfoVo(orgInfoVo);
        loginUser.setId(userId);
        when(userUtils.getLoginUser()).thenReturn(loginUser);
        return userUtils;
    }

    private static class TestPersonService extends GiftPersonInfoServiceImp {
        private GiftPersonInfo saved;

        private TestPersonService(UserUtils userUtils) {
            super(
                    new GiftDataScopeSupport(userUtils),
                    mock(GiftPersonRelationOptionService.class),
                    mock(GiftRecordInfoService.class));
        }

        @Override
        public boolean save(GiftPersonInfo entity) {
            this.saved = entity;
            entity.setId(1L);
            return true;
        }
    }

    private static class TestRelationService extends GiftRelationInfoServiceImp {
        private GiftRelationInfo saved;

        private TestRelationService(UserUtils userUtils) {
            super(new GiftDataScopeSupport(userUtils));
        }

        @Override
        public boolean save(GiftRelationInfo entity) {
            this.saved = entity;
            entity.setId(2L);
            return true;
        }
    }

    private static class TestEventService extends GiftEventInfoServiceImp {
        private GiftEventInfo saved;

        private TestEventService(UserUtils userUtils) {
            super(
                    new GiftDataScopeSupport(userUtils),
                    mock(GiftEventTypeOptionService.class),
                    mock(GiftRecordInfoService.class));
        }

        @Override
        public boolean save(GiftEventInfo entity) {
            this.saved = entity;
            entity.setId(3L);
            return true;
        }
    }

    private static class TestRecordService extends GiftRecordInfoServiceImp {
        private GiftRecordInfo saved;

        private TestRecordService(UserUtils userUtils) {
            super(
                    new GiftDataScopeSupport(userUtils),
                    mock(GiftPersonInfoMapper.class),
                    mock(GiftEventInfoMapper.class));
            GiftRecordInfoMapper mapper = mock(GiftRecordInfoMapper.class);
            when(mapper.insert(any(GiftRecordInfo.class))).thenAnswer(invocation -> {
                GiftRecordInfo record = invocation.getArgument(0);
                record.setId(4L);
                return 1;
            });
            ReflectionTestUtils.setField(this, "baseMapper", mapper);
        }

        @Override
        public boolean save(GiftRecordInfo entity) {
            this.saved = entity;
            entity.setId(4L);
            return true;
        }
    }
}
