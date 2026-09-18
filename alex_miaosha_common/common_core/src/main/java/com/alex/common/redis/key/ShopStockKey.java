package com.alex.common.redis.key;

public class ShopStockKey extends BasePrefix {

    private ShopStockKey(String prefix) {
        super(prefix);
    }

    public static final ShopStockKey shopStockKey = new ShopStockKey("shopStock");
}
