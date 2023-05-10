package com.alex.product.enums;

import lombok.Data;

public enum SourceType {
    JD("jd", "京东"),
    TB("tb", "淘宝"),
    ;

    SourceType(String code, String value) {
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
