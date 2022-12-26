package com.alex.common.redis.key;

/**
 * @description:
 * @author: alex
 * @createDate: 2022/12/9 21:55
 * @version: 1.0.0
 */
public class CommonKey extends BasePrefix {

    private CommonKey(String prefix) {
        super(prefix);
    }

    public static CommonKey commonKey = new CommonKey("common");
}
