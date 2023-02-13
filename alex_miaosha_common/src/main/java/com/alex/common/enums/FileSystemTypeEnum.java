package com.alex.common.enums;

/**
 *@description:  文件系统类型
 *@author:       alex
 *@createDate:   2023/2/13 22:43
 *@version:      1.0.0
 */
public enum FileSystemTypeEnum {
    MINIO("minio", "MINIO");

    FileSystemTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    private String code;

    private String value;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
