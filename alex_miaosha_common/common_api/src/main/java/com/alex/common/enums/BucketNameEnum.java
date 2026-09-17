package com.alex.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:  OSS 存储桶枚举字典 (支持 O(1) 无锁逆向快速查找)
 * author:       majf, alex
 * createDate:   2023/1/12 15:42
 * version:      2.0.0
 */
@Getter
public enum BucketNameEnum {
    USER_BUCKET("user", "user-bucket"),
    GOODS_BUCKET("goods", "goods-bucket"),
    COMMON_BUCKET("common", "common-bucket"),
    FINANCE_BUCKET("finance", "finance-bucket"),
    GIFT_BUCKET("gift", "gift-bucket"),
    AI_BUCKET("ai", "ai-bucket"),
    ;

    private static final Map<String, String> NAME_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    e -> e.getName().toLowerCase(),
                    BucketNameEnum::getValue
            ));

    BucketNameEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 根据业务名称忽略大小写快速查找预置存储桶名称
     *
     * @param name 业务类型标识 (如 "user", "goods", "finance", "gift", "ai")
     * @return 对应的 Bucket 名称 (如 "user-bucket")，未匹配时返回 null
     */
    public static String findValueByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return NAME_MAP.get(name.trim().toLowerCase());
    }
}
