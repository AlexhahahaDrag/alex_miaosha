package com.alex.common.redis.key;

/**
 * description:
 * author:       majf
 * createDate:   2022/10/9 10:04
 * version:      1.0.0
 */
public class LoginIdKey extends BasePrefix {

    private LoginIdKey(String prefix) {
        super(prefix);
    }

    public static LoginIdKey loginIdKey = new LoginIdKey("login_in");

    public static LoginIdKey loginLimitCount = new LoginIdKey("login_limit_count");
}
