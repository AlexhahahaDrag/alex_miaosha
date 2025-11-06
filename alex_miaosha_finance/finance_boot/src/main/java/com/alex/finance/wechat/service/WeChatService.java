package com.alex.finance.wechat.service;

import me.chanjar.weixin.common.error.WxErrorException;

import java.math.BigDecimal;

public interface WeChatService {

    String getToken() throws WxErrorException;

    void sentMessage(String account, Long num) throws WxErrorException;

    void sentShopFinanceMessage(String date, BigDecimal saleAmount, BigDecimal saleNum) throws WxErrorException;
}

