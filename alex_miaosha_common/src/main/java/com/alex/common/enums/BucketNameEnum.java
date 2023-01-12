package com.alex.common.enums;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/12 15:42
 * version:      1.0.0
 */
public enum BucketNameEnum {
    USER_BUCKET("user", "user-bucket"),
    GOODS_BUCKET("goods", "goods-bucket"),
    COMMON_BUCKET("common", "common-bucket"),
    ;

    BucketNameEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
