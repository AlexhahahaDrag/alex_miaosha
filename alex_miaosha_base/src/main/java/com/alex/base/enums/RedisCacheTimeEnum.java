package com.alex.base.enums;

public enum RedisCacheTimeEnum {
    LOGIN_EXTIME(60 * 30 * 24),
    GOODS_LIST_EXTIME(60 * 30 * 24),
    GOODS_ID_EXTIME(60),
    SECKILL_PATH_EXTIME(60),
    GOODS_INFO_EXTIME(60);

    RedisCacheTimeEnum(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }
}
