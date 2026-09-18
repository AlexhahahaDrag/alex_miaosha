package com.alex.common.redis.key;

public class DictKey extends BasePrefix {

    private DictKey(String prefix) {
        super(prefix);
    }

    public static final DictKey dictKey = new DictKey("dict");
}
