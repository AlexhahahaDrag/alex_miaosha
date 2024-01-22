package com.alex.common.redis.key;

public class AccessKey extends BasePrefix {

    public AccessKey(String prefix) {
        super(prefix);
    }

    public static AccessKey withExpire = new AccessKey("access");
}
