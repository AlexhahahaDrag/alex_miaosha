package com.alex.finance.wechat.service;

import me.chanjar.weixin.common.error.WxErrorException;

import java.math.BigDecimal;

public interface WeChatService {

    String getToken() throws WxErrorException;

    void sentMessage(String account, Long num) throws WxErrorException;

    /**
     * 发送通用业务到期/事件提醒微信模板消息
     *
     * @param itemName   事项/卡券名称（对应模板变量 account）
     * @param numText    数量/金额/倒计时描述（对应模板变量 num）
     * @param remindType 提醒业务类型，如：礼尚往来事件 / 消费券到期 / 消费卡到期 / 猫超卡（对应模板变量 type）
     */
    void sendRemindTemplateMessage(String itemName, String numText, String remindType) throws WxErrorException;

    void sentShopFinanceMessage(String date, BigDecimal saleAmount, BigDecimal saleNum) throws WxErrorException;
}
