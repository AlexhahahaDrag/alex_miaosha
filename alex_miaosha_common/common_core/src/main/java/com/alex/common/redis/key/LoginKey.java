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

    public static final LoginKey loginKey = new LoginKey("login:in");

    public static final LoginKey loginLimitCount = new LoginKey("login:limit_count");

    public static final LoginKey loginUuid = new LoginKey("login:uuid");

    public static final LoginKey loginToken = new LoginKey("login:token");

    public static final LoginKey loginAdmin = new LoginKey("login:admin");

    public static final LoginKey loginIpSource = new LoginKey("login:ip_source");

    public static final LoginKey loginOnlineUser = new LoginKey("login:online_user");
}
