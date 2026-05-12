package com.alex.finance.gift;

import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoTServiceImp;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GiftRecordBusinessRuleTest {

    @Test
    void returnRecordMustPointToReceiveRecord() {
        TestRecordService service = new TestRecordService();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.addGiftRecordInfoT(new GiftRecordInfoTVo()
                        .setDirection("RETURN")
                        .setAmount(new BigDecimal("100"))));

        assertEquals("回礼记录必须关联原始收礼记录", exception.getMessage());
    }

    @Test
    void pendingReturnAmountEqualsReceiveAmountMinusReturnAmount() {
        TestRecordService service = new TestRecordService();
        service.receiveRecord = new GiftRecordInfoT()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("1000.00"));
        service.returnRecords = List.of(
                new GiftRecordInfoT().setDirection("RETURN").setAmount(new BigDecimal("300.00")),
                new GiftRecordInfoT().setDirection("RETURN").setAmount(new BigDecimal("120.50"))
        );

        BigDecimal pending = service.calculatePendingReturnAmount(1L);

        assertEquals(new BigDecimal("579.50"), pending);
    }

    @Test
    void markReturnedSetsReturnedFlagOnReceiveRecord() {
        TestRecordService service = new TestRecordService();
        service.receiveRecord = new GiftRecordInfoT()
                .setDirection("RECEIVE")
                .setAmount(new BigDecimal("100.00"));

        service.markReturned(1L);

        assertEquals(1, service.updated.getReturnedFlag());
    }

    private static class TestRecordService extends GiftRecordInfoTServiceImp {
        private GiftRecordInfoT receiveRecord;
        private List<GiftRecordInfoT> returnRecords = List.of();
        private GiftRecordInfoT updated;

        private TestRecordService() {
            super(null);
        }

        @Override
        public GiftRecordInfoT getById(java.io.Serializable id) {
            return receiveRecord;
        }

        @Override
        public List<GiftRecordInfoT> list(Wrapper<GiftRecordInfoT> queryWrapper) {
            return returnRecords;
        }

        @Override
        public boolean updateById(GiftRecordInfoT entity) {
            this.updated = entity;
            return true;
        }

        @Override
        public boolean save(GiftRecordInfoT entity) {
            entity.setId(1L);
            return true;
        }
    }
}
