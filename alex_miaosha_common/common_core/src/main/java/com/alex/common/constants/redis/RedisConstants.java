package com.alex.common.constants.redis;

/**
 * description:  Redis 常量定义
 * author:       majf
 * createDate:   2022/7/14 11:43
 * version:      2.0.0
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    public static final String SEGMENTATION = ":";

    /**
     * @deprecated 请使用标准数字字面量或业务模块自身常量，避免跨域依赖 Redis 常量类
     */
    @Deprecated
    public static final Integer NUM_ONE = 1;

    /**
     * @deprecated 请使用标准数字字面量或业务模块自身常量，避免跨域依赖 Redis 常量类
     */
    @Deprecated
    public static final Integer NUM_FIVE = 5;

    public static final String SEC_KILL_KEY = "SecKillGoodsKey" + SEGMENTATION + "sc";

    public static final String AVOID_REPEAT_COMMIT = "avoid_repeat_commit";
}
