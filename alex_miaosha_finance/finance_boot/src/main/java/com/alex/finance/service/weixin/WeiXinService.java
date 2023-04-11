package com.alex.finance.service.weixin;

import me.chanjar.weixin.common.error.WxErrorException;

public interface WeiXinService {

    String getToken() throws WxErrorException;

    String sentMessage(String account, Long num) throws WxErrorException;
}
