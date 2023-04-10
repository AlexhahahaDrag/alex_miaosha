package com.alex.finance.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wechat")
@Slf4j
@RequiredArgsConstructor
public class WeChatController {
    @Autowired
    private WxMpService wxMpService;

    @GetMapping("/authorize")
    public String authorize() throws WxErrorException {
        // 配置
        // 调用方法
        String result=wxMpService.getAccessToken();
        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData("keyword1", "这是个测试", "#A9A9A9");
        List<WxMpTemplateData> dataList = Lists.newArrayList();
        dataList.add(wxMpTemplateData);
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId("w7rOV66OVxoODGWPZnhyLmAylEWuspR77_3Smk7ivDE")
                .toUser("ol6WP5xKDRqutv_RQRFDyZdcJ3qk")
                .url("https://api.weixin.qq.com/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + result)
                .data(dataList)
                .build();
        String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        log.info("微信网页授权，获取code，result={}",result);
        wxMpService.getCurrentAutoReplyInfo().getKeywordAutoReplyInfo();
        return msgId;
    }

    @GetMapping("/getToken")
    public String getToken() throws WxErrorException {
        return wxMpService.getAccessToken();
    }


//    @GetMapping("/userInfo")
//    public String userInfo(@RequestParam("code") String code,
//                           @RequestParam("state") String url) {
//
//        WxMpOAuth2AccessToken wxMpOAuth2AccessToken=new WxMpOAuth2AccessToken();
//        try {
//            wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
//
//        } catch (WxErrorException e) {
//            log.error("微信网页授权，{}",e);
//            throw new SellException(ResultEnum.WX_MP_ERROR.getCode(),e.getError().getErrorMsg());
//        }
//        String openId = wxMpOAuth2AccessToken.getOpenId();
//        log.info("openId={}",openId);
//        return "redirect:"+url+"?openid="+openId;
//    }
}