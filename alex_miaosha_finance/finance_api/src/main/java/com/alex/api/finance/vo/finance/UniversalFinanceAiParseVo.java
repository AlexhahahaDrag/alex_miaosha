package com.alex.api.finance.vo.finance;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * description: 通用智能记账解析结果 VO (支持日常收支、购物小票、预付卡流水)
 * author: alex
 * createDate: 2026-10-19
 * version: 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UniversalFinanceAiParseVo", description = "通用智能记账解析结果")
public class UniversalFinanceAiParseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "识别的目标场景：DAILY_EXPENSE, SHOP_RECEIPT, PREPAID_CARD", example = "DAILY_EXPENSE")
    private String targetScene;

    @ApiModelProperty(value = "收支方向：expense(支出), income(收入), recharge(充值), consume(消费扣款)", example = "expense")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "记账金额", example = "128.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "账目名称/商户名/消费项目", example = "麦当劳汉堡套餐")
    private String name;

    @ApiModelProperty(value = "分类编码(如 catering, traffic, shopping, salary)", example = "catering")
    private String typeCode;

    @ApiModelProperty(value = "分类显示名称", example = "餐饮美食")
    private String typeName;

    @ApiModelProperty(value = "支付渠道编码(如 wx, zfb, yhk, xj, prepaid_card)", example = "wx")
    private String fromSource;

    @ApiModelProperty(value = "支付渠道显示名称", example = "微信")
    private String fromSourceName;

    @ApiModelProperty(value = "业务发生时间(格式 yyyy-MM-dd HH:mm:ss)", example = "2026-10-18 12:30:00")
    private String infoDate;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "归属人用户ID")
    private Long belongTo;

    @ApiModelProperty(value = "归属人姓名/昵称")
    private String belongToName;

    @ApiModelProperty(value = "备注或原始描述摘要")
    private String description;

    // --- 预付卡/储值卡场景扩展 ---
    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "匹配关联到的预付卡ID(Long2String)")
    private Long prepaidCardId;

    @ApiModelProperty(value = "匹配关联到的预付卡名称")
    private String prepaidCardName;

    @ApiModelProperty(value = "预付卡当前可用余额")
    private BigDecimal prepaidCardBalance;

    // --- 购物小票/明细清单场景扩展 ---
    @ApiModelProperty(value = "购物小票解析出的商品明细条目")
    private List<ReceiptItemVo> receiptItems;

    @ApiModelProperty(value = "解析引擎：AI_DEEPSEEK 或 LOCAL_REGEX_FALLBACK", example = "LOCAL_REGEX_FALLBACK")
    private String parseEngine;

    @ApiModelProperty(value = "解析推理说明或可信度提示")
    private String reasoning;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "ReceiptItemVo", description = "购物小票明细条目")
    public static class ReceiptItemVo implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "商品或条目名称", example = "鲜牛奶950ml")
        private String itemName;

        @ApiModelProperty(value = "单价", example = "16.80")
        private BigDecimal unitPrice;

        @ApiModelProperty(value = "数量", example = "2")
        private Integer quantity;

        @ApiModelProperty(value = "小计金额", example = "33.60")
        private BigDecimal subtotal;
    }
}
