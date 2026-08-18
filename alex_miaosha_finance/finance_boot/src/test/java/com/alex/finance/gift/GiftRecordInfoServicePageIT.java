package com.alex.finance.gift;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * H1：Service 分页必须委托 {@link GiftRecordInfoMapper#getPage}，才能触发 Mapper 上的 {@code @DataPermission}。
 */
@ExtendWith(MockitoExtension.class)
class GiftRecordInfoServicePageIT {

    @Mock
    private GiftRecordInfoMapper giftRecordInfoMapper;
    @Mock
    private GiftPersonInfoMapper giftPersonInfoMapper;
    @Mock
    private GiftEventInfoMapper giftEventInfoMapper;
    @Mock
    private UserUtils userUtils;

    @Captor
    private ArgumentCaptor<Page<GiftRecordInfoVo>> pageCaptor;

    private GiftRecordInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new GiftRecordInfoServiceImp(
                new GiftDataScopeSupport(userUtils),
                giftPersonInfoMapper,
                giftEventInfoMapper,
                null);
        ReflectionTestUtils.setField(service, "baseMapper", giftRecordInfoMapper);
    }

    @Test
    void getPage_should_delegate_to_mapper_getPage_with_data_permission_entry_point() {
        GiftRecordQuery query = new GiftRecordQuery().setDirection("RECEIVE");
        Page<GiftRecordInfoVo> expected = new Page<>(1, 10, 1);
        expected.setRecords(java.util.List.of(new GiftRecordInfoVo()));
        when(giftRecordInfoMapper.getPage(any(), eq(query))).thenReturn(expected);

        Page<GiftRecordInfoVo> actual = service.getPage(1L, 10L, query);

        verify(giftRecordInfoMapper).getPage(pageCaptor.capture(), eq(query));
        assertEquals(1L, pageCaptor.getValue().getCurrent());
        assertEquals(10L, pageCaptor.getValue().getSize());
        assertSame(expected, actual);
    }
}
