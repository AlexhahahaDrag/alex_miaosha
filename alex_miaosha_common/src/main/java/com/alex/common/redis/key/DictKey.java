package com.alex.common.redis.key;

public class DictKey extends BasePrefix {

    private DictKey(String prefix) {
        super(prefix);
    }

    public static DictKey dictKey = new DictKey("dict");
}
