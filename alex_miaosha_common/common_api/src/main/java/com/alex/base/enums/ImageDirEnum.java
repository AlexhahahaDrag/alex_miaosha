package com.alex.base.enums;

/**
 *description:  图片类型枚举类
 *author:       majf
 *createDate:   2022/7/12 16:54
 *version:      1.0.0
 */
public enum ImageDirEnum implements BaseEnum {
    GOODS("0", "goods"),
    INDEX("1", "index");

    ImageDirEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    private String code;

    private String value;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }
}
