package com.alex.finance.message.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 消息通知 8 大业务大类字典枚举
 */
@Getter
public enum NoticeBizType {

    /**
     * 全局消息总开关
     */
    GLOBAL("GLOBAL", "全局消息总开关", "一票控制所有渠道的消息接收，关闭时所有子项静音"),

    /**
     * 礼尚往来事件提醒
     */
    GIFT_EVENT("GIFT_EVENT", "礼尚往来事件提醒", "婚礼、满月、寿宴等重大事件提前1天/当天倒计时"),

    /**
     * 人情礼金回礼备忘
     */
    GIFT_RETURN("GIFT_RETURN", "人情礼金回礼备忘", "往来礼金盈余待回礼备忘提醒"),

    /**
     * 消费券到期提醒
     */
    COUPON("COUPON", "消费券到期提醒", "消费券提前3天/1天到期防作废提醒"),

    /**
     * 消费卡/猫超卡到期
     */
    PREPAID_CARD("PREPAID_CARD", "消费卡/猫超卡到期提醒", "预付卡、商超卡到期防作废提醒"),

    /**
     * 预算超支提醒
     */
    BUDGET_OVER("BUDGET_OVER", "预算超支强提醒", "月度零花钱实际支出超标报警"),

    /**
     * 店铺财务统计通知
     */
    FINANCE_REPORT("FINANCE_REPORT", "店铺财务经营通知", "店铺财务每日/每月经营流水汇总分析"),

    /**
     * 系统公告与广播
     */
    SYSTEM("SYSTEM", "系统公告与全局广播", "平台维护、版本升级与全局公告");

    private final String code;
    private final String name;
    private final String desc;

    NoticeBizType(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static NoticeBizType fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (NoticeBizType type : values()) {
            if (type.getCode().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }

    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
}
