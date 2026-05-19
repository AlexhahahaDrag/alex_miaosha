package com.alex.finance.gift;

import com.alex.finance.gift.event.service.impl.GiftEventInfoTServiceImp;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoTServiceImp;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoTServiceImp;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static class TestPersonService extends GiftPersonInfoTServiceImp {
        private List<Object> removedIds = List.of();

        private TestPersonService() {
            super(null);
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
            super(null);
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
            super(null);
        }

        @Override
        public boolean removeBatchByIds(Collection<?> list) {
            removedIds = List.copyOf(list);
            return true;
        }
    }
}
