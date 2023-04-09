package com.alex.finance.controller;


import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wechat")
@Slf4j
public class WeChatController {
    @Autowired
    private WxMpService wxMpService;

    @GetMapping("/authorize")
    public String authorize() throws WxErrorException {

        String url="http://xiaoheihai.natapp1.cc/sell/wechat/userInfo";

        // 配置

        // 调用方法
        String result=wxMpService.getAccessToken();
        log.info("微信网页授权，获取code，result={}",result);
        return "redirect:"+result;
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