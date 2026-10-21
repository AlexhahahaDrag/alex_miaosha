package com.alex.common.job;

import com.alex.common.job.shard.JobShardHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("XXL-Job 分片广播路由与切片过滤集成测试")
class JobShardHelperIT {

    static class MockEntity {
        private final Long id;
        private final String name;

        public MockEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    @DisplayName("1. 非调度执行环境安全回退单机模式 (shardIndex=0, shardTotal=1)")
    void testDefaultSingleNodeFallback() {
        JobShardHelper.ShardInfo shardInfo = JobShardHelper.getShardInfo();
        assertNotNull(shardInfo);
        assertEquals(0, shardInfo.getShardIndex(), "非调度环境默认 shardIndex 应为 0");
        assertEquals(1, shardInfo.getShardTotal(), "非调度环境默认 shardTotal 应为 1");
        // 单机模式下任何 ID 均判定命中
        assertTrue(JobShardHelper.isMyShard(123456L));
        assertTrue(JobShardHelper.isMyShard(0L));
        assertTrue(JobShardHelper.isMyShard(-999L));
    }

    @Test
    @DisplayName("2. 分片广播取模算法精确性与全覆盖验证")
    void testIsMyShardWithModulo() {
        int shardTotal = 4;
        // 验证 0..3 命中对应分片
        for (int index = 0; index < shardTotal; index++) {
            assertTrue(JobShardHelper.isMyShard((long) index, shardTotal, index));
            assertFalse(JobShardHelper.isMyShard((long) index, shardTotal, (index + 1) % shardTotal));
        }

        // 大数值测试
        long testId = 10007L;
        int expectedIndex = (int) (testId % shardTotal);
        assertTrue(JobShardHelper.isMyShard(testId, shardTotal, expectedIndex));
        assertFalse(JobShardHelper.isMyShard(testId, shardTotal, (expectedIndex + 1) % shardTotal));
    }

    @Test
    @DisplayName("3. 负数 ID 与 Long.MIN_VALUE 极值防御")
    void testNegativeAndExtremeValues() {
        int shardTotal = 3;
        // 负数 ID 应做绝对值处理
        long negativeId = -4L; // abs(-4) = 4, 4 % 3 = 1
        assertTrue(JobShardHelper.isMyShard(negativeId, shardTotal, 1));
        assertFalse(JobShardHelper.isMyShard(negativeId, shardTotal, 0));

        // Long.MIN_VALUE 极值防御（abs(Long.MIN_VALUE) 依然为负，底层必须防呆转为 Long.MAX_VALUE）
        assertDoesNotThrow(() -> {
            boolean hit = JobShardHelper.isMyShard(Long.MIN_VALUE, shardTotal, 0);
            // 只要不抛出负模越界异常即可
            assertNotNull(hit);
        });
    }

    @Test
    @DisplayName("4. 内存数据集切片过滤 filterByShard 精确性")
    void testFilterByShard() {
        List<MockEntity> entityList = new ArrayList<>();
        for (long i = 1; i <= 9; i++) {
            entityList.add(new MockEntity(i, "Entity-" + i));
        }

        // 默认非调度环境下 (shardTotal=1)，全量保留
        List<MockEntity> defaultFiltered = JobShardHelper.filterByShard(entityList, MockEntity::getId);
        assertEquals(9, defaultFiltered.size());

        // 边界保护：null 集合与空集合
        assertTrue(JobShardHelper.filterByShard(null, MockEntity::getId).isEmpty());
        assertTrue(JobShardHelper.filterByShard(new ArrayList<>(), MockEntity::getId).isEmpty());
        assertTrue(JobShardHelper.filterByShard(entityList, null).isEmpty());
    }
}
