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
    USER_BUCKET("user", "user-bucket", true),
    GOODS_BUCKET("goods", "goods-bucket", true),
    COMMON_BUCKET("common", "common-bucket", false),
    FINANCE_BUCKET("finance", "finance-bucket", false),
    GIFT_BUCKET("gift", "gift-bucket", false),
    AI_BUCKET("ai", "ai-bucket", false),
    ;

    private static final Map<String, String> NAME_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    e -> e.getName().toLowerCase(),
                    BucketNameEnum::getValue
            ));

    private static final Map<String, Boolean> PUBLIC_MAP = Arrays.stream(values())
            .flatMap(e -> java.util.stream.Stream.of(
                    java.util.Map.entry(e.getName().toLowerCase(), e.isPublic()),
                    java.util.Map.entry(e.getValue().toLowerCase(), e.isPublic())
            ))
            .collect(Collectors.toUnmodifiableMap(
                    java.util.Map.Entry::getKey,
                    java.util.Map.Entry::getValue,
                    (existing, replacement) -> existing
            ));

    BucketNameEnum(String name, String value) {
        this(name, value, false);
    }

    BucketNameEnum(String name, String value, boolean isPublic) {
        this.name = name;
        this.value = value;
        this.isPublic = isPublic;
    }

    private String name;

    private String value;

    private final boolean isPublic;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPublic() {
        return isPublic;
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

    /**
     * 判断存储桶或业务标识是否为公开只读存储桶（如 "user", "user-bucket", "goods", "goods-bucket"）
     *
     * @param bucketName 存储桶名称或业务标识
     * @return 是否公开
     */
    public static boolean isPublicBucket(String bucketName) {
        if (bucketName == null || bucketName.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(PUBLIC_MAP.get(bucketName.trim().toLowerCase()));
    }
}
