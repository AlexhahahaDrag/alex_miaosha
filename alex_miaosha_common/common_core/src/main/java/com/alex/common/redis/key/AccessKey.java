package com.alex.common.redis.key;

public class AccessKey extends BasePrefix {

    public AccessKey(String prefix) {
        super(prefix);
    }

    public static final AccessKey withExpire = new AccessKey("access");
}
