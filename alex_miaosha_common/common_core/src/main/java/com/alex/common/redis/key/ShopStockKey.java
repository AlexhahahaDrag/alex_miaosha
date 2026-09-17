package com.alex.common.redis.key;

public class ShopStockKey extends BasePrefix {

    private ShopStockKey(String prefix) {
        super(prefix);
    }

    public static ShopStockKey shopStockKey = new ShopStockKey("shopStock");
}
