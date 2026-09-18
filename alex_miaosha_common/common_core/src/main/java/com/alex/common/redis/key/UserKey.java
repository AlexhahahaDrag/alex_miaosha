package com.alex.common.redis.key;

public class UserKey extends BasePrefix {

    private UserKey(String prefix) {
        super(prefix);
    }

    public static final UserKey getById = new UserKey("id");
}
