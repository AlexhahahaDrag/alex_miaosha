package com.alex.finance.gift;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.user.user.UserUtils;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.event.service.impl.GiftEventInfoServiceImp;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoServiceImp;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.eventoption.service.GiftEventTypeOptionService;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiftAggregateBusinessRuleTest {

    @Test
    void personBusinessPageAggregatesGiveReceiveAndLatestRecord() {
        TestPersonService service = new TestPersonService();
        GiftPersonInfo person = new GiftPersonInfo()
                .setPersonName("张明远")
                .setRelationType("FRIEND")
                .setPhone("13800000000");
        person.setId(1L);
        service.people = List.of(person);
        service.records = List.of(
                record(10L, "GIVE", new BigDecimal("200.00"), 1L, 2L, LocalDateTime.of(2026, 5, 1, 10, 0)),
                record(11L, "RECEIVE", new BigDecimal("500.00"), 2L, 1L, LocalDateTime.of(2026, 5, 3, 10, 0)),
                record(12L, "RETURN", new BigDecimal("120.00"), 1L, 2L, LocalDateTime.of(2026, 5, 4, 10, 0)));

        GiftPersonBusinessVo row = service.getBusinessPage(1L, 10L, null).getRecords().get(0);

        assertEquals(new BigDecimal("320.00"), row.getTotalGiveAmount());
        assertEquals(new BigDecimal("500.00"), row.getTotalReceiveAmount());
        assertEquals(new BigDecimal("180.00"), row.getNetAmount());
        assertEquals("RETURN", row.getLatestDirection());
        assertEquals(LocalDateTime.of(2026, 5, 4, 10, 0), row.getLatestRecordTime());
    }

    @Test
    void eventBusinessPageAggregatesParticipantsAndAmounts() {
        TestEventService service = new TestEventService();
        GiftEventInfo event = new GiftEventInfo()
                .setEventName("婚礼")
                .setEventTime(LocalDateTime.of(2026, 6, 1, 12, 0));
        event.setId(9L);
        service.events = List.of(event);
        service.records = List.of(
                record(1L, "GIVE", new BigDecimal("200.00"), 1L, 2L, LocalDateTime.of(2026, 5, 1, 10, 0)).setEventId(9L),
                record(2L, "RECEIVE", new BigDecimal("300.00"), 3L, 2L, LocalDateTime.of(2026, 5, 2, 10, 0)).setEventId(9L));

        GiftEventBusinessVo row = service.getBusinessPage(1L, 10L, null).getRecords().get(0);

        assertEquals(3L, row.getParticipantCount());
        assertEquals(new BigDecimal("500.00"), row.getTotalAmount());
        assertEquals(new BigDecimal("200.00"), row.getGiveAmount());
        assertEquals(new BigDecimal("300.00"), row.getReceiveAmount());
    }

    @Test
    void recordSummaryAggregatesFilteredDirections() {
        TestRecordService service = new TestRecordService();
        service.records = List.of(
                record(1L, "GIVE", new BigDecimal("200.00"), 1L, 2L, LocalDateTime.of(2026, 5, 1, 10, 0)),
                record(2L, "RECEIVE", new BigDecimal("500.00"), 2L, 1L, LocalDateTime.of(2026, 5, 2, 10, 0)),
                record(3L, "RETURN", new BigDecimal("100.00"), 1L, 2L, LocalDateTime.of(2026, 5, 3, 10, 0)));

        GiftRecordSummaryVo summary = service.getSummary(new GiftRecordQuery());

        assertEquals(new BigDecimal("200.00"), summary.getGiveAmount());
        assertEquals(new BigDecimal("500.00"), summary.getReceiveAmount());
        assertEquals(new BigDecimal("100.00"), summary.getReturnAmount());
        assertEquals(new BigDecimal("200.00"), summary.getNetAmount());
        assertEquals(3L, summary.getRecordCount());
    }

    private static GiftRecordInfo record(Long id, String direction, BigDecimal amount, Long giverId, Long receiverId, LocalDateTime payTime) {
        GiftRecordInfo record = new GiftRecordInfo()
                .setDirection(direction)
                .setAmount(amount)
                .setGiverPersonId(giverId)
                .setReceiverPersonId(receiverId)
                .setPayTime(payTime);
        record.setId(id);
        return record;
    }

    private static List<GiftRecordInfoVo> toRecordVos(List<GiftRecordInfo> records) {
        return records.stream().map(entity -> {
            GiftRecordInfoVo vo = new GiftRecordInfoVo();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
    }

    private static class TestPersonService extends GiftPersonInfoServiceImp {
        private List<GiftPersonInfo> people = List.of();
        private List<GiftRecordInfo> records = List.of();

        private TestPersonService() {
            super(
                    new GiftDataScopeSupport(mock(UserUtils.class)),
                    mock(GiftPersonRelationOptionService.class),
                    mock(GiftRecordInfoService.class));
        }

        @Override
        public List<GiftPersonInfoVo> getList(GiftPersonQuery query) {
            return people.stream().map(entity -> {
                GiftPersonInfoVo vo = new GiftPersonInfoVo();
                BeanUtils.copyProperties(entity, vo);
                return vo;
            }).toList();
        }

        @Override
        protected List<GiftRecordInfoVo> listGiftRecordsForAggregate() {
            return toRecordVos(records);
        }

        @Override
        public Page<GiftPersonBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
            long current = pageNum == null ? 1 : pageNum;
            long size = pageSize == null ? 10 : pageSize;
            List<GiftPersonBusinessVo> rows = getList(query).stream()
                    .map(this::toBusinessVo)
                    .toList();
            long from = Math.max(0, (current - 1) * size);
            long to = Math.min(rows.size(), from + size);
            Page<GiftPersonBusinessVo> page = new Page<>(current, size, rows.size());
            page.setRecords(from >= rows.size() ? List.of() : rows.subList((int) from, (int) to));
            return page;
        }
    }

    private static class TestEventService extends GiftEventInfoServiceImp {
        private List<GiftEventInfo> events = List.of();
        private List<GiftRecordInfo> records = List.of();

        private TestEventService() {
            super(
                    new GiftDataScopeSupport(mock(UserUtils.class)),
                    mock(GiftEventTypeOptionService.class),
                    mock(GiftRecordInfoService.class));
        }

        @Override
        public List<GiftEventInfoVo> getList(GiftEventQuery query) {
            return events.stream().map(entity -> {
                GiftEventInfoVo vo = new GiftEventInfoVo();
                BeanUtils.copyProperties(entity, vo);
                return vo;
            }).toList();
        }

        @Override
        protected List<GiftRecordInfoVo> listGiftRecordsForAggregate() {
            return toRecordVos(records);
        }

        @Override
        public Page<GiftEventBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftEventQuery query) {
            long current = pageNum == null ? 1 : pageNum;
            long size = pageSize == null ? 10 : pageSize;
            List<GiftEventBusinessVo> rows = getList(query).stream()
                    .map(this::toBusinessVo)
                    .toList();
            long from = Math.max(0, (current - 1) * size);
            long to = Math.min(rows.size(), from + size);
            Page<GiftEventBusinessVo> page = new Page<>(current, size, rows.size());
            page.setRecords(from >= rows.size() ? List.of() : rows.subList((int) from, (int) to));
            return page;
        }
    }

    private static class TestRecordService extends GiftRecordInfoServiceImp {
        private List<GiftRecordInfo> records = List.of();

        private TestRecordService() {
            super(
                    new GiftDataScopeSupport(mock(UserUtils.class)),
                    mock(GiftPersonInfoMapper.class),
                    mock(GiftEventInfoMapper.class));
            GiftRecordInfoMapper mapper = mock(GiftRecordInfoMapper.class);
            when(mapper.listEntities(any())).thenAnswer(invocation -> records);
            ReflectionTestUtils.setField(this, "baseMapper", mapper);
        }
    }
}
