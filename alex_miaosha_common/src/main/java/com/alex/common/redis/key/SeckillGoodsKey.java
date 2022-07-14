package com.alex.common.redis.key;

public class SeckillGoodsKey extends BasePrefix{

    private SeckillGoodsKey(String prefix) {
        super(prefix);
    }

    public static SeckillGoodsKey seckillCount = new SeckillGoodsKey("sc");

    public static SeckillGoodsKey seckillInof = new SeckillGoodsKey("si");
}
