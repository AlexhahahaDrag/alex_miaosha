package com.alex.finance.service.weixin.impl;

import cn.hutool.http.HttpUtil;
import com.alex.finance.config.WechatAccountConfig;
import com.alex.finance.service.weixin.WeiXinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeiXinServiceImpl implements WeiXinService {

    private final WechatAccountConfig wechatAccountConfig;

    private final String url = "https://api.weixin.qq.com/cgi-bin";


    public String getToken() {
//        wxService.
//        return wxService
        return HttpUtil.get(url + "token?grant_type=client_credential&appid=" + wechatAccountConfig.getAppId() + "&secret=" + wechatAccountConfig.getSecret());
    }
}
