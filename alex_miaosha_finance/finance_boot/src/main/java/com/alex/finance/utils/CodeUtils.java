package com.alex.finance.utils;

import com.alex.common.redis.key.KeyPrefix;
import com.alex.common.redis.key.ShopStockKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeUtils {

    private final RedisUtils redisUtils;

    /**
     * param: key
     * param: date
     * param: info
     * description: 根据key、时间和其他信息获取编码
     * author:      majf
     * return:      java.lang.String
    */
    public String getCode(KeyPrefix key, String date, String... info){
        String countStr = redisUtils.get(key, date);
        Integer count = Integer.parseInt(StringUtils.isEmpty(countStr) ? "0" : countStr) + 1;
        redisUtils.set(ShopStockKey.shopStockKey, date, count, 24 * 60 * 60);
        StringBuilder code = new StringBuilder();
        for (String i : info) {
            code.append(i);
        }
        return code + date + getCode(count);
    }

    /**
     * param: count
     * description: 获取四位数编码
     * author:      majf
     * return:      java.lang.String
     */
    private String getCode(Integer count) {
        if (count < 10) {
            return "000" + count;
        } else if (count < 100) {
            return "00" + count;
        } else if (count < 1000) {
            return "0" + count;
        } else {
            return "" + count;
        }
    }
}
