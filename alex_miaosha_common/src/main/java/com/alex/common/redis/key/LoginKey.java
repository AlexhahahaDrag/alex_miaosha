package com.alex.common.redis.key;

/**
 * description:
 * author:       majf
 * createDate:   2022/10/9 10:04
 * version:      1.0.0
 */
public class LoginKey extends BasePrefix {

    private LoginKey(String prefix) {
        super(prefix);
    }

    public static LoginKey loginKey = new LoginKey("login:in");

    public static LoginKey loginLimitCount = new LoginKey("login:limit_count");

    public static LoginKey loginUuid = new LoginKey("login:uuid");

    public static LoginKey loginToken = new LoginKey("login:token");

    public static LoginKey loginIpSource = new LoginKey("login:ip_source");
}
