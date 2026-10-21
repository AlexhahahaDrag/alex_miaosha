package com.alex.common.job.shard;

import com.xxl.job.core.context.XxlJobHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * XXL-Job 分片广播路由与切片过滤辅助工具
 * <p>安全封装分片索引与总数获取，提供绝对值哈希取模算法，并对本地单机或非 XXL-Job 调度环境提供优雅降级。
 */
@Slf4j
public final class JobShardHelper {

    private JobShardHelper() {
    }

    /**
     * 分片信息模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShardInfo {
        /**
         * 当前分片索引（从 0 开始）
         */
        private int shardIndex;

        /**
         * 分片总数（>= 1）
         */
        private int shardTotal;
    }

    /**
     * 安全获取当前任务的分片信息
     * <p>在非调度执行环境（如本地单元测试、Web 请求触发等）或异常情况下，自动兜底返回 {@code (shardIndex = 0, shardTotal = 1)}。
     *
     * @return 分片信息
     */
    public static ShardInfo getShardInfo() {
        try {
            int total = XxlJobHelper.getShardTotal();
            int index = XxlJobHelper.getShardIndex();
            if (total <= 0) {
                return new ShardInfo(0, 1);
            }
            return new ShardInfo(Math.max(0, index), total);
        } catch (Throwable t) {
            log.debug("获取 XXL-Job 分片信息失败或非调度环境，安全回退单机模式 (0, 1): {}", t.getMessage());
            return new ShardInfo(0, 1);
        }
    }

    /**
     * 校验指定数字 ID 是否命中当前节点的广播分片
     *
     * @param id 数据主键或业务哈希 ID
     * @return true-归属本节点处理；false-归属其他分片节点
     */
    public static boolean isMyShard(long id) {
        ShardInfo shardInfo = getShardInfo();
        return isMyShard(id, shardInfo.getShardTotal(), shardInfo.getShardIndex());
    }

    /**
     * 校验指定数字 ID 在给定的分片配置下是否命中
     *
     * @param id         数据主键或业务哈希 ID
     * @param shardTotal 分片总数
     * @param shardIndex 当前分片索引
     * @return true-命中；false-未命中
     */
    public static boolean isMyShard(long id, int shardTotal, int shardIndex) {
        if (shardTotal <= 1) {
            return true;
        }
        long positiveId = (id == Long.MIN_VALUE) ? Long.MAX_VALUE : Math.abs(id);
        return (positiveId % shardTotal) == shardIndex;
    }

    /**
     * 对内存集合按照当前节点分片进行安全切片过滤
     *
     * @param list        原始待处理集合
     * @param idExtractor 数据实体 ID 提取函数
     * @param <T>         数据实体类型
     * @return 归属本节点处理的实体子列表
     */
    public static <T> List<T> filterByShard(List<T> list, Function<T, Long> idExtractor) {
        if (list == null || list.isEmpty() || idExtractor == null) {
            return Collections.emptyList();
        }
        ShardInfo shardInfo = getShardInfo();
        if (shardInfo.getShardTotal() <= 1) {
            return list;
        }
        return list.stream()
                .filter(item -> {
                    if (item == null) {
                        return false;
                    }
                    Long id = idExtractor.apply(item);
                    if (id == null) {
                        return false;
                    }
                    return isMyShard(id, shardInfo.getShardTotal(), shardInfo.getShardIndex());
                })
                .collect(Collectors.toList());
    }
}
