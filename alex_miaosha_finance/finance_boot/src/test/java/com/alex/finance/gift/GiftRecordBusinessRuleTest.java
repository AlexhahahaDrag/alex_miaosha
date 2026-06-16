package com.alex.finance.gift;

import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.mapper.GiftEventInfoTMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.mapper.GiftRecordInfoTMapper;
import com.alex.common.exception.FinanceException;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoTServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GiftRecordBusinessRuleTest {

    @Mock
    private GiftRecordInfoTMapper giftRecordInfoTMapper;
    @Mock
    private GiftPersonInfoTMapper giftPersonInfoTMapper;
    @Mock
    private GiftEventInfoTMapper giftEventInfoTMapper;
    @Mock
    private UserUtils userUtils;

    private GiftRecordInfoTServiceImp service;

    @BeforeEach
    void setUp() {
        service = new GiftRecordInfoTServiceImp(
                new GiftDataScopeSupport(userUtils),
                giftPersonInfoTMapper,
                giftEventInfoTMapper);
        ReflectionTestUtils.setField(service, "baseMapper", giftRecordInfoTMapper);
        lenient().when(userUtils.getLoginUser()).thenReturn(loginUser());
    }

    @Test
    void returnRecordMustPointToReceiveRecord() {
        FinanceException exception = assertThrows(FinanceException.class, () ->
                service.addGiftRecordInfoT(new GiftRecordInfoTVo()
                        .setDirection("RETURN")
                        .setAmount(new BigDecimal("100"))));

        assertEquals("回礼记录必须关联原始收礼记录", exception.getMsg());
    }

    @Test
    void pendingReturnAmountEqualsReceiveAmountMinusReturnAmount() {
        GiftRecordInfoT receiveRecord = new GiftRecordInfoT()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("1000.00"));
        receiveRecord.setId(1L);
        receiveRecord.setUserId(10L);
        receiveRecord.setOrgId(20L);
        when(giftRecordInfoTMapper.selectById(1L)).thenReturn(receiveRecord);
        when(giftRecordInfoTMapper.sumReturnAmountByRelatedRecordId(1L))
                .thenReturn(new BigDecimal("420.50"));

        BigDecimal pending = service.calculatePendingReturnAmount(1L);

        assertEquals(new BigDecimal("579.50"), pending);
    }

    @Test
    void markReturnedSetsReturnedFlagOnReceiveRecord() {
        GiftRecordInfoT receiveRecord = new GiftRecordInfoT()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("100.00"));
        receiveRecord.setId(1L);
        receiveRecord.setUserId(10L);
        receiveRecord.setOrgId(20L);
        when(giftRecordInfoTMapper.selectById(1L)).thenReturn(receiveRecord);
        when(giftRecordInfoTMapper.updateById(any(GiftRecordInfoT.class))).thenReturn(1);

        service.markReturned(1L);

        org.mockito.ArgumentCaptor<GiftRecordInfoT> captor = org.mockito.ArgumentCaptor.forClass(GiftRecordInfoT.class);
        org.mockito.Mockito.verify(giftRecordInfoTMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getReturnedFlag());
    }

    private TUserVo loginUser() {
        OrgInfoVo orgInfoVo = new OrgInfoVo();
        orgInfoVo.setId(20L);
        TUserVo user = new TUserVo();
        user.setId(10L);
        user.setOrgId(20L);
        user.setOrgInfoVo(orgInfoVo);
        return user;
    }
}
