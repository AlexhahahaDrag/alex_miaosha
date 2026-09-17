package com.alex.common.enums;

import lombok.Getter;

/**
 * 用户消费券状态枚举
 *
 * @author alex
 * @createDate 2025-01-20
 * @version 1.0.0
 */
@Getter
public enum CpnUserCouponStatusEnum {

    UNUSED("UNUSED", "未使用"),
    USED("USED", "已使用"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String description;

    CpnUserCouponStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     */
    public static CpnUserCouponStatusEnum fromCode(String code) {
        for (CpnUserCouponStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}