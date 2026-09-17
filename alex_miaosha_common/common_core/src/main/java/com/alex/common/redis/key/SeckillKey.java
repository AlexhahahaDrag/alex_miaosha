package com.alex.common.redis.key;

/**
 *description:  秒杀key
 *author:       majf
 *createDate:   2022/7/14 9:54
 *version:      1.0.0
 */
public class SeckillKey extends BasePrefix {

    private SeckillKey(String prefix) {
        super(prefix);
    }

    public static SeckillKey isGoodOver = new SeckillKey("go");

    public static SeckillKey getSeckillPath = new SeckillKey("mp");
}
