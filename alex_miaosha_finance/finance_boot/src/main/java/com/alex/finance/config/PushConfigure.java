package com.alex.finance.config;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * 配置类
 * Author:木芒果
 */
@Configuration
public class PushConfigure {

    private final WechatAccountConfig accountConfig;

    // AI Agent：使用构造函数注入替代字段注入，提高代码可测试性和安全性
    // 说明：构造函数注入可以确保依赖不为空，并且便于单元测试
    @Autowired
    public PushConfigure(WechatAccountConfig accountConfig) {
        Assert.notNull(accountConfig, "WechatAccountConfig must not be null!");
        this.accountConfig = accountConfig;
    }

    @Bean
    public WxMpService wxMpService() {
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage());
        return wxMpService;
    }

    @Bean
    public WxMpConfigStorage wxMpConfigStorage() {
        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(accountConfig.getAppId());
        wxMpConfigStorage.setSecret(accountConfig.getSecret());
        return wxMpConfigStorage;
    }
}